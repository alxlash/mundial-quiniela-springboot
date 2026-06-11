package com.mundial.repository;

import com.mundial.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface EquipoRepository extends JpaRepository<Equipo, Long> {
    
    // Este funciona en automático porque sigue las reglas de nomenclatura en inglés (findBy...)
    Optional<Equipo> findByPosicionGrupo(String posicionGrupo);

    // 🔥 SOLUCIÓN: Le indicamos explícitamente a JPA cómo ejecutar tu método personalizado
    @Query("SELECT e FROM Equipo e WHERE e.grupo = :grupo")
    List<Equipo> obtainPosicionesPorGrupo(@Param("grupo") String grupo);
}