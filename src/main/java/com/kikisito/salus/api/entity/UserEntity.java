package com.kikisito.salus.api.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.kikisito.salus.api.StringListConverter;
import com.kikisito.salus.api.type.AccountStatusType;
import com.kikisito.salus.api.type.RoleType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@SuperBuilder
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nif;

    @Column
    private String nombre;

    @Column
    private String apellidos;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    // No es único ya que es común que un miembro de una familia gestione las cuentas de otros miembros
    @Column
    private String telefono;

    @Column
    private Date fechaNacimiento;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "direccion_id", referencedColumnName = "id")
    private DireccionEntity direccion;

    @Convert(converter = StringListConverter.class)
    @Enumerated(EnumType.STRING)
    private List<RoleType> rolesList;

    @Column
    private String verificationToken;

    @Column(columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AccountStatusType accountStatusType = AccountStatusType.NOT_VERIFIED;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<SessionEntity> sessions;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authoritiesList = new ArrayList<>();
        for (RoleType role : rolesList) {
            authoritiesList.add(new SimpleGrantedAuthority(role.name()));
        }
        return authoritiesList;
    }

    @OneToMany(mappedBy = "paciente", fetch = FetchType.LAZY)
    private List<CitaEntity> citasPaciente;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getPassword() {
        return password;
    }

}