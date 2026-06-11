package com.mundial.repository;

import com.mundial.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    
    // Obtener partidos filtrando por si ya se jugaron o no (útil para la quiniela vs el panel de admin)
    List<Partido> findByJugado(boolean jugado);
    
    // Obtener el calendario ordenado por fecha cronológica
    List<Partido> findAllByOrderByFechaHoraAsc();
}