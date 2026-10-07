package com.example.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Pokemon {
    private int id;
    private String name;
    private Sprites sprites;
    private List<PokemonStat> stats;
    private List<PokemonMove> moves;
    private Cries cries;

    @SerializedName("types")
    private List<TypeSlot> types;

    public int getId() { return id; }
    public String getName() { return name; }
    public Sprites getSprites() { return sprites; }
    public List<PokemonStat> getStats() { return stats; }
    public List<PokemonMove> getMoves() { return moves; }
    public Cries getCries() { return cries; }
    public List<TypeSlot> getTypes() { return types; }

    public static class Cries {
        private String latest;
        private String legacy;
        public String getLatest() { return latest; }
        public String getLegacy() { return legacy; }
    }

    public static class TypeSlot {
        @SerializedName("slot")
        private int slot;

        @SerializedName("type")
        private TypeData type;

        public TypeData getType() { return type; }
    }

    public static class TypeData {
        @SerializedName("name")
        private String name;

        public String getName() { return name; }
    }
}