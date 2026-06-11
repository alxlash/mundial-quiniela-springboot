package com.mundial.repository;

import com.mundial.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    
    // 🔥 Busca al equipo por su posición asignada (ej: "A1")
    Optional<Equipo> findByPosicionGrupo(String posicionGrupo);
    List<Equipo> obtainPosicionesPorGrupo(String grupo);
}