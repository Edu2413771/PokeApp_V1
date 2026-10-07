package com.example.pokeapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistorialAdapter extends RecyclerView.Adapter<HistorialAdapter.ViewHolder> {

    private List<ItemHistorial> lista;
    private Context context;
    private SimpleDateFormat sdf;

    public HistorialAdapter(List<ItemHistorial> lista, Context context) {
        this.lista = lista;
        this.context = context;
        // Formato de hora más amigable (ej. 02/10/26 04:30 PM)
        this.sdf = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_historial, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemHistorial item = lista.get(position);

        holder.txtTituloHistorial.setText(item.getTitulo());
        holder.txtDetalleHistorial.setText(item.getDetalle());

        String fechaAmigable = sdf.format(new Date(item.getTimestamp()));
        holder.txtFechaHistorial.setText(fechaAmigable);

        // --- LÓGICA DE COLORES POR ACTIVIDAD ---
        String titulo = item.getTitulo();
        String colorHex = "#888888"; // Gris por defecto

        if (titulo.contains("Pokédex")) {
            colorHex = "#F44336"; // Rojo Pokédex
        } else if (titulo.contains("Maestro")) {
            colorHex = "#9C27B0"; // Morado Maestro de Tipos
        } else if (titulo.contains("Memory")) {
            colorHex = "#E91E63"; // Rosa PokéMemory
        } else if (titulo.contains("Favorito")) {
            colorHex = "#FFCB05"; // Amarillo Favoritos
        } else if (titulo.contains("Quién") || titulo.contains("Quien")) {
            colorHex = "#2196F3"; // Azul Quién es ese Pokémon
        } else if (item.getTipo().equals("combate")) {
            colorHex = "#FF9800"; // Naranja para futuros combates en la Torre
        }

        // Aplicamos el color a la barra lateral y al título
        holder.viewColorTipo.setBackgroundColor(Color.parseColor(colorHex));
        holder.txtTituloHistorial.setTextColor(Color.parseColor(colorHex));
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View viewColorTipo;
        TextView txtTituloHistorial, txtFechaHistorial, txtDetalleHistorial;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorTipo = itemView.findViewById(R.id.viewColorTipo);
            txtTituloHistorial = itemView.findViewById(R.id.txtTituloHistorial);
            txtFechaHistorial = itemView.findViewById(R.id.txtFechaHistorial);
            txtDetalleHistorial = itemView.findViewById(R.id.txtDetalleHistorial);
        }
    }
}