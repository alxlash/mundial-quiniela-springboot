package com.mundial.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pronosticos")
public class Pronostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "partido_id")
    private Partido partido;

    // Los goles que tu amigo CREE que van a quedar
    private int golesLocal;
    private int golesVisitante;

    // Los puntos que gano en este partido (0, 3 o 5)
    private int puntosObtenidos;

    // --- CONSTRUCTORES ---
    public Pronostico() {}

    public Pronostico(Usuario usuario, Partido partido, int golesLocal, int golesVisitante) {
        this.usuario = usuario;
        this.partido = partido;
        this.golesLocal = golesLocal;
        this.golesVisitante = golesVisitante;
        this.puntosObtenidos = 0; // Inicia en cero hasta que se juegue el partido real
    }

    // --- GETTERS AND SETTERS (Los "botones" que el Service necesita) ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Partido getPartido() {
        return partido;
    }

    public void setPartido(Partido partido) {
        this.partido = partido;
    }

    public int getGolesLocal() {
        return golesLocal;
    }

    public void setGolesLocal(int golesLocal) {
        this.golesLocal = golesLocal;
    }

    public int getGolesVisitante() {
        return golesVisitante;
    }

    public void setGolesVisitante(int golesVisitante) {
        this.golesVisitante = golesVisitante;
    }

    public int getPuntosObtenidos() {
        return puntosObtenidos;
    }

    public void setPuntosObtenidos(int puntosObtenidos) {
        this.puntosObtenidos = puntosObtenidos;
    }
}