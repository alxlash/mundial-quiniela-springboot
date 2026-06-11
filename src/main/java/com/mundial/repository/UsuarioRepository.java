package com.mundial.repository;

import com.mundial.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> { 

    // Tabla de posiciones de la quiniela: usuarios con mas puntos primero
    @Query("SELECT u FROM Usuario u ORDER BY u.puntosQuiniela DESC")
    List<Usuario> obtenerRankingQuiniela();
    
 // Para verificar si el apodo ya existe al registrarse
    boolean existsByNombre(String nombre);
    
    // Para buscar al usuario y validar su contraseña al iniciar sesión
    Optional<Usuario> findByNombre(String nombre);
    
}