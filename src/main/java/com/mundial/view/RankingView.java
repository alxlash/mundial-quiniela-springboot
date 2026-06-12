package com.mundial.view;

import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

import com.mundial.entity.Usuario;
import com.mundial.repository.PartidoRepository;
import com.mundial.repository.PronosticoRepository;
import com.mundial.repository.UsuarioRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;

@Route(value = "ranking", layout = MainLayout.class)
public class RankingView extends VerticalLayout implements BeforeEnterObserver {

    private final UsuarioRepository usuarioRepository;
    private final PronosticoRepository pronosticoRepository;
    private final PartidoRepository partidoRepository;

    private Grid<Usuario> gridRanking;
    private long totalPartidosMundial;

    public RankingView(UsuarioRepository usuarioRepository, 
                       PronosticoRepository pronosticoRepository, 
                       PartidoRepository partidoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.pronosticoRepository = pronosticoRepository;
        this.partidoRepository = partidoRepository;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // 1. Encabezado de la Sección
        H2 titulo = new H2("⭐ Tabla General de Posiciones de la Quiniela ⭐");
        H4 subtitulo = new H4("¡Compite con tus amigos y alcanza la cima del torneo!");
        subtitulo.getStyle().set("color", "var(--lumo-secondary-text-color)").set("margin-top", "0");
        add(titulo, subtitulo);

        // Cacheamos el total de partidos para calcular el progreso de los usuarios
        this.totalPartidosMundial = partidoRepository.count();

        // 2. Configuración de la Tabla de Posiciones
        gridRanking = new Grid<>(Usuario.class, false);
        gridRanking.setSizeFull();

        // Columna 1: Medalla / Posición
        gridRanking.addComponentColumn(usuario -> {
            List<Usuario> listaOrdenada = obtenerUsuariosOrdenados();
            int lugar = listaOrdenada.indexOf(usuario) + 1;

            HorizontalLayout celdaPosicion = new HorizontalLayout();
            celdaPosicion.setAlignItems(Alignment.CENTER);

            if (lugar == 1) {
                Span oro = new Span("🥇 1º");
                oro.getStyle().set("font-weight", "bold").set("color", "#eab308");
                celdaPosicion.add(oro);
            } else if (lugar == 2) {
                Span plata = new Span("🥈 2º");
                plata.getStyle().set("font-weight", "bold").set("color", "#94a3b8");
                celdaPosicion.add(plata);
            } else if (lugar == 3) {
                Span bronce = new Span("🥉 3º");
                bronce.getStyle().set("font-weight", "bold").set("color", "#b45309");
                celdaPosicion.add(bronce);
            } else {
                Span comun = new Span("# " + lugar);
                comun.getStyle().set("color", "var(--lumo-secondary-text-color)");
                celdaPosicion.add(comun);
            }

            // Resaltar visualmente la fila completa si es el usuario logueado actualmente
            Long actualLogueadoId = (Long) VaadinSession.getCurrent().getAttribute("USUARIO_ID");
            if (actualLogueadoId != null && actualLogueadoId.equals(usuario.getId())) {
                celdaPosicion.add(new Span(" (Tú)"));
                celdaPosicion.getStyle().set("font-weight", "bold");
            }

            return celdaPosicion;
        }).setHeader("Posición").setWidth("120px").setFlexGrow(0);

        // Columna 2: Nombre del Participante
        gridRanking.addColumn(Usuario::getNombre)
                .setHeader("Participante")
                .setAutoWidth(true)
                .setFlexGrow(1);

        // Columna 3: Puntos Totales Acumulados (Destacados)
        gridRanking.addComponentColumn(usuario -> {
            Span puntos = new Span(usuario.getPuntosQuiniela() + " pts");
            puntos.getStyle()
                    .set("background-color", "var(--lumo-success-color-10pct)")
                    .set("color", "var(--lumo-success-text-color)")
                    .set("padding", "4px 10px")
                    .set("border-radius", "8px")
                    .set("font-weight", "bold");
            return puntos;
        }).setHeader("Puntos Totales").setWidth("140px").setFlexGrow(0);

        // Columna 4: Barra de Progreso de Predicciones Llenadas
        gridRanking.addComponentColumn(usuario -> {
            // Contamos cuántas tendencias válidas ha guardado este usuario específico
            long pronosticosHechos = pronosticoRepository.findAll().stream()
                    .filter(pr -> pr.getUsuario().getId().equals(usuario.getId()) && pr.getTendenciaElegida() != null)
                    .count();

            double porcentaje = totalPartidosMundial > 0 ? (double) pronosticosHechos / totalPartidosMundial : 0.0;

            ProgressBar barra = new ProgressBar();
            barra.setValue(porcentaje);
            barra.setWidth("120px");

            Span txtProgreso = new Span(pronosticosHechos + "/" + totalPartidosMundial);
            txtProgreso.getStyle().set("font-size", "12px").set("color", "var(--lumo-secondary-text-color)");

            HorizontalLayout layoutProgreso = new HorizontalLayout(barra, txtProgreso);
            layoutProgreso.setAlignItems(Alignment.CENTER);
            layoutProgreso.setSpacing(true);

            return layoutProgreso;
        }).setHeader("Progreso Quiniela").setAutoWidth(true);

        add(gridRanking);
getStyle().setWidth("50%");
setAlignItems(Alignment.CENTER);
    }

    /**
     * Carga y refresca los datos de la tabla cada vez que el usuario entra a la pestaña
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long usuarioId = (Long) VaadinSession.getCurrent().getAttribute("USUARIO_ID");
        if (usuarioId == null) {
            event.rerouteTo(""); // Si expiró la sesión, mandarlo al Login
            return;
        }

        // Asignamos la lista ordenada al Grid para que se pinte correctamente
        gridRanking.setItems(obtenerUsuariosOrdenados());
    }

    /**
     * Método auxiliar para jalar de SQLite todos los participantes y ordenarlos de mayor a menor puntaje
     */
    private List<Usuario> obtenerUsuariosOrdenados() {
        List<Usuario> lista = usuarioRepository.findAll();
        // Orden descendente por puntos de la quiniela
        lista.sort((u1, u2) -> Integer.compare(u2.getPuntosQuiniela(), u1.getPuntosQuiniela()));
        return lista;
    }
}
