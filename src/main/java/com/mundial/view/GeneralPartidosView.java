package com.mundial.view;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "general", layout = MainLayout.class)
public class GeneralPartidosView extends VerticalLayout {
    public GeneralPartidosView() {
        add(new H2("📅 Calendario General de Partidos (Próximamente)"));
    }
}