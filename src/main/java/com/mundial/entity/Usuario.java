package com.mundial.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String nombre; // Nombre o apodo de tu amigo
    
    private int puntosQuiniela = 0; // Se acumulan aquí

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public int getPuntosQuiniela() { return puntosQuiniela; }
    public void setPuntosQuiniela(int puntosQuiniela) { this.puntosQuiniela = puntosQuiniela; }
}