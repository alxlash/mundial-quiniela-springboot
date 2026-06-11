package com.mundial.service;

import com.mundial.entity.Equipo;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import org.springframework.stereotype.Service;

@Service
public class InterfazService {

    /**
     * Crea un componente visual (HorizontalLayout) con la bandera oficial en PNG
     * y el nombre del país al lado. ¡Compatible con todos los navegadores!
     */
    public HorizontalLayout crearCeldaConBandera(String emojiBandera, String nombrePais) {
        HorizontalLayout fila = new HorizontalLayout();
        fila.setAlignItems(Alignment.CENTER);
        fila.setSpacing(true);

        // 1. Convertimos el emoji a código ISO de dos letras
        String codigoPais = obtenerCodigoIsoDesdeEmoji(emojiBandera);

        // 2. Creamos la imagen apuntando a la API de banderas
        Image banderaImg = new Image(
            "https://flagcdn.com/w40/" + codigoPais + ".png", 
            "Bandera de " + nombrePais
        );
        banderaImg.setWidth("24px");
        banderaImg.getStyle().set("border-radius", "3px");
        banderaImg.getStyle().set("box-shadow", "0 1px 2px rgba(0,0,0,0.1)");

        // 3. Texto con el nombre del país
        Span nombreTxt = new Span(nombrePais);

        // Armamos la fila
        fila.add(banderaImg, nombreTxt);
        return fila;
    }

    /**
     * Algoritmo matemático interno para extraer el código ISO de un emoji de bandera
     */
    private String obtenerCodigoIsoDesdeEmoji(String emoji) {
        if (emoji == null || emoji.length() < 4) {
            return "un"; // Bandera de la ONU o por defecto si algo falla
        }
        try {
            int cp1 = emoji.codePointAt(0) - 0x1F1E6 + 'a';
            int cp2 = emoji.codePointAt(2) - 0x1F1E6 + 'a';
            return "" + (char) cp1 + (char) cp2;
        } catch (Exception e) {
            return "un";
        }
    }
}