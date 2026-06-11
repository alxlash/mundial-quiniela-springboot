package com.mundial.view;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.VaadinSession;

public class MainLayout extends AppLayout {

    public MainLayout() {
        H1 logo = new H1("🏆 Quiniela Mundial 2026");
        logo.getStyle()
            .set("font-size", "var(--lumo-font-size-l)")
            .set("margin", "0")
            .set("padding-right", "20px")
            .set("color", "var(--lumo-primary-text-color)");

        // Enlaces públicos para todos los usuarios
        RouterLink linkInicio = new RouterLink("🏠 Inicio", BienvenidaView.class);
        RouterLink linkPartidos = new RouterLink("⚽ Mi Quiniela", PartidosView.class);
        RouterLink linkGrupos = new RouterLink("📊 Grupos", GruposView.class);
        RouterLink linkGeneral = new RouterLink("📅 Calendario", GeneralPartidosView.class);
        RouterLink linkRanking = new RouterLink("⭐ Ranking", RankingView.class);

        // Creamos el menú base
        HorizontalLayout menuEnlaces = new HorizontalLayout(linkInicio, linkPartidos, linkGrupos, linkGeneral, linkRanking);
        
        // 🛡️ COMPROBACIÓN DE SEGURIDAD EN EL MENÚ
        Object usuarioNombre = VaadinSession.getCurrent().getAttribute("USUARIO_NOMBRE");
        if (usuarioNombre != null && "admin".equalsIgnoreCase(String.valueOf(usuarioNombre))) {
            // Si el que navega es el admin, le fabricamos e inyectamos su botón secreto
            RouterLink linkAdmin = new RouterLink("⚙️ Admin", AdminPartidosView.class);
            menuEnlaces.add(linkAdmin);
        }

        menuEnlaces.setSpacing(true);
        menuEnlaces.getStyle().set("margin-left", "10px");

        Button btnLogout = new Button(VaadinIcon.SIGN_OUT.create());
        btnLogout.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
        btnLogout.addClickListener(e -> {
            VaadinSession.getCurrent().close();
            getUI().ifPresent(ui -> ui.navigate(""));
        });

        HorizontalLayout derechaLayout = new HorizontalLayout(btnLogout);

        HorizontalLayout barraSuperior = new HorizontalLayout(logo, menuEnlaces, derechaLayout);
        barraSuperior.setWidthFull();
        barraSuperior.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        barraSuperior.expand(menuEnlaces); 
        
        barraSuperior.getStyle()
            .set("padding", "10px 20px")
            .set("border-bottom", "1px solid var(--lumo-contrast-10pct)")
            .set("background-color", "var(--lumo-base-color)");

        addToNavbar(barraSuperior);
    }
}