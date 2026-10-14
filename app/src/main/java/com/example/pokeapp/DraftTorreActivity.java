package com.example.pokeapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DraftTorreActivity extends AppCompatActivity {

    private GridLayout gridOpciones;
    private ProgressBar pbCargando;
    private Button btnEntrarTorre;
    private PokeApiService service;

    private List<String> pokesSeleccionados = new ArrayList<>();
    private int descargasCompletadas = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draft_torre);

        gridOpciones = findViewById(R.id.gridOpciones);
        pbCargando = findViewById(R.id.pbCargandoDraft);
        btnEntrarTorre = findViewById(R.id.btnEntrarTorre);
        service = ApiClient.getClient().create(PokeApiService.class);

        generarOpcionesAleatorias();

        btnEntrarTorre.setOnClickListener(v -> {
            if (pokesSeleccionados.size() == 3) {
                Toast.makeText(this, "Equipo confirmado. ¡A la Torre!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DraftTorreActivity.this, TorreActivity.class);
                intent.putStringArrayListExtra("EQUIPO_JUGADOR", (ArrayList<String>) pokesSeleccionados);
                startActivity(intent);
                finish();
            }
        });
    }

    private void generarOpcionesAleatorias() {
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            int idAleatorio = random.nextInt(151) + 1;
            descargarPokemonParaDraft(String.valueOf(idAleatorio));
        }
    }

    private void descargarPokemonParaDraft(String idPokemon) {
        service.getPokemon(idPokemon).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();
                    String nombre = p.getName().toUpperCase();
                    String sprite = p.getSprites().getFrontDefault();
                    String idReal = String.valueOf(p.getId());

                    crearTarjetaPokemon(idReal, nombre, sprite);
                }
                verificarCargaCompleta();
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                verificarCargaCompleta();
            }
        });
    }

    private synchronized void verificarCargaCompleta() {
        descargasCompletadas++;
        if (descargasCompletadas == 6) {
            pbCargando.setVisibility(View.GONE);
            gridOpciones.setVisibility(View.VISIBLE);
        }
    }

    private void crearTarjetaPokemon(String idApi, String nombre, String urlImagen) {
        CardView card = new CardView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(16, 16, 16, 16);
        card.setLayoutParams(params);
        card.setRadius(16f);
        card.setCardElevation(8f);
        card.setCardBackgroundColor(Color.WHITE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(16, 24, 16, 24);

        ImageView img = new ImageView(this);
        img.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
        Glide.with(this).load(urlImagen).into(img);

        TextView txt = new TextView(this);
        txt.setText(nombre);
        txt.setTextSize(14f);
        txt.setGravity(Gravity.CENTER);
        txt.setTextColor(Color.parseColor("#333333"));
        txt.setPadding(0, 8, 0, 0);

        layout.addView(img);
        layout.addView(txt);
        card.addView(layout);

        card.setOnClickListener(v -> {
            if (pokesSeleccionados.contains(idApi)) {
                pokesSeleccionados.remove(idApi);
                card.setCardBackgroundColor(Color.WHITE);
            } else {
                if (pokesSeleccionados.size() < 3) {
                    pokesSeleccionados.add(idApi);
                    card.setCardBackgroundColor(Color.parseColor("#C8E6C9"));
                } else {
                    Toast.makeText(this, "Ya elegiste 3 Pokémon", Toast.LENGTH_SHORT).show();
                }
            }
            actualizarBoton();
        });

        gridOpciones.addView(card);
    }

    private void actualizarBoton() {
        btnEntrarTorre.setText("¡Entrar a la Torre! (" + pokesSeleccionados.size() + "/3)");
        if (pokesSeleccionados.size() == 3) {
            btnEntrarTorre.setEnabled(true);
            btnEntrarTorre.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336")));
        } else {
            btnEntrarTorre.setEnabled(false);
            btnEntrarTorre.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#888888")));
        }
    }
}