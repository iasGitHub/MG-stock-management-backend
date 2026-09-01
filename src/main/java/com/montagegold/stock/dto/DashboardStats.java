package com.montagegold.stock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DashboardStats {

    private long totalProducts;
    private long productsInAlert;
    private int totalQuantity;
    private double stockValue;
    private long monthlyEntries;
    private long monthlyExits;
}
