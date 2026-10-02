package br.com.mecadojo.api.graduation.api;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.BeltResponse;
import br.com.mecadojo.api.graduation.domain.BeltSystem;
import br.com.mecadojo.api.graduation.service.BeltService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/belts")
@Tag(name = "Belts")
public class BeltController {
    private final BeltService beltService;

    public BeltController(BeltService beltService) {
        this.beltService = beltService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GRADUATION_READ')")
    List<BeltResponse> list(@RequestParam(required = false) BeltSystem system) {
        return beltService.list(system);
    }
}
