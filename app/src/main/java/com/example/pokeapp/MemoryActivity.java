package com.example.pokeapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MemoryActivity extends AppCompatActivity {

    private TextView txtEstadisticasMemory;
    private ProgressBar pbLoadingMemory;
    private Button btnVolverMemory, btnReiniciarMemory;

    private ImageView[] ivCards = new ImageView[12];

    private String[] urlCartas = new String[12];
    private boolean[] cartaBocaArriba = new boolean[12];
    private boolean[] cartaEncontrada = new boolean[12];

    private int primeraCartaIndex = -1;
    private int segundaCartaIndex = -1;
    private boolean bloqueandoTablero = false;

    private int turnos = 0;
    private int paresEncontrados = 0;

    private PokeApiService service;
    private DatabaseReference xpRef;
    private List<String> spritesDescargados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory);

        txtEstadisticasMemory = findViewById(R.id.txtEstadisticasMemory);
        pbLoadingMemory = findViewById(R.id.pbLoadingMemory);
        btnVolverMemory = findViewById(R.id.btnVolverMemory);
        btnReiniciarMemory = findViewById(R.id.btnReiniciarMemory);

        ivCards[0] = findViewById(R.id.imgCard0); ivCards[1] = findViewById(R.id.imgCard1);
        ivCards[2] = findViewById(R.id.imgCard2); ivCards[3] = findViewById(R.id.imgCard3);
        ivCards[4] = findViewById(R.id.imgCard4); ivCards[5] = findViewById(R.id.imgCard5);
        ivCards[6] = findViewById(R.id.imgCard6); ivCards[7] = findViewById(R.id.imgCard7);
        ivCards[8] = findViewById(R.id.imgCard8); ivCards[9] = findViewById(R.id.imgCard9);
        ivCards[10] = findViewById(R.id.imgCard10); ivCards[11] = findViewById(R.id.imgCard11);

        for (int i = 0; i < 12; i++) {
            final int index = i;
            ivCards[i].setOnClickListener(v -> alTocarCarta(index));
        }

        service = ApiClient.getClient().create(PokeApiService.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            xpRef = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid()).child("xp");
        }

        btnVolverMemory.setOnClickListener(v -> finish());
        btnReiniciarMemory.setOnClickListener(v -> iniciarJuegoNuevo());

        iniciarJuegoNuevo();
    }

    private void iniciarJuegoNuevo() {
        turnos = 0;
        paresEncontrados = 0;
        primeraCartaIndex = -1;
        segundaCartaIndex = -1;
        bloqueandoTablero = true;
        spritesDescargados.clear();

        actualizarTextos();
        ocultarTodasLasCartas();

        pbLoadingMemory.setVisibility(View.VISIBLE);
        descargarPokemons(0);
    }

    private void descargarPokemons(int index) {
        if (index == 6) {
            prepararTablero();
            return;
        }

        int randomId = new Random().nextInt(898) + 1;
        service.getPokemon(String.valueOf(randomId)).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSprites() != null) {
                    spritesDescargados.add(response.body().getSprites().getFrontDefault());
                    descargarPokemons(index + 1);
                } else {
                    descargarPokemons(index);
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(MemoryActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void prepararTablero() {
        List<String> deck = new ArrayList<>();
        for (String sprite : spritesDescargados) {
            deck.add(sprite);
            deck.add(sprite);
        }

        Collections.shuffle(deck);
        urlCartas = deck.toArray(new String[0]);

        pbLoadingMemory.setVisibility(View.GONE);
        bloqueandoTablero = false;
    }

    private void ocultarTodasLasCartas() {
        for (int i = 0; i < 12; i++) {
            cartaBocaArriba[i] = false;
            cartaEncontrada[i] = false;

            Glide.with(this).clear(ivCards[i]);
            ivCards[i].setImageResource(android.R.drawable.ic_menu_help);
            ivCards[i].setColorFilter(Color.parseColor("#888888"));
        }
    }

    private void alTocarCarta(int posicion) {
        if (bloqueandoTablero || cartaBocaArriba[posicion] || cartaEncontrada[posicion]) {
            return;
        }

        voltearCartaArriba(posicion);

        if (primeraCartaIndex == -1) {
            primeraCartaIndex = posicion;
        } else {
            segundaCartaIndex = posicion;
            turnos++;
            actualizarTextos();
            verificarSiSonPareja();
        }
    }

    private void voltearCartaArriba(int posicion) {
        cartaBocaArriba[posicion] = true;
        ivCards[posicion].clearColorFilter();
        Glide.with(this).load(urlCartas[posicion]).into(ivCards[posicion]);
    }

    private void verificarSiSonPareja() {
        bloqueandoTablero = true;

        if (urlCartas[primeraCartaIndex].equals(urlCartas[segundaCartaIndex])) {
            cartaEncontrada[primeraCartaIndex] = true;
            cartaEncontrada[segundaCartaIndex] = true;
            paresEncontrados++;
            actualizarTextos();

            primeraCartaIndex = -1;
            segundaCartaIndex = -1;
            bloqueandoTablero = false;

            if (paresEncontrados == 6) {
                terminarJuego();
            }
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                cartaBocaArriba[primeraCartaIndex] = false;
                cartaBocaArriba[segundaCartaIndex] = false;

                Glide.with(this).clear(ivCards[primeraCartaIndex]);
                Glide.with(this).clear(ivCards[segundaCartaIndex]);

                ivCards[primeraCartaIndex].setImageResource(android.R.drawable.ic_menu_help);
                ivCards[primeraCartaIndex].setColorFilter(Color.parseColor("#888888"));

                ivCards[segundaCartaIndex].setImageResource(android.R.drawable.ic_menu_help);
                ivCards[segundaCartaIndex].setColorFilter(Color.parseColor("#888888"));

                primeraCartaIndex = -1;
                segundaCartaIndex = -1;
                bloqueandoTablero = false;
            }, 1000);
        }
    }

    private void actualizarTextos() {
        txtEstadisticasMemory.setText("Turnos: " + turnos + "  |  Pares: " + paresEncontrados + "/6");
    }

    private void terminarJuego() {
        int xpGanada = 50;
        sumarExperiencia(xpGanada);

        // REGISTRO EN HISTORIAL
        HistorialHelper.registrar("minijuego", "PokéMemory", "Completado en " + turnos + " turnos (+ " + xpGanada + " XP)");

        mostrarDialogoPersonalizado("¡Completado!", "¡Has encontrado los 6 pares en " + turnos + " turnos!\n\nGanaste +" + xpGanada + " XP.", true);
    }

    private void sumarExperiencia(int cantidad) {
        if (xpRef != null) {
            xpRef.get().addOnSuccessListener(snapshot -> {
                int xpActual = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                xpRef.setValue(xpActual + cantidad);
            });
        }
    }

    private void mostrarDialogoPersonalizado(String titulo, String mensaje, boolean esVictoria) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_resultado);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialog.setCancelable(false);

        TextView txtTitulo = dialog.findViewById(R.id.txtDialogTitulo);
        TextView txtMensaje = dialog.findViewById(R.id.txtDialogMensaje);
        Button btnSiguiente = dialog.findViewById(R.id.btnDialogSiguiente);

        txtTitulo.setText(titulo);
        txtMensaje.setText(mensaje);
        txtTitulo.setTextColor(Color.parseColor(esVictoria ? "#4CAF50" : "#F44336"));

        btnSiguiente.setText("Volver a jugar");

        btnSiguiente.setOnClickListener(v -> {
            dialog.dismiss();
            iniciarJuegoNuevo();
        });

        dialog.show();
    }
}