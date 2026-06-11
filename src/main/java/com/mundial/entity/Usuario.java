package com.mundial.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Marcamos el nombre como único para que sirva de credencial de logueo
    @Column(unique = true, nullable = false)
    private String nombre; 

    // 🔥 Nuevo campo para la contraseña (aquí se guardará el hash encriptado)
    @Column(nullable = false)
    private String password;

    private int puntosQuiniela = 0;

    // CONSTRUCTORES
    public Usuario() {}

    public Usuario(String nombre, String password) {
        this.nombre = nombre;
        this.password = password;
        this.puntosQuiniela = 0;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getPuntosQuiniela() { return puntosQuiniela; }
    public void setPuntosQuiniela(int puntosQuiniela) { this.puntosQuiniela = puntosQuiniela; }
}