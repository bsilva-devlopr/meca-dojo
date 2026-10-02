package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.graduation.domain.Belt;
import br.com.mecadojo.api.graduation.domain.BeltSystem;
import br.com.mecadojo.api.graduation.repository.BeltRepository;
import br.com.mecadojo.api.shared.exception.BadRequestException;
import br.com.mecadojo.api.shared.exception.NotFoundException;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class BeltResolver {
    private final BeltRepository beltRepository;

    public BeltResolver(BeltRepository beltRepository) {
        this.beltRepository = beltRepository;
    }

    public Belt resolve(String rawCode, BeltSystem system) {
        String code = normalizeCode(rawCode);
        if (system != null) {
            return beltRepository.findBySystemAndCodeAndActiveTrue(system, code)
                .orElseThrow(() -> new NotFoundException("Belt not found"));
        }
        var matches = beltRepository.findAllByCodeAndActiveTrueOrderBySystemAscSequenceAsc(code);
        if (matches.isEmpty()) {
            throw new NotFoundException("Belt not found");
        }
        if (matches.size() > 1) {
            throw new BadRequestException("Belt system is required for this belt code");
        }
        return matches.getFirst();
    }

    public Belt next(Belt current) {
        return beltRepository.findBySystemAndSequenceAndActiveTrue(current.getSystem(), current.getSequence() + 1)
            .orElseThrow(() -> new BadRequestException("Current belt has no next belt in its system"));
    }

    private String normalizeCode(String rawCode) {
        if (rawCode == null || rawCode.isBlank()) {
            throw new BadRequestException("Belt code is required");
        }
        return rawCode.trim().toUpperCase(Locale.ROOT);
    }
}
