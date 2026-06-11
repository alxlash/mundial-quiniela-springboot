package com.mundial.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "equipos")
public class Equipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String banderaEmoji; // "🇲🇽", "🇦🇷", "🇧🇷"
    private String nombre;
    private String grupo; // "A", "B", "C"...
    private String posicionGrupo; // Guardará valores como "1A", "2A", "3B", etc.


    // Estadísticas Reales del Mundial
    private int puntos = 0;
    private int partidosJugados = 0;
    private int golesAFavor = 0;
    private int golesEnContra = 0;

    // Constructores
    public Equipo() {}

    public Equipo(String nombre, String grupo, String banderaEmoji) {
        this.banderaEmoji = banderaEmoji;
        this.nombre = nombre;
        this.grupo = grupo;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }
    public String getBanderaEmoji() { return banderaEmoji; }
    public void setBanderaEmoji(String banderaEmoji) { this.banderaEmoji = banderaEmoji; }
    public int getPuntos() { return puntos; }
    public void setPuntos(int puntos) { this.puntos = puntos; }
    public int getPartidosJugados() { return partidosJugados; }
    public void setPartidosJugados(int partidosJugados) { this.partidosJugados = partidosJugados; }
    public int getGolesAFavor() { return golesAFavor; }
    public void setGolesAFavor(int golesAFavor) { this.golesAFavor = golesAFavor; }
    public int getGolesEnContra() { return golesEnContra; }
    public void setGolesEnContra(int golesEnContra) { this.golesEnContra = golesEnContra; }
    public String getPosicionGrupo() { return posicionGrupo;}
    public void setPosicionGrupo(String posicionGrupo) { this.posicionGrupo = posicionGrupo; }
}