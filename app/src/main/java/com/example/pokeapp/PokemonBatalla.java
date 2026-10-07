package com.example.pokeapp;

import java.util.List;

public class PokemonBatalla {
    private int idAPI; // NUEVO: Necesario para descargar el grito
    private String nombre;
    private int hpMax;
    private int hpActual;
    private int ataque;
    private int defensa;
    private int velocidad;
    private String spriteFrente;
    private String spriteEspalda;
    private List<String> tipos;
    private Movimiento[] movimientos;

    public PokemonBatalla(String nombre, int hpMax, int ataque, int defensa, int velocidad, String spriteFrente, String spriteEspalda, List<String> tipos) {
        this.nombre = nombre;
        this.hpMax = hpMax;
        this.hpActual = hpMax;
        this.ataque = ataque;
        this.defensa = defensa;
        this.velocidad = velocidad;
        this.spriteFrente = spriteFrente;
        this.spriteEspalda = spriteEspalda;
        this.tipos = tipos;
        this.movimientos = new Movimiento[4];
    }

    // NUEVOS METODOS PARA EL GRITO
    public int getIdAPI() { return idAPI; }
    public void setIdAPI(int idAPI) { this.idAPI = idAPI; }

    public String getNombre() { return nombre; }
    public int getHpMax() { return hpMax; }
    public int getHpActual() { return hpActual; }
    public int getAtaque() { return ataque; }
    public int getDefensa() { return defensa; }
    public int getVelocidad() { return velocidad; }
    public String getSpriteFrente() { return spriteFrente; }
    public String getSpriteEspalda() { return spriteEspalda; }
    public List<String> getTipos() { return tipos; }

    public Movimiento[] getMovimientos() { return movimientos; }
    public void setMovimientos(Movimiento[] movimientos) { this.movimientos = movimientos; }

    public void recibirDano(int cantidad) {
        this.hpActual -= cantidad;
        if (this.hpActual < 0) this.hpActual = 0;
    }

    public boolean estaVivo() {
        return this.hpActual > 0;
    }
}