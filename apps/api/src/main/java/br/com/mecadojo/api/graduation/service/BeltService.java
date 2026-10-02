package br.com.mecadojo.api.graduation.service;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.BeltResponse;
import br.com.mecadojo.api.graduation.domain.BeltSystem;
import br.com.mecadojo.api.graduation.repository.BeltRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BeltService {
    private final BeltRepository beltRepository;

    public BeltService(BeltRepository beltRepository) {
        this.beltRepository = beltRepository;
    }

    @Transactional(readOnly = true)
    public List<BeltResponse> list(BeltSystem system) {
        var belts = system == null
            ? beltRepository.findAllByActiveTrueOrderBySystemAscSequenceAsc()
            : beltRepository.findAllBySystemAndActiveTrueOrderBySequenceAsc(system);
        return belts.stream().map(GraduationMapper::belt).toList();
    }
}
