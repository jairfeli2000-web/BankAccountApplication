package com.bank.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bankAccountOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank Account API")
                        .description("Microservicio bancario para gestión de cuentas, consignaciones, retiros y consulta de saldos")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Bank Account Service")
                                .email("support@bank.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
