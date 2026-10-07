package com.example.pokeapp;

public class PokemonMove {
    private Move move;

    public Move getMove() {
        return move;
    }

    public static class Move {
        private String name;
        public String getName() {
            return name;
        }
    }
}