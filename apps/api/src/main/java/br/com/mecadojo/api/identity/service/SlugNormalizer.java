package br.com.mecadojo.api.identity.service;

import java.text.Normalizer;
import java.util.Locale;

public final class SlugNormalizer {
    private SlugNormalizer() {
    }

    public static String slug(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value.trim(), Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9]+", "-")
            .replaceAll("(^-|-$)", "");
        return normalized;
    }

    public static String email(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
