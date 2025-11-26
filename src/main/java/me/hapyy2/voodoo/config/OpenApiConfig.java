package me.hapyy2.voodoo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI voodooOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VooDoo Task Manager API")
                        .description("API for managing tasks, categories, and generating reports. All endpoints are secured and support multi-tenancy (users see only their own data).")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Hapyy2")
                                .email("https://github.com/hapyy2")));
    }
}