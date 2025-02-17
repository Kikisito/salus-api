package com.kikisito.salus.api.embeddable;

import jakarta.persistence.*;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@Embeddable
public class DireccionEmbeddable {
    private String lineaDireccion1;
    private String lineaDireccion2;
    private String codigoPostal;
    private String pais;
    private String provincia;
    private String municipio;
    private String localidad;
}