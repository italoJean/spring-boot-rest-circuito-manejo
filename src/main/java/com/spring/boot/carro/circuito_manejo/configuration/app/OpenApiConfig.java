package com.spring.boot.carro.circuito_manejo.configuration.app;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String SESSION_AUTH_SCHEME = "sessionAuth";

    @Bean
    public OpenAPI circuitoManejoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Circuito de Manejo API")
                        .version("v1")
                        .description("Documentacion de la API REST para la gestion de clientes, paquetes, pagos, reservas, vehiculos y reportes del circuito de manejo.")
                        .contact(new Contact()
                                .name("Equipo Circuito de Manejo")
                                .email("soporte@circuito-manejo.local"))
                        .license(new License()
                                .name("Uso interno")
                                .url("https://example.com/internal-use")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Entorno local")
                ))
                .addSecurityItem(new SecurityRequirement().addList(SESSION_AUTH_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(SESSION_AUTH_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Sesion autenticada generada por Spring Security tras iniciar sesion con OAuth2/Google.")));
    }

    @Bean
    public GroupedOpenApi gestionApi() {
        return GroupedOpenApi.builder()
                .group("gestion")
                .pathsToMatch("/api/**")
                .build();
    }
}
