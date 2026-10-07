package com.example.pokeapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface PokeApiService {

    @GET("pokemon/{id}")
    Call<Pokemon> getPokemon(@Path("id") String id);

    @GET("evolution-chain/{id}")
    Call<EvolutionChain> getEvolutionChain(@Path("id") int id);

    // Permite buscar la especie usando el nombre (String) en lugar del ID (int)
    @GET("pokemon-species/{name}")
    Call<PokemonSpecies> getPokemonSpeciesStr(@Path("name") String name);

}