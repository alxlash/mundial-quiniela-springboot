package com.mundial.view;

import com.mundial.entity.Equipo;
import com.mundial.repository.EquipoRepository;
import com.mundial.service.InterfazService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Arrays;
import java.util.List;

// 🎯 Conectamos esta vista al menú superior horizontal fijo
@Route(value = "grupos", layout = MainLayout.class)
public class GruposView extends VerticalLayout implements BeforeEnterObserver {

    private final EquipoRepository equipoRepository;
    private final InterfazService interfazService;

    private FlexLayout contenedorDeGrupos;

    public GruposView(EquipoRepository equipoRepository, InterfazService interfazService) {
        this.equipoRepository = equipoRepository;
        this.interfazService = interfazService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // 1. Encabezado de la Sección
        H2 titulo = new H2("📊 Tablas de Posiciones Oficiales - Mundial 2026 📊");
        titulo.getStyle().set("text-align", "center").set("margin-bottom", "10px");
        add(titulo);

        // 2. Contenedor Flexible Multicolumna (FlexLayout)
        // Permite que las tablas de los grupos se acomoden solas de lado a lado según el tamaño de pantalla
        contenedorDeGrupos = new FlexLayout();
        contenedorDeGrupos.setWidthFull();
        contenedorDeGrupos.getStyle()
                .set("flex-wrap", "wrap") // Si no caben, bajan al siguiente renglón
                .set("gap", "25px")       // Separación uniforme entre tarjetas de grupos
                .set("justify-content", "center");

        add(contenedorDeGrupos);
    }

    /**
     * Evento que se ejecuta antes de renderizar la página: valida sesión y dibuja los grupos
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Long usuarioId = (Long) VaadinSession.getCurrent().getAttribute("USUARIO_ID");
        if (usuarioId == null) {
            event.rerouteTo(""); // Si no está logueado, directo al Login
            return;
        }

        // Limpiamos el contenedor para refrescar y no duplicar los datos si el usuario reingresa
        contenedorDeGrupos.removeAll();

        // 3. 🔥 FORMATO MUNDIAL 2026: Actualizado de 8 a 12 grupos (Del Grupo A al Grupo L)
        List<String> gruposMundial = Arrays.asList(
            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"
        );

        for (String letraGrupo : gruposMundial) {
            VerticalLayout tarjetaGrupo = crearTarjetaDeGrupo("GRUPO " + letraGrupo);
            contenedorDeGrupos.add(tarjetaGrupo);
        }
    }

    /**
     * Método de soporte para fabricar la tarjeta individual de cada grupo con su tabla interna
     */
    private VerticalLayout crearTarjetaDeGrupo(String nombreGrupo) {
        // Contenedor vertical que funcionará como una "tarjeta" visual para el grupo
        VerticalLayout tarjeta = new VerticalLayout();
        tarjeta.setWidth("440px"); // Ancho ideal para que quepan dos columnas en monitores estándar
        tarjeta.getStyle()
                .set("background-color", "var(--lumo-base-color)")
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "16px")
                .set("padding", "15px")
                .set("box-shadow", "0 4px 6px -1px rgba(0,0,0,0.05)");

        // Subtítulo del grupo
        H3 subtitulo = new H3(nombreGrupo);
        subtitulo.getStyle().set("color", "var(--lumo-primary-text-color)").set("margin", "0 0 10px 0");
        tarjeta.add(subtitulo);

        // 4. Configuración del mini Grid para los 4 equipos del grupo
        Grid<Equipo> grid = new Grid<>(Equipo.class, false);
        grid.setAllRowsVisible(true); // Desactiva los scrolls internos del grid para que se expanda limpio

        // Columna País: Utiliza tu servicio para jalar imagen de bandera + texto
        grid.addComponentColumn(equipo -> 
            interfazService.crearCeldaConBandera(equipo.getBanderaEmoji(), equipo.getNombre())
        ).setHeader("País").setAutoWidth(true).setFlexGrow(1);

        // Columnas compactas de estadísticas de juego
        grid.addColumn(Equipo::getPuntos).setHeader("PTS").setWidth("55px").setFlexGrow(0);
        grid.addColumn(Equipo::getGolesAFavor).setHeader("GF").setWidth("45px").setFlexGrow(0);
        grid.addColumn(Equipo::getGolesEnContra).setHeader("GC").setWidth("45px").setFlexGrow(0);
        
        // Columna calculada: Diferencia de Goles (DG = GF - GC)
        grid.addColumn(e -> (e.getGolesAFavor() - e.getGolesEnContra()))
                .setHeader("DG")
                .setWidth("45px")
                .setFlexGrow(0);

        // 5. Cargar y ordenar los equipos de este grupo específico desde SQLite
        // Filtramos por el nombre del grupo y ordenamos por Puntos (Descendente), y luego Diferencia de Goles
        List<Equipo> equiposFiltrados = equipoRepository.findAll().stream()
                .filter(e -> nombreGrupo.equalsIgnoreCase(e.getGrupo()))
                .sorted((e1, e2) -> {
                    int comparePuntos = Integer.compare(e2.getPuntos(), e1.getPuntos());
                    if (comparePuntos != 0) return comparePuntos;
                    
                    int dg1 = e1.getGolesAFavor() - e1.getGolesEnContra();
                    int dg2 = e2.getGolesAFavor() - e2.getGolesEnContra();
                    return Integer.compare(dg2, dg1);
                })
                .toList();

        grid.setItems(equiposFiltrados);
        tarjeta.add(grid);

        return tarjeta;
    }
}