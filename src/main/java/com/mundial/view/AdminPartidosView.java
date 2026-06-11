package com.mundial.view;

import com.mundial.entity.Equipo;
import com.mundial.entity.Partido;
import com.mundial.repository.EquipoRepository;
import com.mundial.repository.PartidoRepository;
import com.mundial.service.InterfazService;
import com.mundial.service.QuinielaService; // 🔥 Tu motor de puntos
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.Route;
import java.util.List;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.server.VaadinSession;

@Route(value = "admin-partidos", layout = MainLayout.class)
public class AdminPartidosView extends VerticalLayout implements BeforeEnterObserver { // 🔥 Agregamos el Observer

    private final PartidoRepository partidoRepository;
    private final EquipoRepository equipoRepository;
    private final InterfazService interfazService;
    private final QuinielaService quinielaService;

    private Grid<Partido> gridPartidos;
    private Tab tabGrupos;
    private Tab tabEliminatoria;
    private Tabs tabsFiltro;

    public AdminPartidosView(PartidoRepository partidoRepository, EquipoRepository equipoRepository, 
                              InterfazService interfazService, QuinielaService quinielaService) {
        // ... Todo tu constructor se queda exactamente IGUAL a como lo tienes ...
        this.partidoRepository = partidoRepository;
        this.equipoRepository = equipoRepository;
        this.interfazService = interfazService;
        this.quinielaService = quinielaService;

        setSizeFull();
        setPadding(true);

        H2 titulo = new H2("⚙️ Panel de Control: Calendario General del Mundial ⚙️");
        
        // Un pequeño texto de ayuda para el usuario
        Span ayuda = new Span("💡 Tip: Haz doble clic sobre cualquier fila para registrar o modificar los goles reales.");
        ayuda.getStyle().set("color", "#64748b").set("font-style", "italic");

        add(titulo, ayuda);

        tabGrupos = new Tab("Fase de Grupos");
        tabEliminatoria = new Tab("Fase Eliminatoria (Calificatorios)");
        tabsFiltro = new Tabs(tabGrupos, tabEliminatoria);
        tabsFiltro.setWidthFull();
        add(tabsFiltro);

        configurarGrid();
        add(gridPartidos);

        tabsFiltro.addSelectedChangeListener(event -> actualizarTabla());
        actualizarTabla();
    }

    private void configurarGrid() {
        gridPartidos = new Grid<>(Partido.class, false);
        gridPartidos.setSizeFull();

        // Herramientas de edición en línea de Vaadin
        Editor<Partido> editor = gridPartidos.getEditor();
        Binder<Partido> binder = new Binder<>(Partido.class);
        editor.setBinder(binder);
        editor.setBuffered(true); // Requiere confirmación (botón guardar) para aplicar cambios

        // 1. Columna No. de Partido
        gridPartidos.addColumn(Partido::getNumeroPartido).setHeader("No.").setAutoWidth(true).setFlexGrow(0);

        // 2. Columna Local
        gridPartidos.addComponentColumn(partido -> resolverComponenteEquipo(partido.getCodigoLocal()))
                .setHeader("Equipo Local").setAutoWidth(true);

        // 3. 🔥 COLUMNA GOLES LOCAL (EDITABLE)
        IntegerField fieldGolesLocal = new IntegerField();
        fieldGolesLocal.setWidthFull();
        fieldGolesLocal.setMin(0);
        // Vinculamos el input con el atributo golesLocal de la Entidad
        binder.forField(fieldGolesLocal).bind(Partido::getGolesLocal, Partido::setGolesLocal);
        
        gridPartidos.addColumn(p -> p.getGolesLocal() != null ? p.getGolesLocal() : "-")
                .setHeader("L")
                .setEditorComponent(fieldGolesLocal) // Le dice al Grid qué componente mostrar al editar
                .setAutoWidth(true);

        // 4. 🔥 COLUMNA GOLES VISITANTE (EDITABLE)
        IntegerField fieldGolesVisitante = new IntegerField();
        fieldGolesVisitante.setWidthFull();
        fieldGolesVisitante.setMin(0);
        // Vinculamos el input con el atributo golesVisitante de la Entidad
        binder.forField(fieldGolesVisitante).bind(Partido::getGolesVisitante, Partido::setGolesVisitante);

        gridPartidos.addColumn(p -> p.getGolesVisitante() != null ? p.getGolesVisitante() : "-")
                .setHeader("V")
                .setEditorComponent(fieldGolesVisitante) // Le dice al Grid qué componente mostrar al editar
                .setAutoWidth(true);

        // 5. Columna Visitante
        gridPartidos.addComponentColumn(partido -> resolverComponenteEquipo(partido.getCodigoVisitante()))
                .setHeader("Equipo Visitante").setAutoWidth(true);

        // 6. Sede, Fecha y Estado
        gridPartidos.addColumn(p -> p.getFecha().toString() + " " + p.getHora().toString()).setHeader("Fecha y Hora").setAutoWidth(true);
        gridPartidos.addColumn(Partido::getEstadio).setHeader("Estadio").setAutoWidth(true);
        gridPartidos.addColumn(p -> p.isJugado() ? "✅ Finalizado" : "⏳ Pendiente").setHeader("Estado").setAutoWidth(true);

        // 7. 🔥 COLUMNA DE ACCIONES (Aparece sólo cuando la fila está en modo edición)
        Grid.Column<Partido> columnaAcciones = gridPartidos.addComponentColumn(partido -> {
            Button btnEditarEstatico = new Button(VaadinIcon.EDIT.create());
            btnEditarEstatico.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            // Si hacen clic al icono, también abre el editor
            btnEditarEstatico.addClickListener(e -> editor.editItem(partido));
            return btnEditarEstatico;
        }).setAutoWidth(true);

        // Configuración de los botones de Guardar/Cancelar dentro de la fila activa
        Button btnGuardar = new Button("Guardar", VaadinIcon.CHECK.create(), e -> editor.save());
        btnGuardar.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SUCCESS);

        Button btnCancelar = new Button(VaadinIcon.CLOSE.create(), e -> editor.cancel());
        btnCancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        HorizontalLayout accionesEditor = new HorizontalLayout(btnGuardar, btnCancelar);
        accionesEditor.setPadding(false);
        columnaAcciones.setEditorComponent(accionesEditor);

        // 🔥 EVENTO DE DOBLE CLIC: Abre el editor en la fila seleccionada
        gridPartidos.addItemDoubleClickListener(e -> {
            editor.editItem(e.getItem());
            Component editorComponent = e.getColumn().getEditorComponent();
            if (editorComponent instanceof IntegerField) {
                ((IntegerField) editorComponent).focus();
            }
        });

        // 🔥 PROCESO DE GUARDADO: Aquí ocurre la magia del negocio
        editor.addSaveListener(event -> {
            Partido partidoEditado = event.getItem();
            
            if (partidoEditado.getGolesLocal() == null || partidoEditado.getGolesVisitante() == null) {
                Notification.show("⚠️ Debes capturar ambos marcadores.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                // 1. Guardamos el partido en la DB marcándolo como JUGADO
                partidoEditado.setJugado(true);
                partidoRepository.save(partidoEditado);

                // 2. 🔥 DISPARAMOS TU MOTOR DE PUNTOS
                // Este método buscará todas las quinielas de tus amigos para este partido,
                // calculará sus aciertos y actualizará el ranking en tiempo real.
                quinielaService.registrarResultadoReal(
                        partidoEditado.getNumeroPartido(), 
                        partidoEditado.getGolesLocal(), 
                        partidoEditado.getGolesVisitante()
                );

                Notification.show("⚽ ¡Resultado guardado! Puntos de la quiniela recalculados.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

                // Refrescamos la tabla para ver el nuevo marcador pintado
                actualizarTabla();

            } catch (Exception ex) {
                Notification.show("❌ Error al procesar el resultado: " + ex.getMessage(), 5000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

    private void actualizarTabla() {
        Tab pestañaSeleccionada = tabsFiltro.getSelectedTab();
        if (pestañaSeleccionada == tabGrupos) {
            List<Partido> partidosGrupos = partidoRepository.findAll().stream()
                    .filter(p -> "GRUPOS".equals(p.getFase()))
                    .toList();
            gridPartidos.setItems(partidosGrupos);
        } else {
            List<Partido> partidosEliminatoria = partidoRepository.findAll().stream()
                    .filter(p -> "ELIMINATORIA".equals(p.getFase()))
                    .toList();
            gridPartidos.setItems(partidosEliminatoria);
        }
    }

    private Component resolverComponenteEquipo(String codigo) {
        Equipo equipo = equipoRepository.findByPosicionGrupo(codigo).orElse(null);
        if (equipo != null) {
            return interfazService.crearCeldaConBandera(equipo.getBanderaEmoji(), equipo.getNombre());
        } else {
            Span badgeCodigo = new Span(codigo);
            badgeCodigo.getStyle().set("background-color", "#eef2f7").set("color", "#475569")
                    .set("padding", "2px 8px").set("border-radius", "4px").set("font-weight", "bold");
            return badgeCodigo;
        }
    }
    
    /**
     * 🛡️ FILTRO DE SEGURIDAD PARA EL ADMINISTRADOR
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // 1. Verificamos si hay alguien logueado
        Object usuarioId = VaadinSession.getCurrent().getAttribute("USUARIO_ID");
        Object usuarioNombre = VaadinSession.getCurrent().getAttribute("USUARIO_NOMBRE");

        // 2. Si no hay sesión, o si el nombre de usuario NO es tu usuario admin, denegamos el acceso
        if (usuarioId == null || ! "admin".equalsIgnoreCase(String.valueOf(usuarioNombre))) {
            
            // 🚫 ¡Intruso detectado! Lo redirigimos de vuelta a la Bienvenida de usuarios
            event.rerouteTo("bienvenida");
        }
    }
}