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

    // El constructor une nuestro servicio con los planos (interfaces) que creamos antes
    public QuinielaService(PartidoRepository partidoRepository, PronosticoRepository pronosticoRepository, 
                           UsuarioRepository usuarioRepository, EquipoRepository equipoRepository) {
        this.partidoRepository = partidoRepository;
        this.pronosticoRepository = pronosticoRepository;
        this.usuarioRepository = usuarioRepository;
        this.equipoRepository = equipoRepository;
    }

    /**
     * Algoritmo Rey: Cuando el Administrador anota el resultado REAL de un partido,
     * este metodo calcula los puntos de TODOS los amigos automaticamente.
     */
    @Transactional
    public void registrarResultadoReal(int partidoId, int golesLocalReal, int golesVisitaReal) {
        // Buscamos el partido usando el ID tipo int
        Partido partido = partidoRepository.findById((long) partidoId)
                .orElseThrow(() -> new IllegalArgumentException("Partido no encontrado"));

        // 1. Actualizar el partido con el marcador real
        partido.setGolesLocal(golesLocalReal);
        partido.setGolesVisitante(golesVisitaReal);
        partido.setJugado(true);
        partidoRepository.save(partido);

        // 2. Buscar todos los pronosticos que hicieron tus amigos para este partido
        List<Pronostico> pronosticos = pronosticoRepository.findByPartido(partido);

        for (Pronostico p : pronosticos) {
            int puntosGanados = 0;

            // REGLA 1: ¡Le atino al marcador exacto! (Ej: Dijo 2-1 y quedo 2-1) -> 5 Puntos
            if (p.getGolesLocal() == golesLocalReal && p.getGolesVisitante() == golesVisitaReal) {
                puntosGanados = 5;
            } 
            // REGLA 2: No le atino al marcador, pero si al GANADOR o EMPATE -> 3 Puntos
            else if (obtenerResultado(p.getGolesLocal(), p.getGolesVisitante()) == obtenerResultado(golesLocalReal, golesVisitaReal)) {
                puntosGanados = 3;
            }
            // REGLA 3: No le atino a nada -> 0 Puntos

            p.setPuntosObtenidos(puntosGanados);
            pronosticoRepository.save(p);

            // Actualizar los puntos totales en el perfil de tu amigo
            Usuario usuario = p.getUsuario();
            recalcularPuntosTotalesUsuario(usuario);
        }
        
        // 3. 🔥 SOLUCIÓN: Buscamos dinámicamente los equipos reales usando sus códigos de posición
        Equipo equipoLocal = equipoRepository.findByPosicionGrupo(partido.getCodigoLocal()).orElse(null);
        Equipo equipoVisitante = equipoRepository.findByPosicionGrupo(partido.getCodigoVisitante()).orElse(null);

        // Si los equipos ya están definidos (fase de grupos o clasificados ya calculados), actualizamos sus estadísticas
        if (equipoLocal != null) {
            actualizarEstadisticasEquipos(equipoLocal, golesLocalReal, golesVisitaReal);
        }
        if (equipoVisitante != null) {
            actualizarEstadisticasEquipos(equipoVisitante, golesVisitaReal, golesLocalReal);
        }
    }

    private int obtenerResultado(int local, int visita) {
        if (local > visita) return 1;  // Gana Local
        if (visita > local) return -1; // Gana Visita
        return 0;                      // Empate
    }

    private void recalcularPuntosTotalesUsuario(Usuario usuario) {
        List<Pronostico> todosSusPronosticos = pronosticoRepository.findByUsuario(usuario);
        int sumaPuntos = todosSusPronosticos.stream().mapToInt(Pronostico::getPuntosObtenidos).sum();
        usuario.setPuntosQuiniela(sumaPuntos);
        usuarioRepository.save(usuario);
    }

    private void actualizarEstadisticasEquipos(Equipo equipo, int golesAFavor, int golesEnContra) {
        equipo.setGolesAFavor(equipo.getGolesAFavor() + golesAFavor);
        equipo.setGolesEnContra(equipo.getGolesEnContra() + golesEnContra);
        
        if (golesAFavor > golesEnContra) {
            equipo.setPuntos(equipo.getPuntos() + 3); // Gano
        } else if (golesAFavor == golesEnContra) {
            equipo.setPuntos(equipo.getPuntos() + 1); // Empato
        }
        equipoRepository.save(equipo);
    }
}