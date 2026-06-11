package com.mundial.view;

import com.mundial.entity.*;
import com.mundial.repository.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;

@Route(value = "bienvenida", layout = MainLayout.class)
public class BienvenidaView extends VerticalLayout implements BeforeEnterObserver {

    private final UsuarioRepository usuarioRepository;
    private final PronosticoRepository pronosticoRepository;
    private final PartidoRepository partidoRepository;

    private H1 lblSaludo;
    private H3 lblPuntos;
    private H3 lblRanking;
    private H3 lblCompletado;

    public BienvenidaView(UsuarioRepository usuarioRepository, 
                          PronosticoRepository pronosticoRepository, 
                          PartidoRepository partidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pronosticoRepository = pronosticoRepository;
        this.partidoRepository = partidoRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.CENTER);

        // 1. GRAN BIENVENIDA
        lblSaludo = new H1("¡Bienvenido al Mundial!");
        lblSaludo.getStyle().set("margin-top", "40px").set("color", "var(--lumo-primary-text-color)");
        add(lblSaludo);

        Paragraph instrucciones = new Paragraph("Revisa tus estadísticas en tiempo real y gestiona tus predicciones desde el menú superior.");
        instrucciones.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-bottom", "20px");
        add(instrucciones);

        // 2. PANEL DE ESTADÍSTICAS DEL JUGADOR
        HorizontalLayout panelEstadisticas = new HorizontalLayout();
        panelEstadisticas.setWidthFull();
        panelEstadisticas.setMaxWidth("900px");
        panelEstadisticas.setSpacing(true);

        lblPuntos = new H3("0 pts");
        Div cardPuntos = crearTarjetaMetrica("Mis Puntos Acumulados", lblPuntos, VaadinIcon.TROPHY, "#eab308");
        
        lblRanking = new H3("Cargando...");
        Div cardRanking = crearTarjetaMetrica("Mi Posición Global", lblRanking, VaadinIcon.CHEVRON_UP_SMALL, "#3b82f6");
        
        lblCompletado = new H3("0 / 0");
        Div cardProgreso = crearTarjetaMetrica("Pronósticos Completados", lblCompletado, VaadinIcon.CHECK_CIRCLE, "#22c55e");

        panelEstadisticas.add(cardPuntos, cardRanking, cardProgreso);
        panelEstadisticas.setFlexGrow(1, cardPuntos, cardRanking, cardProgreso);
        add(panelEstadisticas);

        // 3. BOTÓN DE ACCIÓN CENTRAL
        Button btnIrAQuiniela = new Button("Gestionar Mis Pronósticos", VaadinIcon.GAMEPAD.create());
        btnIrAQuiniela.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        btnIrAQuiniela.getStyle().set("margin-top", "40px").set("padding", "15px 40px");
        btnIrAQuiniela.addClickListener(e -> UI.getCurrent().navigate(PartidosView.class));
        add(btnIrAQuiniela);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long usuarioId = (Long) VaadinSession.getCurrent().getAttribute("USUARIO_ID");
        if (usuarioId == null) {
            event.rerouteTo(""); // Corregido sin marcas extrañas
            return;
        }

        Usuario usuarioLogueado = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuarioLogueado != null) {
            lblSaludo.setText("⚽ ¡La quiniela retonera, " + usuarioLogueado.getNombre() + "! ⚽"); // Corregido sin marcas extrañas
            lblPuntos.setText(usuarioLogueado.getPuntosQuiniela() + " PTS");

            List<Usuario> todosLosUsuarios = usuarioRepository.findAll();
            todosLosUsuarios.sort((u1, u2) -> Integer.compare(u2.getPuntosQuiniela(), u1.getPuntosQuiniela()));
            int posicion = 1;
            for (Usuario u : todosLosUsuarios) {
                if (u.getId().equals(usuarioLogueado.getId())) break;
                posicion++;
            }
            lblRanking.setText("# " + posicion + " de " + todosLosUsuarios.size());

            long totalPartidos = partidoRepository.count();
            long pronosticosHechos = pronosticoRepository.findAll().stream()
                    .filter(p -> p.getUsuario().getId().equals(usuarioId) && p.getTendenciaElegida() != null)
                    .count();
            lblCompletado.setText(pronosticosHechos + " de " + totalPartidos);
        }
    }

    private Div crearTarjetaMetrica(String titulo, H3 componenteValor, VaadinIcon icono, String colorHex) {
        Div card = new Div();
        card.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "12px")
                .set("padding", "20px")
                .set("display", "flex")
                .set("align-items", "center")
                .set("gap", "15px")
                .set("box-shadow", "0 4px 6px -1px rgba(0,0,0,0.05)");

        var iconElement = icono.create();
        iconElement.getStyle().set("font-size", "32px").set("color", colorHex);

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        Paragraph pTitulo = new Paragraph(titulo);
        pTitulo.getStyle().set("margin", "0").set("color", "var(--lumo-secondary-text-color)").set("font-size", "14px");
        componenteValor.getStyle().set("margin", "5px 0 0 0").set("font-weight", "bold");

        infoLayout.add(pTitulo, componenteValor);
        card.add(iconElement, infoLayout);
        return card;
    }
}