package com.mundial.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "partidos")
public class Partido {

    @Id
    private int numeroPartido; // El "No.dePartido" del CSV será nuestra llave primaria

    private String codigoLocal;     // Guardará "A1", "A2", "L4", etc.
    private String codigoVisitante; // Guardará "B1", "B2", etc.
    
    private LocalDate fecha;
    private LocalTime hora;
    private String estadio;
    private String ciudad;
    private String paisSede; // Para evitar confusión con el país del equipo, lo llamamos paisSede

    private Integer golesLocal;     // Nullable hasta que se juegue
    private Integer golesVisitante; // Nullable hasta que se juegue
    private boolean jugado = false;

	    // Dentro de la clase Partido.java:
	    private String fase; // Guardará la etapa del torneo
	
	    public String getFase() { return fase; }
	    public void setFase(String fase) { this.fase = fase; }
    
    
    // --- CONSTRUCTORES ---
    public Partido() {}

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
}