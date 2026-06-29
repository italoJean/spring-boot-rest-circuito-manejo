package com.spring.boot.carro.circuito_manejo.presentation.controller;

import com.spring.boot.carro.circuito_manejo.configuration.app.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Autenticacion", description = "Endpoints relacionados con la sesion autenticada del usuario.")
@SecurityRequirement(name = OpenApiConfig.SESSION_AUTH_SCHEME)
public class AuthenticationController {

    @GetMapping("/user-info")
    @Operation(summary = "Obtener informacion del usuario autenticado", description = "Devuelve los atributos expuestos por el proveedor OAuth2 del usuario con sesion activa.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Informacion del usuario autenticado obtenida correctamente"),
            @ApiResponse(responseCode = "401", description = "No existe una sesion autenticada")
    })
    public Map<String, Object> getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Collections.singletonMap("error", "No autenticado");
        }
        return principal.getAttributes();
    }
}
