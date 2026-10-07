package com.example.pokeapp;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatallaActivity extends AppCompatActivity {

    // Vistas de Carga
    private LinearLayout layoutCargaBatalla;
    private TextView txtEstadoCarga;

    // Vistas de la Arena
    private TextView txtNombreRival, txtNombreJugador, txtHpJugadorNum, txtLogBatalla;
    private ProgressBar pbHpRival, pbHpJugador;
    private ImageView imgRival, imgJugador;
    private Button btnAtacar, btnCambiar, btnHuir;

    // Datos del Combate
    private PokeApiService service;
    private List<PokemonBatalla> equipoJugador = new ArrayList<>();
    private List<PokemonBatalla> equipoRival = new ArrayList<>();

    private int indiceActivoJugador = 0;
    private int indiceActivoRival = 0;
    private int pokemonsDescargados = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batalla);

        enlazarVistas();
        service = ApiClient.getClient().create(PokeApiService.class);

        btnHuir.setOnClickListener(v -> finish());

        btnCambiar.setOnClickListener(v -> {
            Toast.makeText(this, "Aún no programamos el cambio de equipo.", Toast.LENGTH_SHORT).show();
        });

        btnAtacar.setOnClickListener(v -> ejecutarTurno());

        // Iniciar la descarga de los 6 Pokémon (3 tuyos, 3 del rival)
        descargarEquiposAleatorios();
    }

    private void enlazarVistas() {
        layoutCargaBatalla = findViewById(R.id.layoutCargaBatalla);
        txtEstadoCarga = findViewById(R.id.txtEstadoCarga);

        txtNombreRival = findViewById(R.id.txtNombreRival);
        pbHpRival = findViewById(R.id.pbHpRival);
        imgRival = findViewById(R.id.imgRival);

        txtNombreJugador = findViewById(R.id.txtNombreJugador);
        pbHpJugador = findViewById(R.id.pbHpJugador);
        txtHpJugadorNum = findViewById(R.id.txtHpJugadorNum);
        imgJugador = findViewById(R.id.imgJugador);

        txtLogBatalla = findViewById(R.id.txtLogBatalla);
        btnAtacar = findViewById(R.id.btnAtacar);
        btnCambiar = findViewById(R.id.btnCambiar);
        btnHuir = findViewById(R.id.btnHuir);
    }

    private void descargarEquiposAleatorios() {
        if (pokemonsDescargados == 6) {
            // Ya tenemos los 6. Empezar la pelea.
            layoutCargaBatalla.setVisibility(View.GONE);
            actualizarArena();
            txtLogBatalla.setText("¡Un Entrenador Rival te desafía a un combate 3 vs 3!");
            return;
        }

        txtEstadoCarga.setText("Reclutando equipos (" + pokemonsDescargados + "/6)...");

        // Obtenemos un Pokémon aleatorio
        int idAleatorio = new Random().nextInt(649) + 1;

        service.getPokemon(String.valueOf(idAleatorio)).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();

                    // Extraer estadísticas básicas
                    int hpBase = 50, atkBase = 50, defBase = 50, spdBase = 50;
                    if (p.getStats() != null) {
                        for (PokemonStat stat : p.getStats()) {
                            switch (stat.getStat().getName()) {
                                case "hp": hpBase = stat.getBaseStat(); break;
                                case "attack": atkBase = stat.getBaseStat(); break;
                                case "defense": defBase = stat.getBaseStat(); break;
                                case "speed": spdBase = stat.getBaseStat(); break;
                            }
                        }
                    }

                    // ¡NUEVO! Extraer la lista de tipos para que el molde no tire error
                    List<String> tiposPoke = new ArrayList<>();
                    if (p.getTypes() != null) {
                        for (Pokemon.TypeSlot ts : p.getTypes()) {
                            tiposPoke.add(ts.getType().getName());
                        }
                    }

                    // Multiplicamos el HP x2
                    int hpMaximoReal = hpBase * 2;

                    String spriteFrente = p.getSprites().getFrontDefault();
                    String spriteEspalda = p.getSprites().getBackDefault();
                    if (spriteEspalda == null) spriteEspalda = spriteFrente;

                    // ¡AQUÍ ESTÁ LA CORRECCIÓN! Ahora le pasamos la lista de 'tiposPoke' al final
                    PokemonBatalla nuevoPoke = new PokemonBatalla(
                            p.getName().toUpperCase(Locale.ROOT),
                            hpMaximoReal, atkBase, defBase, spdBase,
                            spriteFrente, spriteEspalda, tiposPoke
                    );

                    // Los 3 primeros van para el jugador, los 3 siguientes para el rival
                    if (pokemonsDescargados < 3) {
                        equipoJugador.add(nuevoPoke);
                    } else {
                        equipoRival.add(nuevoPoke);
                    }

                    pokemonsDescargados++;
                    descargarEquiposAleatorios(); // Recursividad
                } else {
                    descargarEquiposAleatorios(); // Reintentar si falla
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(BatallaActivity.this, "Error de conexión, reintentando...", Toast.LENGTH_SHORT).show();
                descargarEquiposAleatorios();
            }
        });
    }

    private void actualizarArena() {
        PokemonBatalla miPoke = equipoJugador.get(indiceActivoJugador);
        PokemonBatalla suPoke = equipoRival.get(indiceActivoRival);

        // Actualizar UI del Rival
        txtNombreRival.setText(suPoke.getNombre());
        pbHpRival.setMax(suPoke.getHpMax());
        pbHpRival.setProgress(suPoke.getHpActual());
        Glide.with(this).load(suPoke.getSpriteFrente()).into(imgRival);

        // Actualizar UI del Jugador
        txtNombreJugador.setText(miPoke.getNombre());
        pbHpJugador.setMax(miPoke.getHpMax());
        pbHpJugador.setProgress(miPoke.getHpActual());
        txtHpJugadorNum.setText(miPoke.getHpActual() + " / " + miPoke.getHpMax());
        Glide.with(this).load(miPoke.getSpriteEspalda()).into(imgJugador);
    }

    private void ejecutarTurno() {
        PokemonBatalla miPoke = equipoJugador.get(indiceActivoJugador);
        PokemonBatalla suPoke = equipoRival.get(indiceActivoRival);

        btnAtacar.setEnabled(false);

        // 1. Atacas tú primero
        int miDano = (miPoke.getAtaque() * 20) / suPoke.getDefensa();
        if (miDano < 5) miDano = 5;

        suPoke.recibirDano(miDano);
        txtLogBatalla.setText("¡" + miPoke.getNombre() + " usó su ataque!\nCausó " + miDano + " de daño.");
        actualizarArena();

        if (!suPoke.estaVivo()) {
            txtLogBatalla.setText("¡El " + suPoke.getNombre() + " rival se ha debilitado!");
            btnAtacar.setEnabled(true);
            return;
        }

        // 2. El rival ataca de vuelta después de 1 segundo
        new Handler().postDelayed(() -> {
            int suDano = (suPoke.getAtaque() * 20) / miPoke.getDefensa();
            if (suDano < 5) suDano = 5;

            miPoke.recibirDano(suDano);
            txtLogBatalla.setText("¡" + suPoke.getNombre() + " enemigo atacó!\nCausó " + suDano + " de daño.");
            actualizarArena();

            if (!miPoke.estaVivo()) {
                txtLogBatalla.setText("¡Tu " + miPoke.getNombre() + " se ha debilitado!");
            }

            btnAtacar.setEnabled(true);
        }, 1500);
    }
}