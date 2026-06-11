package com.mundial.repository;

import com.mundial.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar a un amigo por su nombre (para cuando inicie sesión en la quiniela)
    Optional<Usuario> findByNombre(String nombre);

    // Tabla de posiciones de la quiniela: usuarios con mas puntos primero
    @Query("SELECT u FROM Usuario u ORDER BY u.puntosQuiniela DESC")
    List<Usuario> obtenerRankingQuiniela();
}