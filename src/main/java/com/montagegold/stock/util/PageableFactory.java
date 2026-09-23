package com.montagegold.stock.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Construit un Pageable sûr : page >= 0, taille bornée et tri restreint a
 * une liste blanche (protection contre les colonnes inconnues et les
 * tailles de page excessives passees en paramètre).
 */
public final class PageableFactory {

    /** Taille maximale d'une page, quel que soit le paramètre reçu. */
    public static final int MAX_SIZE = 100;

    private PageableFactory() {
    }

    /**
     * @param sortBy         colonne demandée (tombe sur le défaut si hors liste blanche)
     * @param sortDir        {@code asc} ou {@code desc} (toute autre valeur => asc)
     * @param defaultSortBy  colonne par défaut, doit appartenir à {@code allowedSortBy}
     * @param allowedSortBy  liste blanche des colonnes triables
     */
    public static Pageable of(int page, int size, String sortBy, String sortDir,
                              String defaultSortBy, Set<String> allowedSortBy) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDir)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        String property = sortBy != null && allowedSortBy.contains(sortBy)
                ? sortBy
                : defaultSortBy;
        return of(page, size, property, direction);
    }

    /** Variante pour un tri fixe (ex. mouvements toujours triés par date desc). */
    public static Pageable of(int page, int size, String sortBy, Sort.Direction direction) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        return PageRequest.of(safePage, safeSize, Sort.by(direction, sortBy));
    }
}
