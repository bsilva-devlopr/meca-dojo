package br.com.mecadojo.api.identity.security;

import br.com.mecadojo.api.identity.domain.RoleName;
import br.com.mecadojo.api.shared.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final JwtProperties properties;
    private final ObjectMapper objectMapper;

    public JwtService(JwtProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        if (properties.secret() == null || properties.secret().length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be configured with at least 32 characters");
        }
    }

    public String issueAccessToken(UUID userId, UUID tenantId, Set<RoleName> roles) {
        Instant now = Instant.now();
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", userId.toString());
        payload.put("tenant", tenantId.toString());
        payload.put("roles", roles.stream().map(Enum::name).sorted().toList());
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", now.plus(properties.accessTokenTtl()).getEpochSecond());

        String unsigned = encodeJson(header) + "." + encodeJson(payload);
        return unsigned + "." + sign(unsigned);
    }

    public JwtClaims parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new UnauthorizedException("Invalid access token");
            }
            String unsigned = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsigned), parts[2])) {
                throw new UnauthorizedException("Invalid access token");
            }
            Map<String, Object> payload = objectMapper.readValue(
                URL_DECODER.decode(parts[1]),
                new TypeReference<Map<String, Object>>() {
                }
            );
            long exp = ((Number) payload.get("exp")).longValue();
            if (Instant.ofEpochSecond(exp).isBefore(Instant.now())) {
                throw new UnauthorizedException("Access token expired");
            }
            UUID userId = UUID.fromString((String) payload.get("sub"));
            UUID tenantId = UUID.fromString((String) payload.get("tenant"));
            @SuppressWarnings("unchecked")
            List<String> roleValues = (List<String>) payload.get("roles");
            Set<RoleName> roles = roleValues.stream().map(RoleName::valueOf).collect(java.util.stream.Collectors.toSet());
            return new JwtClaims(userId, tenantId, roles);
        } catch (UnauthorizedException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new UnauthorizedException("Invalid access token");
        }
    }

    private String encodeJson(Object value) {
        try {
            return URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not encode JWT", exception);
        }
    }

    private String sign(String unsigned) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            return URL_ENCODER.encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign JWT", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return java.security.MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.UTF_8),
            actual.getBytes(StandardCharsets.UTF_8)
        );
    }

    public record JwtClaims(UUID userId, UUID tenantId, Set<RoleName> roles) {
    }
}
