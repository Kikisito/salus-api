package com.kikisito.salus.api.service;

import com.kikisito.salus.api.dto.AuthenticationResponse;
import com.kikisito.salus.api.dto.LoginRequest;
import com.kikisito.salus.api.dto.RegisterRequest;
import com.kikisito.salus.api.entity.*;
import com.kikisito.salus.api.exception.*;
import com.kikisito.salus.api.repository.DireccionRepository;
import com.kikisito.salus.api.repository.PasswordResetRepository;
import com.kikisito.salus.api.repository.SessionRepository;
import com.kikisito.salus.api.repository.UserRepository;
import com.kikisito.salus.api.type.AccountStatusType;
import com.kikisito.salus.api.type.RoleType;
import com.kikisito.salus.api.type.TokenType;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    public static final String VERIFY_EMAIL_PATH = "/api/v1/auth/verification/verify?token=";

    @Value("${application.host}")
    private String host;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final PasswordResetRepository passwordResetRepository;

    @Autowired
    private final SessionRepository sessionRepository;

    @Autowired
    private final DireccionRepository direccionRepository;

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
    private final static String PASSWORD_RESET_EMAIL_TEXT = "Para recuperar tu contraseña haz click en el siguiente enlace: {link}";

    private final static Integer PASSWORD_RESET_EXPIRATION = 60 * 60; // SECONDS

    @Transactional
    public void sendPasswordRecoveryMail(String email) {
        // Obtenemos la entidad asociada al email
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(DataNotFoundException::userNotFound);

        // Calculamos cuando expira el token de contraseña olvidada
        Instant expiryInstant = Instant.now().plusSeconds(PASSWORD_RESET_EXPIRATION);

        // Si ya existe un PasswordResetEntity lo actualizamos, si no lo creamos
        PasswordResetEntity passwordResetEntity = passwordResetRepository.findByUserEntity(userEntity)
                .orElse(PasswordResetEntity.builder().userEntity(userEntity).build());

        passwordResetEntity.setToken(jwtService.generatePasswordResetToken(userEntity, PASSWORD_RESET_EXPIRATION));
        passwordResetEntity.setExpiryDate(Date.from(expiryInstant));
        passwordResetEntity.setUsed(false);

        PasswordResetEntity finalPasswordResetEntity = passwordResetRepository.save(passwordResetEntity);

        String recoverLink = host + "/api/v1/auth/recover?token=" + finalPasswordResetEntity.getToken();
        emailingService.sendEmail(email, PASSWORD_RESET_EMAIL_SUBJECT, PASSWORD_RESET_EMAIL_TEXT.replace("{link}", recoverLink));
    }

    @Transactional
    public void recoverPassword(String token, String newPassword) {
        // Obtenemos el PasswordResetEntity
        PasswordResetEntity passwordReset = passwordResetRepository.findByToken(token).orElseThrow(DataNotFoundException::tokenNotFound);

        // Comprobamos que no ha caducado el token
        if (passwordReset.getExpiryDate().before(new Date())) {
            throw InvalidTokenException.tokenExpired();
        }

        // Comprobamos que no se ha usado el token
        if (passwordReset.isUsed()) {
            throw InvalidTokenException.tokenAlreadyUsed();
        }

        // Cambiamos la contraseña y guardamos
        UserEntity userEntity = passwordReset.getUserEntity();
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(userEntity);

        passwordReset.setUsed(true);
        passwordResetRepository.save(passwordReset);
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
        UserEntity userEntity = UserEntity.builder()
                .nif(request.getNif())
                .nombre(request.getNombre())
                .apellidos(request.getApellidos())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .telefono(request.getTelefono())
                .fechaNacimiento(request.getFechaNacimiento())
                .accountStatusType(AccountStatusType.NOT_VERIFIED)
                .verificationToken(jwtService.generateEmailVerificationToken(request.getEmail()))
                .rolesList(roleTypeList)
                .build();

        UserEntity savedUser = saveCredentials(userEntity);

        // La dirección es opcional, pero si se ha proporcionado la guardamos
        if(request.getDireccion() != null) {
            DireccionEntity direccionEntity = DireccionEntity.builder()
                    .lineaDireccion1(request.getDireccion().getLineaDireccion1())
                    .lineaDireccion2(request.getDireccion().getLineaDireccion2())
                    .codigoPostal(request.getDireccion().getCodigoPostal())
                    .pais(request.getDireccion().getPais())
                    .provincia(request.getDireccion().getProvincia())
                    .municipio(request.getDireccion().getMunicipio())
                    .localidad(request.getDireccion().getLocalidad())
                    .build();
            DireccionEntity savedDireccion = saveDireccion(direccionEntity);

            // Asignamos la dirección al usuario
            savedUser.setDireccion(savedDireccion);
            userRepository.save(savedUser);
        }

        // Generamos los tokens de acceso y enviamos el email de verificación
        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);
        String publicId = getPublicId();

        String verificationLink = host + VERIFY_EMAIL_PATH + savedUser.getVerificationToken();
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

        String verificationLink = host + VERIFY_EMAIL_PATH + savedUser.getVerificationToken();
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

    private UserEntity saveCredentials(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    private DireccionEntity saveDireccion(DireccionEntity direccionEntity) {
        return direccionRepository.save(direccionEntity);
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