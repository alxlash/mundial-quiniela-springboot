package com.mundial.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pronosticos")
public class Pronostico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    // 🔥 El núcleo del pronóstico: Guardará de forma fija "LOCAL", "EMPATE" o "VISITANTE"
    @Column(nullable = false)
    private String tendenciaElegida;

    // 🔥 Nuevo campo opcional para el debate/picardía entre amigos
    @Column(length = 255)
    private String comentario;

    // Guardará 1 (si acierta) o 0 (si falla)
    private int puntosObtenidos = 0;

    // CONSTRUCTORES
    public Pronostico() {}

    public Pronostico(Usuario usuario, Partido partido, String tendenciaElegida, String comentario) {
        this.usuario = usuario;
        this.partido = partido;
        this.tendenciaElegida = tendenciaElegida;
        this.comentario = comentario;
        this.puntosObtenidos = 0;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Partido getPartido() { return partido; }
    public void setPartido(Partido partido) { this.partido = partido; }

    public String getTendenciaElegida() { return tendenciaElegida; }
    public void setTendenciaElegida(String tendenciaElegida) { this.tendenciaElegida = tendenciaElegida; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public int getPuntosObtenidos() { return puntosObtenidos; }
    public void setPuntosObtenidos(int puntosObtenidos) { this.puntosObtenidos = puntosObtenidos; }
}