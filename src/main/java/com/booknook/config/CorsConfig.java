package com.booknook.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig — allows the browser to call our API from any origin.
 *
 * CORS (Cross-Origin Resource Sharing) is a browser security rule.
 * Without this, a browser running on port 3000 (or any other origin)
 * would be blocked from calling our API on port 8080.
 * Since our frontend is served from the same server (port 8080),
 * this is mainly needed for development tools and direct API testing.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")          // apply to all /api/* endpoints
                .allowedOrigins("*")            // allow any origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
