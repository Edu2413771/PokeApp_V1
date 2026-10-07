package com.example.pokeapp;

import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuienEsActivity extends AppCompatActivity {

    private ImageView imgSilueta;
    private TextView txtCargando, txtRacha, txtMultiplicador;
    private Button btnOpcion1, btnOpcion2, btnOpcion3, btnOpcion4, btnSalirJuego;

    private PokeApiService service;
    private String nombreCorrecto = "";
    private int rachaActual = 0;

    private FirebaseAuth mAuth;
    private DatabaseReference perfilRef;

    private ColorStateList colorBotonNormal;
    private int colorSiluetaTema;
    private final int COLOR_CORRECTO = Color.parseColor("#4CAF50");
    private final int COLOR_INCORRECTO = Color.parseColor("#F44336");

    private final String[] nombresFalsos = {
            "bulbasaur", "charizard", "squirtle", "pikachu", "gengar", "eevee", "snorlax", "mewtwo",
            "chikorita", "cyndaquil", "totodile", "typhlosion", "lugia", "ho-oh", "tyranitar", "umbreon",
            "treecko", "torchic", "mudkip", "blaziken", "gardevoir", "rayquaza", "salamence", "metagross",
            "turtwig", "chimchar", "piplup", "lucario", "garchomp", "dialga", "palkia", "arceus",
            "snivy", "tepig", "oshawott", "zoroark", "haxorus", "hydreigon", "reshiram", "zekrom",
            "chespin", "fennekin", "froakie", "greninja", "sylveon", "xerneas", "yveltal", "zygarde",
            "rowlet", "litten", "popplio", "incineroar", "decidueye", "mimikyu", "solgaleo", "lunala",
            "grookey", "scorbunny", "sobble", "cinderace", "dragapult", "zacian", "zamazenta", "eternatus",
            "sprigatito", "fuecoco", "quaxly", "meowscarada", "tinkaton", "ceruledge", "koraidon", "miraidon"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quien_es);

        enlazarVistas();

        int modoActual = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        colorSiluetaTema = (modoActual == Configuration.UI_MODE_NIGHT_YES) ? Color.WHITE : Color.BLACK;

        int colorFondo = (modoActual == Configuration.UI_MODE_NIGHT_YES) ? Color.parseColor("#1E1E1E") : Color.WHITE;
        colorBotonNormal = ColorStateList.valueOf(colorFondo);

        service = ApiClient.getClient().create(PokeApiService.class);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            perfilRef = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid());
        }

        btnSalirJuego.setOnClickListener(v -> finish());

        actualizarMarcadoresUI(1.0);
        cargarNuevoPokemon();
    }

    private void enlazarVistas() {
        imgSilueta = findViewById(R.id.imgSilueta);
        txtCargando = findViewById(R.id.txtCargando);
        txtRacha = findViewById(R.id.txtRacha);
        txtMultiplicador = findViewById(R.id.txtMultiplicador);

        btnOpcion1 = findViewById(R.id.btnOpcion1);
        btnOpcion2 = findViewById(R.id.btnOpcion2);
        btnOpcion3 = findViewById(R.id.btnOpcion3);
        btnOpcion4 = findViewById(R.id.btnOpcion4);
        btnSalirJuego = findViewById(R.id.btnSalirJuego);
    }

    private void cargarNuevoPokemon() {
        txtCargando.setVisibility(View.VISIBLE);
        imgSilueta.setImageDrawable(null);
        activarBotones(false);
        resetearColorBotones();

        int idAleatorio = new Random().nextInt(1025) + 1;

        Call<Pokemon> call = service.getPokemon(String.valueOf(idAleatorio));
        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();
                    nombreCorrecto = p.getName().toUpperCase(Locale.ROOT);

                    if (p.getSprites() != null) {
                        String imgUrl = p.getSprites().getFrontDefault();
                        if (imgUrl != null) {
                            Glide.with(QuienEsActivity.this)
                                    .load(imgUrl)
                                    .into(imgSilueta);

                            imgSilueta.setColorFilter(colorSiluetaTema, PorterDuff.Mode.SRC_ATOP);
                        }
                    }

                    txtCargando.setVisibility(View.GONE);
                    configurarOpciones();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(QuienEsActivity.this, "Reconectando...", Toast.LENGTH_SHORT).show();
                cargarNuevoPokemon();
            }
        });
    }

    private void configurarOpciones() {
        List<String> opciones = new ArrayList<>();
        opciones.add(nombreCorrecto);

        Random rnd = new Random();
        while (opciones.size() < 4) {
            String falso = nombresFalsos[rnd.nextInt(nombresFalsos.length)].toUpperCase(Locale.ROOT);
            if (!opciones.contains(falso)) {
                opciones.add(falso);
            }
        }

        Collections.shuffle(opciones);

        btnOpcion1.setText(opciones.get(0));
        btnOpcion2.setText(opciones.get(1));
        btnOpcion3.setText(opciones.get(2));
        btnOpcion4.setText(opciones.get(3));

        View.OnClickListener listener = v -> verificarRespuesta((Button) v);
        btnOpcion1.setOnClickListener(listener);
        btnOpcion2.setOnClickListener(listener);
        btnOpcion3.setOnClickListener(listener);
        btnOpcion4.setOnClickListener(listener);

        activarBotones(true);
    }

    private void verificarRespuesta(Button botonPresionado) {
        activarBotones(false);
        imgSilueta.clearColorFilter();

        String respuestaElegida = botonPresionado.getText().toString();

        if (respuestaElegida.equals(nombreCorrecto)) {
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECTO));

            rachaActual++;
            double multiplicador = 1.0 + ((rachaActual / 10) * 0.1);
            multiplicador = Math.min(multiplicador, 5.0);

            int xpGanada = (int) Math.round(5 * multiplicador);

            actualizarMarcadoresUI(multiplicador);
            sumarXpFirebase(xpGanada);

            Toast.makeText(this, "¡+" + xpGanada + " XP!", Toast.LENGTH_SHORT).show();

            // AQUÍ ES DONDE VA EL REGISTRO DE VICTORIA
            HistorialHelper.registrar("minijuego", "¿Quién es ese Pokémon?", "Adivinaste a " + nombreCorrecto + " (+ " + xpGanada + " XP)");

        } else {
            botonPresionado.setBackgroundTintList(ColorStateList.valueOf(COLOR_INCORRECTO));
            iluminarBotonCorrecto();

            rachaActual = 0;
            actualizarMarcadoresUI(1.0);
            Toast.makeText(this, "¡Fallaste!", Toast.LENGTH_SHORT).show();

            // AQUÍ ES DONDE VA EL REGISTRO DE DERROTA
            HistorialHelper.registrar("minijuego", "¿Quién es ese Pokémon?", "Fallaste intentando adivinar a " + nombreCorrecto);
        }

        new Handler().postDelayed(this::cargarNuevoPokemon, 2000);
    }

    private void iluminarBotonCorrecto() {
        if (btnOpcion1.getText().toString().equals(nombreCorrecto)) btnOpcion1.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECTO));
        if (btnOpcion2.getText().toString().equals(nombreCorrecto)) btnOpcion2.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECTO));
        if (btnOpcion3.getText().toString().equals(nombreCorrecto)) btnOpcion3.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECTO));
        if (btnOpcion4.getText().toString().equals(nombreCorrecto)) btnOpcion4.setBackgroundTintList(ColorStateList.valueOf(COLOR_CORRECTO));
    }

    private void resetearColorBotones() {
        btnOpcion1.setBackgroundTintList(colorBotonNormal);
        btnOpcion2.setBackgroundTintList(colorBotonNormal);
        btnOpcion3.setBackgroundTintList(colorBotonNormal);
        btnOpcion4.setBackgroundTintList(colorBotonNormal);
    }

    private void actualizarMarcadoresUI(double multiplicador) {
        txtRacha.setText("🔥 Racha: " + rachaActual);
        txtMultiplicador.setText(String.format(Locale.ROOT, "⚡ x%.1f", multiplicador));
    }

    private void activarBotones(boolean activo) {
        btnOpcion1.setEnabled(activo);
        btnOpcion2.setEnabled(activo);
        btnOpcion3.setEnabled(activo);
        btnOpcion4.setEnabled(activo);
    }

    private void sumarXpFirebase(int xpSumar) {
        if (perfilRef != null) {
            perfilRef.child("xp").get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    int xpActual = snapshot.getValue(Integer.class);
                    int nuevaXp = xpActual + xpSumar;
                    int nuevoNivel = (nuevaXp / 100) + 1;

                    perfilRef.child("xp").setValue(nuevaXp);
                    perfilRef.child("nivel").setValue(nuevoNivel);
                }
            });
        }
    }
}