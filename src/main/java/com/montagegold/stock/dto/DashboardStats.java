package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardStats {

    private long totalProduits;
    private long produitsEnAlerte;
    private int quantiteTotale;
    private double valeurStock;
    private long entreesDuMois;
    private long sortiesDuMois;
}
