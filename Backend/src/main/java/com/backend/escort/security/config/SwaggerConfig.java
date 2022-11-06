package com.backend.escort.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build().apiInfo(info());
    }

    private ApiInfo info() {
        return new ApiInfoBuilder()
                .title("EscortMe API")
                .description("EscortMe Backend project API")
                .version("1.1.0")
                .license("No License")
                .licenseUrl("No License")
                .contact(new Contact("Tony Lebea", "","Lebeatony.tl@gmail.com"))
                .build();

    }

}
