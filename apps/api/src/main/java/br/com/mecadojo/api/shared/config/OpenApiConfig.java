package br.com.mecadojo.api.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI mecaDojoOpenApi() {
        return new OpenAPI()
            .info(new Info()
                .title("Meca Dojo API")
                .version("0.0.1")
                .description("Foundation API for the Meca Dojo platform."));
    }
}
