package com.utms.ygk;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralized composite score calculation for YGK evaluation.
 *
 * Formula (max 100 points):
 *   GPA component      = (gpa / 4.00) × 60   (60 % weight)
 *   Language component = (languageScore / 100) × 40   (40 % weight)
 *   Adjustment         = manual override within ±5 points (policy cap)
 *   compositeScore     = GPA_component + Language_component + adjustment
 *
 * All inputs are validated before computation.
 */
@Service
public class CompositeScoreService {

    private static final BigDecimal GPA_MAX       = new BigDecimal("4.00");
    private static final BigDecimal LANGUAGE_MAX  = new BigDecimal("100");
    private static final BigDecimal GPA_WEIGHT    = new BigDecimal("60");
    private static final BigDecimal LANG_WEIGHT   = new BigDecimal("40");
    private static final BigDecimal ADJUSTMENT_CAP = new BigDecimal("5");

    /**
     * Computes the composite score.
     *
     * @param gpa           student GPA (0.00 – 4.00)
     * @param languageScore language exam/assessment score (0 – 100)
     * @param adjustment    manual policy adjustment (capped at ±5)
     * @return composite score rounded to 2 decimal places
     */
    public BigDecimal compute(BigDecimal gpa, BigDecimal languageScore, BigDecimal adjustment) {
        validateGpa(gpa);
        validateLanguageScore(languageScore);
        BigDecimal safeAdjustment = clampAdjustment(adjustment);

        BigDecimal gpaComponent  = gpa.divide(GPA_MAX, 10, RoundingMode.HALF_UP).multiply(GPA_WEIGHT);
        BigDecimal langComponent = languageScore.divide(LANGUAGE_MAX, 10, RoundingMode.HALF_UP).multiply(LANG_WEIGHT);

        return gpaComponent.add(langComponent).add(safeAdjustment).setScale(2, RoundingMode.HALF_UP);
    }

    private void validateGpa(BigDecimal gpa) {
        if (gpa == null || gpa.compareTo(BigDecimal.ZERO) < 0 || gpa.compareTo(GPA_MAX) > 0) {
            throw new IllegalArgumentException("GPA must be between 0.00 and 4.00");
        }
    }

    private void validateLanguageScore(BigDecimal languageScore) {
        if (languageScore == null || languageScore.compareTo(BigDecimal.ZERO) < 0
                || languageScore.compareTo(LANGUAGE_MAX) > 0) {
            throw new IllegalArgumentException("Language score must be between 0 and 100");
        }
    }

    private BigDecimal clampAdjustment(BigDecimal adjustment) {
        if (adjustment == null) return BigDecimal.ZERO;
        if (adjustment.compareTo(ADJUSTMENT_CAP) > 0)  return ADJUSTMENT_CAP;
        if (adjustment.compareTo(ADJUSTMENT_CAP.negate()) < 0) return ADJUSTMENT_CAP.negate();
        return adjustment;
    }
}
