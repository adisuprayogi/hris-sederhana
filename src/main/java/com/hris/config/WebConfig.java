package com.hris.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Web Configuration
 * Configures MVC-related settings
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configure CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }

    /**
     * Configure resource handlers for static files
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Upload directory for profile photos
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:./uploads/");

        // Dist resources (built CSS/JS from npm)
        registry.addResourceHandler("/dist/**")
                .addResourceLocations("classpath:/static/dist/");

        // Static resources
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }

    /**
     * Register view controllers for simple pages
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/auth/login");
    }

    /**
     * Register custom formatters for date handling
     */
    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Formatter for LocalDate to work with HTML5 date input (yyyy-MM-dd format)
        registry.addFormatter(new Formatter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            public String print(LocalDate date, Locale locale) {
                if (date == null) {
                    return "";
                }
                return date.format(formatter);
            }

            @Override
            public LocalDate parse(String text, Locale locale) throws ParseException {
                if (text == null || text.trim().isEmpty()) {
                    return null;
                }
                return LocalDate.parse(text, formatter);
            }
        });
    }
}
