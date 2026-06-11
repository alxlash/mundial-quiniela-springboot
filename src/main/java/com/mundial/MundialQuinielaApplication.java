package com.mundial;

import com.vaadin.flow.component.dependency.StyleSheet;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// 🔥 LA SOLUCIÓN DEFINITIVA: Vaadin descarga e inyecta la fuente de forma nativa en el HTML
@StyleSheet("https://cdn.jsdelivr.net/npm/country-flag-emoji-polyfill@0.1.8/dist/TwemojiCountryFlags.css")
@SpringBootApplication
public class MundialQuinielaApplication {

    public static void main(String[] args) {
        SpringApplication.run(MundialQuinielaApplication.class, args);
    }
}