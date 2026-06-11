package com.mundial.repository;

import com.mundial.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {
    
    // Obtener partidos filtrando por si ya se jugaron o no
    List<Partido> findByJugado(boolean jugado);
    
    // 🔥 CORREGIDO: Sintaxis exacta de Spring Data para ordenar por Fecha y luego por Hora
    List<Partido> findAllByOrderByFechaAscHoraAsc();
}