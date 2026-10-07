package com.example.pokeapp;

public class ItemHistorial {
    private String id;
    private String tipo; // Puede ser: "minijuego", "combate", "sistema"
    private String titulo;
    private String detalle;
    private long timestamp;

    public ItemHistorial() {
    }

    public ItemHistorial(String id, String tipo, String titulo, String detalle, long timestamp) {
        this.id = id;
        this.tipo = tipo;
        this.titulo = titulo;
        this.detalle = detalle;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getDetalle() { return detalle; }
    public long getTimestamp() { return timestamp; }
}