package com.example.pokeapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class EvolutionChain {
    private ChainLink chain;
    public ChainLink getChain() { return chain; }

    public static class ChainLink {
        private Species species;
        @SerializedName("evolves_to")
        private List<ChainLink> evolvesTo;

        public Species getSpecies() { return species; }
        public List<ChainLink> getEvolvesTo() { return evolvesTo; }
    }

    public static class Species {
        private String name;
        public String getName() { return name; }
    }
}