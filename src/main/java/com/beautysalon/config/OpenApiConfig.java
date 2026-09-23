package com.beautysalon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BeautySalon & LUMORA - API REST Backend")
                        .version("v1.0.0")
                        .description("Documentação interativa da API REST para Gestão de Salões de Beleza, Clínicas de Estética e Barbearias (Multi-Tenant).")
                        .contact(new Contact()
                                .name("Suporte BeautySalon")
                                .email("suporte@beautysalon.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")));
    }
}
