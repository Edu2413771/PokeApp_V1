package com.example.pokeapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class FavoritoAdapter extends RecyclerView.Adapter<FavoritoAdapter.ViewHolder> {

    private List<PokemonFavorito> listaFavoritos;
    private Context context;

    public FavoritoAdapter(List<PokemonFavorito> listaFavoritos, Context context) {
        this.listaFavoritos = listaFavoritos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorito, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PokemonFavorito pokemon = listaFavoritos.get(position);

        holder.txtNombreFav.setText(pokemon.getNombreVisible());

        Glide.with(context)
                .load(pokemon.getUrlImagen())
                .into(holder.imgFav);

        if (pokemon.isEsShiny()) {
            holder.txtShinyIcon.setVisibility(View.VISIBLE);
        } else {
            holder.txtShinyIcon.setVisibility(View.GONE);
        }

        // ¡EL NUEVO CÓDIGO DE NAVEGACIÓN!
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, MainActivity.class);
            // Empaquetamos los datos clave para enviarlos a la Pokédex
            intent.putExtra("POKEMON_BUSQUEDA", pokemon.getFormaAPI());
            intent.putExtra("ES_SHINY", pokemon.isEsShiny());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return listaFavoritos.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFav;
        TextView txtNombreFav, txtShinyIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFav = itemView.findViewById(R.id.imgFav);
            txtNombreFav = itemView.findViewById(R.id.txtNombreFav);
            txtShinyIcon = itemView.findViewById(R.id.txtShinyIcon);
        }
    }
}