package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.response.AuthenticationResponse;
import com.kikisito.salus.api.dto.request.LoginRequest;
import com.kikisito.salus.api.dto.request.RegisterRequest;
import com.kikisito.salus.api.embeddable.DireccionEmbeddable;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.*;
import com.kikisito.salus.api.repository.PasswordResetRepository;
import com.kikisito.salus.api.repository.SessionRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.AccountStatusType;
import com.kikisito.salus.api.type.RoleType;
import com.kikisito.salus.api.type.TokenType;
import java.security.SecureRandom;
import java.util.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${application.host}")
    private String host;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordResetRepository passwordResetRepository;

    @Autowired
    private final SessionRepository sessionRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private final JwtService jwtService;

    @Autowired
    private final AuthenticationManager authenticationManager;

    @Autowired
    private final EmailingService emailingService;

    private static final SecureRandom secureRandom = new SecureRandom(); // thread safe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    private final static String VERIFY_EMAIL_SUBJECT = "Verifica tu correo electrónico";
    private final static String VERIFY_EMAIL_TEXT = "Te damos la bienvenida. Por favor, verifica tu correo electrónico a través del siguiente enlace: {link}";

    private final static String PASSWORD_RESET_EMAIL_SUBJECT = "Recuperación de contraseña";
    private final static String PASSWORD_RESET_EMAIL_TEXT = "Para recuperar tu contraseña haz click en el siguiente enlace: {token}";

    @Transactional
    public void sendPasswordRecoveryMail(String email, String nif) {
        // Obtenemos la entidad asociada al email
        Optional<UserEntity> userEntityOptional = userRepository.findByEmailAndNif(email, nif);

        // Pese a que no exista el usuario, no informamos al usuario de que no existe y abandonamos el proceso (por seguridad)
        if(userEntityOptional.isEmpty()) {
            return;
        }

        // Obtenemos el usuario (aquí ya sabemos que existe)
        UserEntity userEntity = userEntityOptional.get();

        // Si ya existe un PasswordResetEntity lo actualizamos, si no lo creamos
        PasswordResetEntity passwordResetEntity = passwordResetRepository.findByUserEntity(userEntity)
                .orElse(PasswordResetEntity.builder().userEntity(userEntity).build());

        passwordResetEntity.setToken(jwtService.generatePasswordResetToken(userEntity));

        PasswordResetEntity finalPasswordResetEntity = passwordResetRepository.save(passwordResetEntity);

        emailingService.sendEmail(email, PASSWORD_RESET_EMAIL_SUBJECT, PASSWORD_RESET_EMAIL_TEXT.replace("{token}", finalPasswordResetEntity.getToken()));
    }

    @Transactional
    public void recoverPassword(String token, String newPassword) {
        // Obtenemos el PasswordResetEntity
        PasswordResetEntity passwordReset = passwordResetRepository.findByToken(token).orElseThrow(DataNotFoundException::tokenNotFound);

        // Comprobamos que no ha caducado el token
        if (jwtService.isTokenExpired(token)) {
            throw InvalidTokenException.tokenExpired();
        }

        // Cambiamos la contraseña y guardamos
        UserEntity userEntity = passwordReset.getUserEntity();
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);

        // Borramos el token
        passwordResetRepository.delete(passwordReset);
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Comprobaciones previas (email/dni existentes)
        userRepository.findByEmail(request.getEmail()).ifPresent((userEntity) -> {
            throw ConflictException.emailIsRegistered();
        });

        userRepository.findByNif(request.getNif()).ifPresent((userEntity) -> {
            throw ConflictException.nifIsRegistered();
        });

        // Creamos el usuario y la dirección
        List<RoleType> roleTypeList = new ArrayList<>();
        roleTypeList.add(RoleType.USER);

        // La dirección es opcional, pero si se ha proporcionado la guardamos
        DireccionEmbeddable direccionEmbeddable = DireccionEmbeddable.builder()
                .lineaDireccion1(request.getDireccion().getLineaDireccion1())
                .lineaDireccion2(request.getDireccion().getLineaDireccion2())
                .codigoPostal(request.getDireccion().getCodigoPostal())
                .pais(request.getDireccion().getPais())
                .provincia(request.getDireccion().getProvincia())
                .municipio(request.getDireccion().getMunicipio())
                .localidad(request.getDireccion().getLocalidad())
                .build();

        UserEntity userEntity = UserEntity.builder()
                .nif(request.getNif())
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .sexo(request.getSexo())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .direccion(direccionEmbeddable)
                .accountStatusType(AccountStatusType.NOT_VERIFIED)
                .verificationToken(jwtService.generateEmailVerificationToken(request.getEmail()))
                .rolesList(roleTypeList)
                .build();

        UserEntity savedUser = saveCredentials(userEntity);

        // Generamos los tokens de acceso y enviamos el email de verificación
        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);
        String publicId = getPublicId();

        String verificationLink = host + "VERIFY_EMAIL_PATH" + savedUser.getVerificationToken();
        emailingService.sendEmail(request.getEmail(),
                VERIFY_EMAIL_SUBJECT,
                VERIFY_EMAIL_TEXT.replace("{link}", verificationLink));
        saveUserSession(savedUser, accessToken, refreshToken, publicId);

        String jwt = jwtService.generateJWT(userEntity, accessToken, refreshToken, publicId);
        return AuthenticationResponse.builder()
                .jwt(jwt)
                .build();
    }

    @Transactional
    public void verifyEmailByToken(String token) {
        UserEntity userEntity = userRepository.findByVerificationToken(token).orElseThrow(InvalidTokenException::tokenAlreadyUsed);

        if(userEntity.getAccountStatusType() == AccountStatusType.VERIFIED) {
            throw ConflictException.emailIsVerified();
        }

        userEntity.setVerificationToken(null);
        userEntity.setAccountStatusType(AccountStatusType.VERIFIED);

        userRepository.save(userEntity);
    }

    @Transactional
    public void resendVerificationEmail(UserEntity userEntity) {
        if (userEntity.getAccountStatusType() == AccountStatusType.VERIFIED) {
            throw ConflictException.emailIsVerified();
        }

        // Generamos un nuevo token y lo enviamos
        userEntity.setVerificationToken(jwtService.generateEmailVerificationToken(userEntity.getEmail()));
        UserEntity savedUser = saveCredentials(userEntity);

        String verificationLink = host + "VERIFY_EMAIL_PATH" + savedUser.getVerificationToken();
        emailingService.sendEmail(savedUser.getEmail(), VERIFY_EMAIL_SUBJECT, VERIFY_EMAIL_TEXT.replace("{link}", verificationLink));
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        UserEntity userEntity = userRepository.findByNif(request.getNif())
                .orElseThrow(DataNotFoundException::userNotFound);

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(userEntity.getEmail(), request.getPassword()));

        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);
        String publicId = getPublicId();

        saveUserSession(userEntity, accessToken, refreshToken, publicId);

        String jwt = jwtService.generateJWT(userEntity, accessToken, refreshToken, publicId);

        return AuthenticationResponse.builder()
                .jwt(jwt)
                .build();
    }

    @Transactional
    public void logout(String jwt) {
        sessionRepository.findByAccessToken(jwtService.extractAccessToken(jwt)).ifPresent(sessionRepository::delete);
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);

        if(userEntity.isPresent()) {
            throw ConflictException.emailIsRegistered();
        } else {
            return true;
        }
    }

    @Transactional(readOnly = true)
    public boolean isNifAvailable(String nif) {
        Optional<UserEntity> userEntity = userRepository.findByNif(nif);

        if(userEntity.isPresent()) {
            throw ConflictException.nifIsRegistered();
        } else {
            return true;
        }
    }

    private UserEntity saveCredentials(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    private void saveUserSession(UserEntity userEntity, String accessToken, String refreshToken, String publicId) {
        SessionEntity session = SessionEntity.builder()
                .user(userEntity)
                .publicId(publicId)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(TokenType.BEARER)
                .build();
        sessionRepository.save(session);
    }

    public static String getPublicId() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}