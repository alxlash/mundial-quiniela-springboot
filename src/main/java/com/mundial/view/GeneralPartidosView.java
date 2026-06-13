package com.mundial.view;

import com.mundial.entity.Equipo;
import com.mundial.entity.Partido;
import com.mundial.repository.EquipoRepository;
import com.mundial.repository.PartidoRepository;
import com.mundial.service.InterfazService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route(value = "calendario", layout = MainLayout.class)
public class GeneralPartidosView extends VerticalLayout implements BeforeEnterObserver {

    private final PartidoRepository partidoRepository;
    private final EquipoRepository equipoRepository; 
    private final InterfazService interfazService;

    private Grid<Partido> gridCalendario;
    private Tabs tabsFases;
    private Tabs tabsJornadas; // 🔥 Selector secundario exclusivo para Grupos
    
    // Pestañas Principales
    private Tab tabGrupos;
    private Tab tabDieciseisavos;
    private Tab tabOctavos;
    private Tab tabCuartos;
    private Tab tabSemis;
    private Tab tabFinal;

    // Sub-pestañas de Grupos
    private Tab tabJornada1;
    private Tab tabJornada2;
    private Tab tabJornada3;

    public GeneralPartidosView(PartidoRepository partidoRepository, 
                              EquipoRepository equipoRepository, 
                              InterfazService interfazService) {
        this.partidoRepository = partidoRepository;
        this.equipoRepository = equipoRepository;
        this.interfazService = interfazService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // 1. Encabezado
        H2 titulo = new H2("📅 Calendario Completo de Partidos 🏆");
        titulo.getStyle().set("margin-bottom", "5px");
        Span subtitulo = new Span("Consulta las fechas, horarios, sedes y resultados oficiales del torneo.");
        subtitulo.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(titulo, subtitulo);

        // 2. Pestañas Principales (Fases)
        tabGrupos = new Tab("Fase de Grupos 👥");
        tabDieciseisavos = new Tab("Dieciseisavos 🧭");
        tabOctavos = new Tab("Octavos de Final ⚔");
        tabCuartos = new Tab("Cuartos de Final 🏟");
        tabSemis = new Tab("Semifinales 🚀");
        tabFinal = new Tab("Gran Final 👑");

        tabsFases = new Tabs(tabGrupos, tabDieciseisavos, tabOctavos, tabCuartos, tabSemis, tabFinal);
        tabsFases.setWidthFull();
        add(tabsFases);

        // 3. 🔥 Sub-Pestañas de Jornadas (Solo se ven si "Fase de Grupos" está activo)
        tabJornada1 = new Tab("Jornada 1 (Partidos 1 - 24)");
        tabJornada2 = new Tab("Jornada 2 (Partidos 25 - 48)");
        tabJornada3 = new Tab("Jornada 3 (Partidos 49 - 72)");

        tabsJornadas = new Tabs(tabJornada1, tabJornada2, tabJornada3);
        tabsJornadas.setWidthFull();
        tabsJornadas.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "8px");
        add(tabsJornadas);

        // Listeners de Navegación
        tabsFases.addSelectedChangeListener(event -> evaluarVisibilidadYFiltrar());
        tabsJornadas.addSelectedChangeListener(event -> filtrarPartidosPorFase());

        // 4. Configuración del Grid
        gridCalendario = new Grid<>(Partido.class, false);
        gridCalendario.setSizeFull();
gridCalendario.getStyle().set("--lumo-size-m", "80px").setWidth("100%");

        // COLUMNA 1: Info Temporal
        gridCalendario.addComponentColumn(partido -> {
            VerticalLayout tempoLayout = new VerticalLayout();
            tempoLayout.setPadding(false);
            tempoLayout.setSpacing(false);

            Span badgeNumero = new Span("N° " + partido.getNumeroPartido());
            badgeNumero.getStyle()
                    .set("font-size", "11px")
                    .set("font-weight", "bold")
                    .set("background-color", "var(--lumo-primary-color-10pct)")
                    .set("color", "var(--lumo-primary-text-color)")
                    .set("padding", "2px 8px")
                    .set("border-radius", "12px");

            String txtFecha = (partido.getFecha() != null) ? partido.getFecha().format(DateTimeFormatter.ofPattern("dd MMM, yyyy")) : "Por definir";
            String txtHora = (partido.getHora() != null) ? partido.getHora().toString() : "--:--";
            
            Span fechaLabel = new Span(txtFecha);
            fechaLabel.getStyle().set("font-weight", "600").set("font-size", "14px").set("margin-top", "6px");
            
            Span horaLabel = new Span(new Icon(VaadinIcon.CLOCK), new Span(" " + txtHora));
            horaLabel.getStyle().set("font-size", "12px").set("color", "var(--lumo-secondary-text-color)");
            horaLabel.getChildren().filter(c -> c instanceof Icon).forEach(i -> ((Icon)i).setSize("12px"));

            tempoLayout.add(badgeNumero, fechaLabel, horaLabel);
            return tempoLayout;
        }).setHeader("Info. Partido").setWidth("150px").setFlexGrow(0);

        // COLUMNA 2: Enfrentamiento
        gridCalendario.addComponentColumn(partido -> {
            HorizontalLayout matchLayout = new HorizontalLayout();
            matchLayout.setWidthFull();
            matchLayout.setAlignItems(Alignment.CENTER);
            matchLayout.setJustifyContentMode(JustifyContentMode.CENTER);

            Component localCelda = interfazService.crearCeldaConBandera(partido.getBanderaLocal(), partido.getNombreLocal());
            
            HorizontalLayout marcadorLayout = new HorizontalLayout();
            marcadorLayout.getStyle()
                    .set("background-color", "var(--lumo-contrast-10pct)")
                    .set("padding", "6px 16px")
                    .set("border-radius", "20px")
                    .set("margin", "0 20px")
                    .set("font-weight", "bold")
                    .set("min-width", "60px")
                    .set("text-align", "center");

            if (partido.isJugado()) {
                H3 score = new H3(partido.getGolesLocal() + " - " + partido.getGolesVisitante());
                score.getStyle().set("margin", "0").set("color", "var(--lumo-success-text-color)");
                marcadorLayout.add(score);
                marcadorLayout.getStyle().set("background-color", "var(--lumo-success-color-10pct)");
            } else {
                Span vs = new Span("VS");
                vs.getStyle().set("color", "var(--lumo-secondary-text-color)");
                marcadorLayout.add(vs);
            }

            Component visitanteCelda = interfazService.crearCeldaConBandera(partido.getBanderaVisitante(), partido.getNombreVisitante());

            matchLayout.add(localCelda, marcadorLayout, visitanteCelda);
            return matchLayout;
        }).setHeader("Enfrentamiento / Resultado").setFlexGrow(1);

        // COLUMNA 3: Sede
        gridCalendario.addComponentColumn(partido -> {
            VerticalLayout ubicacionLayout = new VerticalLayout();
            ubicacionLayout.setPadding(false);
            ubicacionLayout.setSpacing(false);

            Span txtEstadio = new Span(partido.getEstadio() != null ? partido.getEstadio() : "Estadio por asignar");
            txtEstadio.getStyle().set("font-weight", "600").set("font-size", "13px");

            String localizacion = (partido.getCiudad() != null ? partido.getCiudad() : "Ubicación") 
                    + (partido.getPaisSede() != null ? ", " + partido.getPaisSede() : "");
            
            Span txtSede = new Span(new Icon(VaadinIcon.MAP_MARKER), new Span(" " + localizacion));
            txtSede.getStyle().set("font-size", "12px").set("color", "var(--lumo-secondary-text-color)").set("margin-top", "2px");
            txtSede.getChildren().filter(c -> c instanceof Icon).forEach(i -> ((Icon)i).setSize("11px"));

            ubicacionLayout.add(txtEstadio, txtSede);
            return ubicacionLayout;
        }).setHeader("Sede y Localización").setWidth("250px").setFlexGrow(0);

        add(gridCalendario);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long usuarioId = (Long) VaadinSession.getCurrent().getAttribute("USUARIO_ID");
        if (usuarioId == null) {
            event.rerouteTo("");
            return;
        }
        tabsFases.setSelectedTab(tabGrupos);
        tabsJornadas.setSelectedTab(tabJornada1);
        evaluarVisibilidadYFiltrar();
    }

    /**
     * 👁️ Controla si se muestran o no las sub-pestañas de jornadas de grupos
     */
    private void evaluarVisibilidadYFiltrar() {
        boolean esFaseGrupos = tabsFases.getSelectedTab().equals(tabGrupos);
        tabsJornadas.setVisible(esFaseGrupos); // Se oculta mágicamente si entras a Octavos, Semis, etc.
        filtrarPartidosPorFase();
    }

    /**
     * 🔍 Lógica de Filtrado por Segmentos Exactos
     */
    private void filtrarPartidosPorFase() {
        Tab faseSeleccionada = tabsFases.getSelectedTab();
        String filtroFase = "";
        
        int idMinimo = 1;
        int idMaximo = 999;
        boolean filtrarPorRango = false;

        if (faseSeleccionada.equals(tabGrupos)) {
            filtroFase = "GRUPO";
            filtrarPorRango = true;
            
            // ✂️ Segmentamos matemáticamente por el número de partido
            Tab jornadaSeleccionada = tabsJornadas.getSelectedTab();
            if (jornadaSeleccionada.equals(tabJornada1)) {
                idMinimo = 1;
                idMaximo = 24;
            } else if (jornadaSeleccionada.equals(tabJornada2)) {
                idMinimo = 25;
                idMaximo = 48;
            } else if (jornadaSeleccionada.equals(tabJornada3)) {
                idMinimo = 49;
                idMaximo = 72;
            }
        } else if (faseSeleccionada.equals(tabDieciseisavos)) {
            filtroFase = "DIECISEISAVOS";
        } else if (faseSeleccionada.equals(tabOctavos)) {
            filtroFase = "OCTAVOS";
        } else if (faseSeleccionada.equals(tabCuartos)) {
            filtroFase = "CUARTOS";
        } else if (faseSeleccionada.equals(tabSemis)) {
            filtroFase = "SEMIFINAL";
        } else if (faseSeleccionada.equals(tabFinal)) {
            filtroFase = "FINAL";
        }

        final String faseBuscar = filtroFase;
        final int min = idMinimo;
        final int max = idMaximo;
        final boolean aplicarRango = filtrarPorRango;

        List<Equipo> todosLosEquipos = equipoRepository.findAll();

        List<Partido> partidosFiltrados = partidoRepository.findAll().stream()
                .filter(p -> p.getFase() != null && p.getFase().toUpperCase().contains(faseBuscar))
                .filter(p -> !aplicarRango || (p.getNumeroPartido() >= min && p.getNumeroPartido() <= max)) // 🔥 FILTRO DE RANGO DE PARTIDOS
                .peek(partido -> {
                    // Mapeo Local
                    Optional<Equipo> elLocal = todosLosEquipos.stream()
                            .filter(e -> e.getPosicionGrupo() != null && e.getPosicionGrupo().equalsIgnoreCase(partido.getCodigoLocal()))
                            .findFirst();

                    if (elLocal.isPresent()) {
                        partido.setNombreLocal(elLocal.get().getNombre());
                        partido.setBanderaLocal(elLocal.get().getBanderaEmoji());
                        partido.setGrupo(elLocal.get().getGrupo().replace("GRUPO ", "")); 
                    } else {
                        partido.setNombreLocal(partido.getCodigoLocal());
                        partido.setBanderaLocal("🏳️");
                    }

                    // Mapeo Visitante
                    Optional<Equipo> elVisitante = todosLosEquipos.stream()
                            .filter(e -> e.getPosicionGrupo() != null && e.getPosicionGrupo().equalsIgnoreCase(partido.getCodigoVisitante()))
                            .findFirst();

                    if (elVisitante.isPresent()) {
                        partido.setNombreVisitante(elVisitante.get().getNombre());
                        partido.setBanderaVisitante(elVisitante.get().getBanderaEmoji());
                    } else {
                        partido.setNombreVisitante(partido.getCodigoVisitante());
                        partido.setBanderaVisitante("🏳️");
                    }
                })
                .sorted((p1, p2) -> Integer.compare(p1.getNumeroPartido(), p2.getNumeroPartido())) 
                .toList();

            gridCalendario.setItems(partidosFiltrados);
    }
}
