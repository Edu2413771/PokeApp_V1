package com.example.pokeapp;

import com.google.gson.annotations.SerializedName;

public class Sprites {

    @SerializedName("front_default")
    private String frontDefault;

    @SerializedName("front_shiny")
    private String frontShiny;

    // ¡NUEVO! Le decimos a la app que también descargue el sprite de espalda
    @SerializedName("back_default")
    private String backDefault;

    @SerializedName("back_shiny")
    private String backShiny;

    public String getFrontDefault() {
        return frontDefault;
    }

    public void setFrontDefault(String frontDefault) {
        this.frontDefault = frontDefault;
    }

    public String getFrontShiny() {
        return frontShiny;
    }

    public void setFrontShiny(String frontShiny) {
        this.frontShiny = frontShiny;
    }

    // ¡NUEVOS METODOS GET Y SET PARA LA ESPALDA!
    public String getBackDefault() {
        return backDefault;
    }

    public void setBackDefault(String backDefault) {
        this.backDefault = backDefault;
    }

    public String getBackShiny() {
        return backShiny;
    }

    public void setBackShiny(String backShiny) {
        this.backShiny = backShiny;
    }
}