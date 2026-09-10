package com.example.backendgymteo1.config;

import com.example.backendgymteo1.config.security.CurrentUser;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Comparator;
import java.util.List;

@Configuration
public class SwaggerConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(CurrentUser.class);
    }

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    private static final List<String> ORDERED_TAGS = List.of(
            "Autenticación",
            "Roles",
            "Usuarios",
            "Socios",
            "Planes de Membresía",
            "Membresías"
    );

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Gimnasio Backend API")
                        .description("Documentación interactiva de la API con autenticación JWT Bearer - Backend Gym")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Gym")
                                .email("soporte@gym.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .tags(List.of(
                        new Tag().name("Autenticación").description("Endpoints para registro, inicio y cierre de sesión en el gimnasio"),
                        new Tag().name("Roles").description("Consulta del catálogo de roles del gimnasio"),
                        new Tag().name("Usuarios").description("Gestión integral de usuarios y perfiles"),
                        new Tag().name("Socios").description("Operaciones de administración y consulta de socios del gimnasio"),
                        new Tag().name("Planes de Membresía").description("Operaciones CRUD para la consulta, creación, actualización y eliminación de planes y tarifas del gimnasio"),
                        new Tag().name("Membresías").description("Operaciones para la emisión, consulta y gestión de membresías de socios")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa tu token JWT para autenticarte (sin la palabra 'Bearer ')")));
    }

    @Bean
    public OpenApiCustomizer sortTagsCustomizer() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().sort(Comparator.comparingInt(tag -> {
                    int index = ORDERED_TAGS.indexOf(tag.getName());
                    return index != -1 ? index : 999;
                }));
            }
        };
    }
}
