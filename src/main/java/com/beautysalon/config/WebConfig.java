package com.beautysalon.config;

import com.beautysalon.repository.EmpresaRepository;
import com.beautysalon.repository.UserRepository;
import com.beautysalon.tenant.TenantInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final EmpresaRepository empresaRepository;
    private final UserRepository userRepository;

    public WebConfig(EmpresaRepository empresaRepository, UserRepository userRepository) {
        this.empresaRepository = empresaRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(new TenantInterceptor(empresaRepository, userRepository))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "/robots.txt",
                        "/sitemap.xml",
                        "/error"
                );
    }

    @org.springframework.beans.factory.annotation.Value("${upload.dir:uploads}")
    private String uploadDir = "uploads";

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadUri = uploadPath.toUri().toString();
        if (!uploadUri.endsWith("/")) {
            uploadUri += "/";
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri);

        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }

    @Override
    public void addFormatters(@NonNull org.springframework.format.FormatterRegistry registry) {
        registry.addConverter(new com.beautysalon.converter.StringToBigDecimalConverter());
        registry.addConverter(new com.beautysalon.converter.StringToDoubleConverter());
        registry.addConverter(new com.beautysalon.converter.StringToIntegerConverter());
        registry.addConverter(new com.beautysalon.converter.StringToLocalDateConverter());
    }
}
