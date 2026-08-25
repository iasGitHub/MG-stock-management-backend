package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class FournisseurResponse {

    private Long id;
    private String code;
    private String nom;
    private String telephone;
    private String adresse;
    private LocalDateTime dateCreation;
}
