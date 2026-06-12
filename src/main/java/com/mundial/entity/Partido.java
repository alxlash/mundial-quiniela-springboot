package com.mundial.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "partidos")
public class Partido {

    @Id
    private int numeroPartido; // El "No.dePartido" del CSV es nuestra llave primaria

    private String codigoLocal;     // Guardará "A1", "A2", "L4", etc.
    private String codigoVisitante; // Guardará "B1", "B2", etc.
    
    private LocalDate fecha;
    private LocalTime hora;
    private String estadio;
    private String ciudad;
    private String paisSede; 

    private Integer golesLocal;     
    private Integer golesVisitante; 
    private boolean jugado = false;
    private String fase; // Guardará la etapa del torneo ("GRUPO", "DIECISEISAVOS", etc.)
    private String grupo; // 🔥 Campo faltante: Guardará "A", "B", "C", etc.

    // 💡 CAMPOS TRANSIENT (Virtuales): No se crean en la tabla de la base de datos SQLite,
    // pero nos sirven para pasarle las banderas y nombres reales de los equipos a la interfaz web.
    @Transient
    private String nombreLocal;
    @Transient
    private String banderaLocal;
    @Transient
    private String nombreVisitante;
    @Transient
    private String banderaVisitante;

    // --- CONSTRUCTORES ---
    public Partido() {}

    // --- 🔥 GETTER VIRTUAL COMPATIBLE CON ID ---
    // Como tu repositorio e interfaz a veces buscan un "Id" genérico, este método
    // hace que llamar a getId() devuelva el numeroPartido sin alterar tu base de datos.
    public Integer getId() {
        return this.numeroPartido;
    }

    // --- GETTERS Y SETTERS ---
    public int getNumeroPartido() { return numeroPartido; }
    public void setNumeroPartido(int numeroPartido) { this.numeroPartido = numeroPartido; }

    public String getCodigoLocal() { return codigoLocal; }
    public void setCodigoLocal(String codigoLocal) { this.codigoLocal = codigoLocal; }

    public String getCodigoVisitante() { return codigoVisitante; }
    public void setCodigoVisitante(String codigoVisitante) { this.codigoVisitante = codigoVisitante; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public String getEstadio() { return estadio; }
    public void setEstadio(String estadio) { this.estadio = estadio; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getPaisSede() { return paisSede; }
    public void setPaisSede(String paisSede) { this.paisSede = paisSede; }

    public Integer getGolesLocal() { return golesLocal; }
    public void setGolesLocal(Integer golesLocal) { this.golesLocal = golesLocal; }

    public Integer getGolesVisitante() { return golesVisitante; }
    public void setGolesVisitante(Integer golesVisitante) { this.golesVisitante = golesVisitante; }

    public boolean isJugado() { return jugado; }
    public void setJugado(boolean jugado) { this.jugado = jugado; }

    public String getFase() { return fase; }
    public void setFase(String fase) { this.fase = fase; }

    public String getGrupo() { return grupo; }
    public void setGrupo(String grupo) { this.grupo = grupo; }

    public String getNombreLocal() { return nombreLocal; }
    public void setNombreLocal(String nombreLocal) { this.nombreLocal = nombreLocal; }

    public String getBanderaLocal() { return banderaLocal; }
    public void setBanderaLocal(String banderaLocal) { this.banderaLocal = banderaLocal; }

    public String getNombreVisitante() { return nombreVisitante; }
    public void setNombreVisitante(String nombreVisitante) { this.nombreVisitante = nombreVisitante; }

    public String getBanderaVisitante() { return banderaVisitante; }
    public void setBanderaVisitante(String banderaVisitante) { this.banderaVisitante = banderaVisitante; }
}