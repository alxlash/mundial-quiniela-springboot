package com.mundial.service;

import com.mundial.entity.*;
import com.mundial.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class QuinielaService {

    private final PartidoRepository partidoRepository;
    private final PronosticoRepository pronosticoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipoRepository equipoRepository;

    public QuinielaService(PartidoRepository partidoRepository, PronosticoRepository pronosticoRepository, 
                           UsuarioRepository usuarioRepository, EquipoRepository equipoRepository) {
        this.partidoRepository = partidoRepository;
        this.pronosticoRepository = pronosticoRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
    }

    /**
     * 🔥 NUEVO MÉTODO: Guarda o actualiza la tendencia ("LOCAL", "EMPATE", "VISITANTE")
     * junto con un comentario opcional para tirar barrio con los amigos.
     */
    @Transactional
    public void guardarOActualizarPronostico(Long usuarioId, int numeroPartido, String tendencia, String comentario) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId));
                
        Partido partido = partidoRepository.findById((long) numeroPartido)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado con No: " + numeroPartido));

        // Buscamos si ya existía un pronóstico previo para este partido y usuario
        List<Pronostico> pronosticosExistentes = pronosticoRepository.findByPartido(partido);
        Pronostico pronostico = pronosticosExistentes.stream()
                .filter(p -> p.getUsuario().getId().equals(usuarioId))
                .findFirst()
                .orElse(new Pronostico());

        // Si es nuevo, enlazamos las relaciones
        if (pronostico.getId() == null) {
            pronostico.setUsuario(usuario);
            pronostico.setPartido(partido);
        }

        // Asignamos los nuevos valores simplificados
        pronostico.setTendenciaElegida(tendencia); // "LOCAL", "EMPATE" o "VISITANTE"
        pronostico.setComentario(comentario);
        pronostico.setPuntosObtenidos(0); // Por defecto en 0 hasta que se juegue el partido

        pronosticoRepository.save(pronostico);
    }

    /**
     * 🔥 ALGORITMO REY ACTUALIZADO:
     * Compara las tendencias de texto de forma directa. ¡1 punto o 0 puntos!
     */
    @Transactional
    public void registrarResultadoReal(int partidoId, int golesLocalReal, int golesVisitaReal) {
        Partido partido = partidoRepository.findById((long) partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        // 1. Actualizar el partido con el marcador real en el panel de admin
        partido.setGolesLocal(golesLocalReal);
        partido.setGolesVisitante(golesVisitaReal);
        partido.setJugado(true);
        partidoRepository.save(partido);

        // 2. 🔥 Traducimos los goles reales a un String de tendencia oficial
        String tendenciaReal = calcularTendenciaString(golesLocalReal, golesVisitaReal);

        // 3. Evaluar los pronósticos de todos tus amigos
        List<Pronostico> pronosticos = pronosticoRepository.findByPartido(partido);

        for (Pronostico p : pronosticos) {
            int puntosGanados = 0;

            // 🔥 REGLA DE ORO: Comparación directa de cadenas de texto
            if (p.getTendenciaElegida() != null && p.getTendenciaElegida().equals(tendenciaReal)) {
                puntosGanados = 1; // Acierta ➔ 1 punto
            } // Falla ➔ se queda con 0 puntos

            p.setPuntosObtenidos(puntosGanados);
            pronosticoRepository.save(p);

            // Sumamos el punto a la bolsa acumulada de tu amigo
            Usuario usuario = p.getUsuario();
            recalcularPuntosTotalesUsuario(usuario);
        }
        
        // 4. Actualizar estadísticas reales del Mundial en la tabla de Equipos
        Equipo equipoLocal = equipoRepository.findByPosicionGrupo(partido.getCodigoLocal()).orElse(null);
        Equipo equipoVisitante = equipoRepository.findByPosicionGrupo(partido.getCodigoVisitante()).orElse(null);

        if (equipoLocal != null) {
            actualizarEstadisticasEquipos(equipoLocal, golesLocalReal, golesVisitaReal);
        }
        if (equipoVisitante != null) {
            actualizarEstadisticasEquipos(equipoVisitante, golesVisitaReal, golesLocalReal);
        }
    }

    /**
     * 🔥 Método Auxiliar para traducir marcadores a etiquetas fijas
     */
    private String calcularTendenciaString(int golesLocal, int golesVisitante) {
        if (golesLocal > golesVisitante) return "LOCAL";
        if (golesVisitante > golesLocal) return "VISITANTE";
        return "EMPATE";
    }

    private void recalcularPuntosTotalesUsuario(Usuario usuario) {
        List<Pronostico> todosSusPronosticos = pronosticoRepository.findByUsuario(usuario);
        int sumaPuntos = todosSusPronosticos.stream().mapToInt(Pronostico::getPuntosObtenidos).sum();
        usuario.setPuntosQuiniela(sumaPuntos);
        usuarioRepository.save(usuario);
    }

    private void actualizarEstadisticasEquipos(Equipo equipo, int golesAFavor, int golesEnContra) {
        equipo.setPartidosJugados(equipo.getPartidosJugados() + 1);
        equipo.setGolesAFavor(equipo.getGolesAFavor() + golesAFavor);
        equipo.setGolesEnContra(equipo.getGolesEnContra() + golesEnContra);
        
        if (golesAFavor > golesEnContra) {
            equipo.setPuntos(equipo.getPuntos() + 3);
        } else if (golesAFavor == golesEnContra) {
            equipo.setPuntos(equipo.getPuntos() + 1);
        }
        equipoRepository.save(equipo);
    }

    // Método para recuperar la tendencia guardada por el usuario (Para pintar la vista)
    public String obtenerTendenciaPronosticada(Long usuarioId, int numeroPartido) {
        return pronosticoRepository.findByPartido(partidoRepository.findById((long) numeroPartido).orElse(null))
                .stream()
                .filter(p -> p.getUsuario().getId().equals(usuarioId))
                .findFirst()
                .map(Pronostico::getTendenciaElegida)
                .orElse(null);
    }

    // Método para recuperar el comentario guardado por el usuario (Para pintar la vista)
    public String obtenerComentarioPronosticado(Long usuarioId, int numeroPartido) {
        return pronosticoRepository.findByPartido(partidoRepository.findById((long) numeroPartido).orElse(null))
                .stream()
                .filter(p -> p.getUsuario().getId().equals(usuarioId))
                .findFirst()
                .map(Pronostico::getComentario)
                .orElse(null);
    }
}