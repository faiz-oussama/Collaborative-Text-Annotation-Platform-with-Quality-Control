package com.annotations.demo.config;

import com.annotations.demo.controller.CustomErrorController;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    public ErrorController customErrorController() {
        return new CustomErrorController();
    }
}
