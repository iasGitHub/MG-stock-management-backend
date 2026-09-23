package com.montagegold.stock.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PageableFactoryTest {

    private static final Set<String> ALLOWED = Set.of("name", "reference");

    @Test
    void sizeIsCappedToMax() {
        Pageable pageable = PageableFactory.of(0, 5000, "name", "asc", "name", ALLOWED);
        assertThat(pageable.getPageSize()).isEqualTo(PageableFactory.MAX_SIZE);
    }

    @Test
    void negativeSizeFallsBackToOne() {
        Pageable pageable = PageableFactory.of(0, -5, "name", "asc", "name", ALLOWED);
        assertThat(pageable.getPageSize()).isEqualTo(1);
    }

    @Test
    void negativePageIsClampedToZero() {
        Pageable pageable = PageableFactory.of(-3, 10, "name", "asc", "name", ALLOWED);
        assertThat(pageable.getPageNumber()).isZero();
    }

    @Test
    void unknownSortColumnFallsBackToDefault() {
        Pageable pageable = PageableFactory.of(0, 10, "passwordHash", "asc", "name", ALLOWED);
        assertThat(pageable.getSort().stream().findFirst().orElseThrow().getProperty())
                .isEqualTo("name");
    }

    @Test
    void allowedSortColumnIsUsed() {
        Pageable pageable = PageableFactory.of(0, 10, "reference", "desc", "name", ALLOWED);
        assertThat(pageable.getSort().stream().findFirst().orElseThrow().getProperty())
                .isEqualTo("reference");
        assertThat(pageable.getSort().stream().findFirst().orElseThrow().isDescending())
                .isTrue();
    }

    @Test
    void invalidDirectionFallsBackToAscending() {
        Pageable pageable = PageableFactory.of(0, 10, "name", "sideways", "name", ALLOWED);
        assertThat(pageable.getSort().stream().findFirst().orElseThrow().isAscending())
                .isTrue();
    }

    @Test
    void fixedSortOverloadCapsSizeToo() {
        Pageable pageable = PageableFactory.of(0, 999, "movementDate", Sort.Direction.DESC);
        assertThat(pageable.getPageSize()).isEqualTo(PageableFactory.MAX_SIZE);
        assertThat(pageable.getSort().stream().findFirst().orElseThrow().isDescending())
                .isTrue();
    }
}
