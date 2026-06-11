package com.mundial.repository;

import com.mundial.entity.Pronostico;
import com.mundial.entity.Usuario;
import com.mundial.entity.Partido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PronosticoRepository extends JpaRepository<Pronostico, Long> {
    
    // Buscar todos los pronósticos que ha hecho un usuario específico
    List<Pronostico> findByUsuario(Usuario usuario);

    // Buscar si un usuario ya tiene un pronóstico guardado para un partido concreto (para no duplicar)
    Optional<Pronostico> findByUsuarioAndPartido(Usuario usuario, Partido partido);
    
    // Buscar todos los pronósticos de un partido (útil cuando el admin registra el resultado real y toca recalcular los puntos de todos)
    List<Pronostico> findByPartido(Partido partido);
}