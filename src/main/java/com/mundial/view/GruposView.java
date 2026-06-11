package com.mundial.view;

import com.mundial.entity.Equipo;
import com.mundial.repository.EquipoRepository;
import com.mundial.service.InterfazService; // 1. Agregamos el import de tu nuevo servicio
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.Arrays;
import java.util.List;

@Route("grupos")
public class GruposView extends VerticalLayout {

    // 2. Declaramos ambas variables de clase aquí arriba
    private final EquipoRepository equipoRepository;
    private final InterfazService interfazService;

    // 3. El constructor ahora recibe y mapea correctamente ambos componentes
    public GruposView(EquipoRepository equipoRepository, InterfazService interfazService) {
        this.equipoRepository = equipoRepository;
        this.interfazService = interfazService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Encabezado principal
        H2 titulo = new H2("📊 Tablas de Posiciones - Mundial 2026 📊");
        titulo.getStyle().set("text-align", "center");
        titulo.getStyle().set("margin-bottom", "20px");
        add(titulo);

        // 🔥 LA MAGIA CENTRALIZADA: Llamamos al parche de banderas con una sola línea limpia
        // this.interfazService.aplicarParcheBanderas(this);
        
        // 🔥 LA CLAVE: Usamos FlexLayout en lugar de VerticalLayout para el contenedor
        FlexLayout contenedorFlex = new FlexLayout();
        contenedorFlex.setWidthFull();
        
        // Permitir que los elementos pasen a la siguiente fila si ya no caben
        contenedorFlex.setFlexWrap(FlexLayout.FlexWrap.WRAP); 
        
        // Espaciado elegante entre las tarjetas de los grupos
        contenedorFlex.getStyle().set("gap", "20px"); 

        // Lista oficial de los 12 grupos del Mundial 2026 (De la A a la L)
        List<String> grupos = Arrays.asList(
            "Grupo A", "Grupo B", "Grupo C", "Grupo D",
            "Grupo E", "Grupo F", "Grupo G", "Grupo H",
            "Grupo I", "Grupo J", "Grupo K", "Grupo L"
        );

        // ⚙️ CONFIGURACIÓN DE DISTRIBUCIÓN (Elige tu favorita):
        // Para 4x3 (4 columnas): Usa "calc(25% - 15px)"
        // Para 3x4 (3 columnas): Usa "calc(33.33% - 14px)"
        // Para 2x6 (2 columnas): Usa "calc(50% - 10px)"
        String anchoColumna = "calc(33.33% - 14px)"; 

        for (String grupo : grupos) {
            VerticalLayout tarjetaGrupo = crearTarjetaParaGrupo(grupo);
            
            // Le asignamos el ancho dinámico a cada grupo
            tarjetaGrupo.getStyle().set("width", anchoColumna);
            tarjetaGrupo.getStyle().set("min-width", "280px"); // Seguridad para pantallas móviles
            
            contenedorFlex.add(tarjetaGrupo);
        }

        add(contenedorFlex);
    }

    /**
     * Genera un bloque visual independiente (Tarjeta) para un grupo
     */
    private VerticalLayout crearTarjetaParaGrupo(String nombreGrupo) {
        VerticalLayout tarjeta = new VerticalLayout();
        tarjeta.setPadding(true);
        tarjeta.setSpacing(true);
        
        // Estilo de tarjeta moderna (Borde sutil y fondo limpio)
        tarjeta.getStyle().set("border", "1px solid #e2e8f0");
        tarjeta.getStyle().set("border-radius", "8px");
        tarjeta.getStyle().set("background-color", "#f8fafc");
        tarjeta.getStyle().set("box-shadow", "0 1px 3px rgba(0,0,0,0.05)");

        // Subtítulo del Grupo
        H3 subtitulo = new H3(nombreGrupo);
        subtitulo.getStyle().set("color", "#1676f3");
        subtitulo.getStyle().set("margin", "0");

        // Configuración de la Tabla (Grid)
        Grid<Equipo> grid = new Grid<>(Equipo.class, false);
        grid.setAllRowsVisible(true); // Evita scrolls internos feos

        // Columnas compactas para que quepan bien en la cuadrícula
     // Cambiamos la columna simple por un renderizador de componentes HTML
     // Cambiamos la columna para renderizar una Imagen + Texto
     // 🔥 LLAMADA MAESTRA: El servicio se encarga de fabricar la bandera e incrustarla
        grid.addComponentColumn(equipo -> 
            interfazService.crearCeldaConBandera(equipo.getBanderaEmoji(), equipo.getNombre())
        ).setHeader("País").setAutoWidth(true);
        
        
        
        grid.addColumn(Equipo::getPuntos).setHeader("PTS").setWidth("50px").setFlexGrow(0);
        grid.addColumn(Equipo::getGolesAFavor).setHeader("GF").setWidth("45px").setFlexGrow(0);
        grid.addColumn(Equipo::getGolesEnContra).setHeader("GC").setWidth("45px").setFlexGrow(0);
        grid.addColumn(e -> (e.getGolesAFavor() - e.getGolesEnContra())).setHeader("DG").setWidth("45px").setFlexGrow(0);

        // Cargar datos de SQLite ordenados
        List<Equipo> equiposDelGrupo = equipoRepository.obtainPosicionesPorGrupo(nombreGrupo);
        grid.setItems(equiposDelGrupo);

        tarjeta.add(subtitulo, grid);
        return tarjeta;
    }
 
}