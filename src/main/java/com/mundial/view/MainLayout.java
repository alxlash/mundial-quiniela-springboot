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
        H1 logo = new H1("🏆 Quiniela retonera 'Mundial 2026' ⚽ ");
        logo.getStyle()
	        .set("--lumo-font-size-s", "28px") // Aumenta el tamaño del texto de la cabecera (por defecto es 13px)
	        .set("--vaadin-grid-header-cell-font-weight", "700") // Lo hace más robusto/negrita
	        .set("--vaadin-grid-header-cell-color", "var(--lumo-header-text-color)") // Color de título destacado
            
            .set("margin", "0")
            .set("padding-right", "20px")
            .set("color", "var(--lumo-primary-text-color)");

        // Enlaces públicos para todos los usuarios
        RouterLink linkInicio = new RouterLink(" 🏠 Inicio  ", BienvenidaView.class);
	        linkInicio.getElement().getStyle()
	        .set("font-size", "20px")    // Define el tamaño exacto en píxeles (ej: 16px o 18px)
	        .set("font-weight", "500");  // Controla el grosor del texto
        RouterLink linkPartidos = new RouterLink(" ⚽ Mi Quiniela  ", PartidosView.class);
	        linkPartidos.getElement().getStyle()
	        .set("font-size", "20px")    // Define el tamaño exacto en píxeles (ej: 16px o 18px)
	        .set("font-weight", "500");  // Controla el grosor del texto
        RouterLink linkGrupos = new RouterLink(" 📊 Grupos  ", GruposView.class);
	        linkGrupos.getElement().getStyle()
	        .set("font-size", "20px")    // Define el tamaño exacto en píxeles (ej: 16px o 18px)
	        .set("font-weight", "500");  // Controla el grosor del texto        
        RouterLink linkGeneral = new RouterLink(" 📅 Calendario  ", GeneralPartidosView.class);
 	        linkGeneral.getElement().getStyle()
	        .set("font-size", "20px")    // Define el tamaño exacto en píxeles (ej: 16px o 18px)
	        .set("font-weight", "500");  // Controla el grosor del texto       
        RouterLink linkRanking = new RouterLink(" ⭐ Ranking ", RankingView.class);
	        linkRanking.getElement().getStyle()
	        .set("font-size", "20px")    // Define el tamaño exacto en píxeles (ej: 16px o 18px)
	        .set("font-weight", "500");  // Controla el grosor del texto 
        // Creamos el menú base
        HorizontalLayout menuEnlaces = new HorizontalLayout(linkInicio, linkPartidos, linkGrupos, linkGeneral, linkRanking);
        
        // 🛡️ COMPROBACIÓN DE SEGURIDAD EN EL MENÚ
        Object usuarioNombre = VaadinSession.getCurrent().getAttribute("USUARIO_NOMBRE");
        if (usuarioNombre != null && "Alex".equalsIgnoreCase(String.valueOf(usuarioNombre))) {
            // Si el que navega es el admin, le fabricamos e inyectamos su botón secreto
            RouterLink linkAdmin = new RouterLink("⚙️", AdminPartidosView.class);
	 	        linkAdmin.getElement().getStyle()
		        .set("font-size", "24px")    // Define el tamaño exacto en píxeles (ej: 16px o 18px)
		        .set("font-weight", "500");  // Controla el grosor del texto            
            menuEnlaces.add(linkAdmin);
        }

        menuEnlaces.setSpacing(true);
menuEnlaces.getStyle().set("margin-left", "10px").setHeight("20px");

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
