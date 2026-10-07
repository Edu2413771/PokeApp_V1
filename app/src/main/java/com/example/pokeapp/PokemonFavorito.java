package com.example.pokeapp;

public class PokemonFavorito {
    private int idAPI;
    private String nombreVisible;
    private String formaAPI;
    private boolean esShiny;
    private String urlImagen;

    // Firebase necesita un constructor vacío obligatorio
    public PokemonFavorito() {
    }

    public PokemonFavorito(int idAPI, String nombreVisible, String formaAPI, boolean esShiny, String urlImagen) {
        this.idAPI = idAPI;
        this.nombreVisible = nombreVisible;
        this.formaAPI = formaAPI;
        this.esShiny = esShiny;
        this.urlImagen = urlImagen;
    }

    public int getIdAPI() { return idAPI; }
    public void setIdAPI(int idAPI) { this.idAPI = idAPI; }

    public String getNombreVisible() { return nombreVisible; }
    public void setNombreVisible(String nombreVisible) { this.nombreVisible = nombreVisible; }

    public String getFormaAPI() { return formaAPI; }
    public void setFormaAPI(String formaAPI) { this.formaAPI = formaAPI; }

    public boolean isEsShiny() { return esShiny; }
    public void setEsShiny(boolean esShiny) { this.esShiny = esShiny; }

    public String getUrlImagen() { return urlImagen; }
    public void setUrlImagen(String urlImagen) { this.urlImagen = urlImagen; }
}