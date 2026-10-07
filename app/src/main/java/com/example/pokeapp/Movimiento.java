package com.example.pokeapp;

public class Movimiento {
    private String nombre;
    private String tipo;
    private int poder;

    public Movimiento(String nombre, String tipo, int poder) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.poder = poder;
    }

    public String getNombre() { return nombre; }
    public String getTipo() { return tipo; }
    public int getPoder() { return poder; }
}