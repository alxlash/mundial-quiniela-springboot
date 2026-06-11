package com.mundial.view;

import com.mundial.entity.*;
import com.mundial.repository.*;
import com.mundial.service.InterfazService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import java.util.Optional;

// Definimos la ruta de esta pantalla: http://localhost:8080/partidos
@Route("partidos")
public class PartidosView extends VerticalLayout {

    private final PartidoRepository partidoRepository;
    private final PronosticoRepository pronosticoRepository;
    private final InterfazService interfazService;
    private final EquipoRepository equipoRepository;
    private Usuario usuarioLogueado;

    // Campos del formulario flotante para apostar
    private IntegerField txtGolesLocal = new IntegerField("Goles Local");
    private IntegerField txtGolesVisita = new IntegerField("Goles Visitante");
    private Button btnGuardarPronostico = new Button("Guardar Pronóstico");
    private Partido partidoSeleccionado;
    private H4 lblInfoPartido = new H4("Selecciona un partido de la lista para apostar");

    // 🎯 EL CONSTRUCTOR: Recibe repositorios y servicios de forma correcta
    public PartidosView(PartidoRepository partidoRepository, PronosticoRepository pronosticoRepository, 
                        InterfazService interfazService, EquipoRepository equipoRepository) {
        this.partidoRepository = partidoRepository;
        this.pronosticoRepository = pronosticoRepository;
        this.interfazService = interfazService;
        this.equipoRepository = equipoRepository;

        // 1. Validar si el usuario pasó por el Login
        usuarioLogueado = VaadinSession.getCurrent().getAttribute(Usuario.class);
        if (usuarioLogueado == null) {
            add(new H2("No has iniciado sesión. Por favor ve a la página principal."));
            return;
        }

        setSizeFull();
        setPadding(true);

        // Encabezado con los datos del usuario
        H2 titulo = new H2("⚽ Partidos del Mundial - Quiniela de " + usuarioLogueado.getNombre() + " ⚽");
        H4 puntosActuales = new H4("Tus Puntos Totales: " + usuarioLogueado.getPuntosQuiniela() + " pts");
        puntosActuales.getStyle().set("color", "green");

        // 2. Configurar la Tabla (Grid) de Partidos
        Grid<Partido> gridPartidos = new Grid<>(Partido.class, false);

        // COLUMNA LOCAL: Busca el código ("A1", "A2") en la tabla de equipos
        gridPartidos.addComponentColumn(pio -> {
            Equipo equipoLocal = equipoRepository.findByPosicionGrupo(pio.getCodigoLocal()).orElse(null);
            if (equipoLocal != null) {
                return interfazService.crearCeldaConBandera(equipoLocal.getBanderaEmoji(), equipoLocal.getNombre());
            } else {
                return new com.vaadin.flow.component.html.Span(pio.getCodigoLocal());
            }
        }).setHeader("Equipo Local").setAutoWidth(true);

        gridPartidos.addColumn(Partido::getGolesLocal).setHeader("Goles L");
        gridPartidos.addColumn(Partido::getGolesVisitante).setHeader("Goles V");

        // COLUMNA VISITANTE: Hace exactamente lo mismo para el visitante
        gridPartidos.addComponentColumn(pio -> {
            Equipo equipoVisita = equipoRepository.findByPosicionGrupo(pio.getCodigoVisitante()).orElse(null);
            if (equipoVisita != null) {
                return interfazService.crearCeldaConBandera(equipoVisita.getBanderaEmoji(), equipoVisita.getNombre());
            } else {
                return new com.vaadin.flow.component.html.Span(pio.getCodigoVisitante());
            }
        }).setHeader("Equipo Visitante").setAutoWidth(true);

        gridPartidos.addColumn(pio -> pio.isJugado() ? "Finalizado" : "Pendiente").setHeader("Estado");

        // Llenar la tabla con los partidos reales de la base de datos
        gridPartidos.setItems(partidoRepository.findAll());
        
        // 3. Configurar el Formulario de apuestas (abajo de la tabla)
        txtGolesLocal.setMin(0);
        txtGolesVisita.setMin(0);
        txtGolesLocal.setWidth("120px");
        txtGolesVisita.setWidth("120px");
        btnGuardarPronostico.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnGuardarPronostico.setEnabled(false);

        HorizontalLayout layoutFormulario = new HorizontalLayout(txtGolesLocal, txtGolesVisita, btnGuardarPronostico);
        layoutFormulario.setAlignItems(Alignment.AUTO);

        // Al hacer clic en una fila de la tabla, cargar ese partido en el formulario
        gridPartidos.asSingleSelect().addValueChangeListener(event -> {
            partidoSeleccionado = event.getValue();
            if (partidoSeleccionado != null) {
                if (partidoSeleccionado.isJugado()) {
                    lblInfoPartido.setText("Este partido ya se jugó. No puedes modificar tu apuesta.");
                    btnGuardarPronostico.setEnabled(false);
                } else {
                    // 🔥 SOLUCIÓN AQUÍ: Buscamos los nombres de manera dinámica en el repositorio para el formulario
                    Equipo local = equipoRepository.findByPosicionGrupo(partidoSeleccionado.getCodigoLocal()).orElse(null);
                    Equipo visita = equipoRepository.findByPosicionGrupo(partidoSeleccionado.getCodigoVisitante()).orElse(null);
                    
                    String nombreLocal = (local != null) ? local.getNombre() : partidoSeleccionado.getCodigoLocal();
                    String nombreVisita = (visita != null) ? visita.getNombre() : partidoSeleccionado.getCodigoVisitante();

                    lblInfoPartido.setText("Apostar para: " + nombreLocal + " vs " + nombreVisita);
                    btnGuardarPronostico.setEnabled(true);
                    
                    // Buscar si el usuario ya había hecho una apuesta previa para rellenar los campos
                    Optional<Pronostico> existente = pronosticoRepository.findByUsuarioAndPartido(usuarioLogueado, partidoSeleccionado);
                    if (existente.isPresent()) {
                        txtGolesLocal.setValue(existente.get().getGolesLocal());
                        txtGolesVisita.setValue(existente.get().getGolesVisitante());
                    } else {
                        txtGolesLocal.clear();
                        txtGolesVisita.clear();
                    }
                }
            }
        });

        // Lógica para guardar la apuesta en la base de datos SQLite
        btnGuardarPronostico.addClickListener(event -> {
            if (partidoSeleccionado != null && txtGolesLocal.getValue() != null && txtGolesVisita.getValue() != null) {
                
                // Buscar si es actualización o apuesta nueva
                Pronostico pronostico = pronosticoRepository.findByUsuarioAndPartido(usuarioLogueado, partidoSeleccionado)
                        .orElse(new Pronostico(usuarioLogueado, partidoSeleccionado, 0, 0));

                pronostico.setGolesLocal(txtGolesLocal.getValue());
                pronostico.setGolesVisitante(txtGolesVisita.getValue());
                pronosticoRepository.save(pronostico);

                Notification.show("¡Pronóstico guardado con éxito!", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
        });

        // Agregar todo al contenedor de la pantalla
        add(titulo, puntosActuales, gridPartidos, lblInfoPartido, layoutFormulario);
    }
}