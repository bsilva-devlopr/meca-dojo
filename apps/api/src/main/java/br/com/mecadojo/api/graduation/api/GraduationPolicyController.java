package br.com.mecadojo.api.graduation.api;

import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.GraduationPolicyResponse;
import br.com.mecadojo.api.graduation.api.dto.GraduationDtos.UpdateGraduationPolicyRequest;
import br.com.mecadojo.api.graduation.domain.BeltSystem;
import br.com.mecadojo.api.graduation.service.GraduationPolicyService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/graduation-policies")
@Tag(name = "Graduation Policies")
public class GraduationPolicyController {
    private final GraduationPolicyService graduationPolicyService;

    public GraduationPolicyController(GraduationPolicyService graduationPolicyService) {
        this.graduationPolicyService = graduationPolicyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GRADUATION_POLICY_READ')")
    List<GraduationPolicyResponse> list() {
        return graduationPolicyService.list();
    }

    @PutMapping("/{beltCode}")
    @PreAuthorize("hasAuthority('GRADUATION_POLICY_UPDATE')")
    GraduationPolicyResponse update(
        @PathVariable String beltCode,
        @RequestParam(required = false) BeltSystem system,
        @Valid @RequestBody UpdateGraduationPolicyRequest request
    ) {
        return graduationPolicyService.update(beltCode, system, request);
    }
}
