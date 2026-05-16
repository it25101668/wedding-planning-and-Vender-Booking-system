package com.example.user_management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Registers a custom static-resource handler so that images stored in
 * the WMS/Img/ folder on disk are served at the /pkg-img/** URL path.
 *
 * Example:  /pkg-img/abc123.jpg  →  <project-root>/Img/abc123.jpg
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Absolute path to WMS/Img/ using the JVM working directory (project root)
        String imgDir = Paths.get(System.getProperty("user.dir"))
                .resolve("Img")
                .toUri()
                .toString();   // e.g. "file:/C:/Users/.../WMS/Img/"

        // Make sure it ends with /
        if (!imgDir.endsWith("/")) {
            imgDir = imgDir + "/";
        }

        registry.addResourceHandler("/pkg-img/**")
                .addResourceLocations(imgDir);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Map the root URL (/) to templates/index.html
        registry.addViewController("/").setViewName("index");
        // Redirect dashboard to users list as a default
        registry.addRedirectViewController("/admin/dashboard", "/admin/users");
    }
}
