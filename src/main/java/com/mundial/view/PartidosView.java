package com.mundial.view;

import com.mundial.entity.*;
import com.mundial.repository.*;
import com.mundial.service.InterfazService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// 🎯 Conectamos esta vista al menú común agregando: layout = MainLayout.class
@Route(value = "partidos", layout = MainLayout.class)
public class PartidosView extends VerticalLayout {

    private final PartidoRepository partidoRepository;
    private final PronosticoRepository pronosticoRepository;
    private final InterfazService interfazService;
    private final EquipoRepository equipoRepository;
    private final UsuarioRepository usuarioRepository;
    
    private Usuario usuarioLogueado;
    private Grid<Partido> gridPartidos;
    private List<Partido> todosLosPartidos; // Cache local para filtrar rápido

    public PartidosView(PartidoRepository partidoRepository, PronosticoRepository pronosticoRepository, 
                        InterfazService interfazService, EquipoRepository equipoRepository,
                        UsuarioRepository usuarioRepository) {
        this.partidoRepository = partidoRepository;
        this.pronosticoRepository = pronosticoRepository;
        this.interfazService = interfazService;
        this.equipoRepository = equipoRepository;
        this.usuarioRepository = usuarioRepository;

        // 1. Validar la sesión
        Long usuarioId = (Long) VaadinSession.getCurrent().getAttribute("USUARIO_ID");
        if (usuarioId == null) {
            add(new H2("No has iniciado sesión. Por favor ve a la página principal."));
            return;
        }
        
        usuarioLogueado = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuarioLogueado == null) {
            add(new H2("Error al cargar los datos del usuario."));
            return;
        }

        setSizeFull();
        setPadding(true);

        // Encabezado secundario integrado
        H4 puntosActuales = new H4("Tus Puntos de Quiniela: " + usuarioLogueado.getPuntosQuiniela() + " pts");
        puntosActuales.getStyle().set("color", "#16a34a");

        // 🔥 2. SELECCIÓN POR FASES (TABS AMIGABLES)
        Tab tabFase1 = new Tab("Fase 1 (Partidos 1-24)");
        Tab tabFase2 = new Tab("Fase 2 (Partidos 25-48)");
        Tab tabFase3 = new Tab("Fase 3 (Partidos 49-72)");
        Tabs selectorFases = new Tabs(tabFase1, tabFase2, tabFase3);
        selectorFases.setWidthFull();

        // 3. CONFIGURACIÓN DE LA TABLA
        gridPartidos = new Grid<>(Partido.class, false);
        gridPartidos.setSizeFull();

        gridPartidos.addColumn(Partido::getNumeroPartido).setHeader("#").setWidth("60px").setFlexGrow(0);

        gridPartidos.addComponentColumn(pio -> {
            Equipo equipoLocal = equipoRepository.findByPosicionGrupo(pio.getCodigoLocal()).orElse(null);
            if (equipoLocal != null) {
                return interfazService.crearCeldaConBandera(equipoLocal.getBanderaEmoji(), equipoLocal.getNombre());
            } else {
                return new Span(pio.getCodigoLocal());
            }
        }).setHeader("Equipo Local").setAutoWidth(true);

        // Selector L - E - V
        gridPartidos.addComponentColumn(pio -> {
            RadioButtonGroup<String> rbFila = new RadioButtonGroup<>();
            rbFila.setItems("L", "E", "V");
            rbFila.getElement().setAttribute("theme", "horizontal small");
            if (pio.isJugado()) rbFila.setEnabled(false);

            Optional<Pronostico> existente = pronosticoRepository.findByUsuarioAndPartido(usuarioLogueado, pio);
            if (existente.isPresent()) {
                String tend = existente.get().getTendenciaElegida();
                if ("LOCAL".equals(tend)) rbFila.setValue("L");
                else if ("EMPATE".equals(tend)) rbFila.setValue("E");
                else if ("VISITANTE".equals(tend)) rbFila.setValue("V");
            }

            rbFila.addValueChangeListener(e -> {
                if (e.isFromClient() && e.getValue() != null) {
                    Pronostico pronostico = pronosticoRepository.findByUsuarioAndPartido(usuarioLogueado, pio).orElse(new Pronostico());
                    pronostico.setUsuario(usuarioLogueado);
                    pronostico.setPartido(pio);
                    
                    if ("L".equals(e.getValue())) pronostico.setTendenciaElegida("LOCAL");
                    else if ("E".equals(e.getValue())) pronostico.setTendenciaElegida("EMPATE");
                    else if ("V".equals(e.getValue())) pronostico.setTendenciaElegida("VISITANTE");
                    
                    pronosticoRepository.save(pronostico);
                    Notification.show("⚽ Guardado", 1000, Notification.Position.BOTTOM_END).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                }
            });
            return rbFila;
        }).setHeader("Pronóstico").setWidth("180px").setFlexGrow(0);

        gridPartidos.addComponentColumn(pio -> {
            Equipo equipoVisita = equipoRepository.findByPosicionGrupo(pio.getCodigoVisitante()).orElse(null);
            if (equipoVisita != null) {
                return interfazService.crearCeldaConBandera(equipoVisita.getBanderaEmoji(), equipoVisita.getNombre());
            } else {
                return new Span(pio.getCodigoVisitante());
            }
        }).setHeader("Equipo Visitante").setAutoWidth(true);

        gridPartidos.addColumn(pio -> "11:00").setHeader("Horario").setWidth("90px").setFlexGrow(0);
        gridPartidos.addColumn(pio -> "Estadio Mundial").setHeader("Estadio").setAutoWidth(true);

        // Caja de comentarios
        gridPartidos.addComponentColumn(pio -> {
            TextField txtCompComentario = new TextField();
            txtCompComentario.setPlaceholder("Sin comentario");
            txtCompComentario.setWidth("180px");
            if (pio.isJugado()) txtCompComentario.setEnabled(false);

            pronosticoRepository.findByUsuarioAndPartido(usuarioLogueado, pio).ifPresent(pr -> {
                txtCompComentario.setValue(pr.getComentario() != null ? pr.getComentario() : "");
            });

            txtCompComentario.addBlurListener(e -> {
                Pronostico pronostico = pronosticoRepository.findByUsuarioAndPartido(usuarioLogueado, pio).orElse(new Pronostico());
                pronostico.setUsuario(usuarioLogueado);
                pronostico.setPartido(pio);
                pronostico.setComentario(txtCompComentario.getValue().trim());
                pronosticoRepository.save(pronostico);
            });
            return txtCompComentario;
        }).setHeader("Comentario").setAutoWidth(true);

        // 4. LÓGICA DE FILTRADO DINÁMICO POR PESTAÑA
        todosLosPartidos = partidoRepository.findAll();

        selectorFases.addSelectedChangeListener(event -> {
            Tab pestañaSeleccionada = event.getSelectedTab();
            if (pestañaSeleccionada == tabFase1) {
                filtrarPartidos(1, 24);
            } else if (pestañaSeleccionada == tabFase2) {
                filtrarPartidos(25, 48);
            } else if (pestañaSeleccionada == tabFase3) {
                filtrarPartidos(49, 72);
            }
        });

        // Carga inicial: Mostrar por defecto la Fase 1
        filtrarPartidos(1, 24);

        add(puntosActuales, selectorFases, gridPartidos);
    }

    private void filtrarPartidos(int inicio, int fin) {
        List<Partido> filtrados = todosLosPartidos.stream()
                .filter(p -> p.getNumeroPartido() >= inicio && p.getNumeroPartido() <= fin)
                .collect(Collectors.toList());
        gridPartidos.setItems(filtrados);
    }
}