package com.example.pokeapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MaestroTiposActivity extends AppCompatActivity {

    private ImageView imgPokemonMisterio;
    private ProgressBar pbCargandoPokemon;
    private Button btnAdivinar, btnVolverMaestro;
    private TextView txtRachaMaestro;

    private PokeApiService service;
    private DatabaseReference xpRef;

    private List<String> tiposCorrectos = new ArrayList<>();
    private List<String> tiposSeleccionados = new ArrayList<>();
    private String nombrePokemonActual = "";

    private int rachaActual = 0;
    private double multiplicador = 1.0;

    private final int[] botonesTiposIds = {
            R.id.btnTypeNormal, R.id.btnTypeFire, R.id.btnTypeWater,
            R.id.btnTypeGrass, R.id.btnTypeElectric, R.id.btnTypeIce,
            R.id.btnTypeFighting, R.id.btnTypePoison, R.id.btnTypeGround,
            R.id.btnTypeFlying, R.id.btnTypePsychic, R.id.btnTypeBug,
            R.id.btnTypeRock, R.id.btnTypeGhost, R.id.btnTypeDragon,
            R.id.btnTypeDark, R.id.btnTypeSteel, R.id.btnTypeFairy
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maestro_tipos);

        imgPokemonMisterio = findViewById(R.id.imgPokemonMisterio);
        pbCargandoPokemon = findViewById(R.id.pbCargandoPokemon);
        btnAdivinar = findViewById(R.id.btnAdivinar);
        btnVolverMaestro = findViewById(R.id.btnVolverMaestro);
        txtRachaMaestro = findViewById(R.id.txtRachaMaestro);

        service = ApiClient.getClient().create(PokeApiService.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            xpRef = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid()).child("xp");
        }

        configurarBotonesTipos();
        actualizarTextoRacha();

        btnAdivinar.setOnClickListener(v -> comprobarRespuesta());
        btnVolverMaestro.setOnClickListener(v -> finish());

        cargarNuevoPokemon();
    }

    private void configurarBotonesTipos() {
        asignarLogicaBoton(R.id.btnTypeNormal, "normal");
        asignarLogicaBoton(R.id.btnTypeFire, "fire");
        asignarLogicaBoton(R.id.btnTypeWater, "water");
        asignarLogicaBoton(R.id.btnTypeGrass, "grass");
        asignarLogicaBoton(R.id.btnTypeElectric, "electric");
        asignarLogicaBoton(R.id.btnTypeIce, "ice");
        asignarLogicaBoton(R.id.btnTypeFighting, "fighting");
        asignarLogicaBoton(R.id.btnTypePoison, "poison");
        asignarLogicaBoton(R.id.btnTypeGround, "ground");
        asignarLogicaBoton(R.id.btnTypeFlying, "flying");
        asignarLogicaBoton(R.id.btnTypePsychic, "psychic");
        asignarLogicaBoton(R.id.btnTypeBug, "bug");
        asignarLogicaBoton(R.id.btnTypeRock, "rock");
        asignarLogicaBoton(R.id.btnTypeGhost, "ghost");
        asignarLogicaBoton(R.id.btnTypeDragon, "dragon");
        asignarLogicaBoton(R.id.btnTypeDark, "dark");
        asignarLogicaBoton(R.id.btnTypeSteel, "steel");
        asignarLogicaBoton(R.id.btnTypeFairy, "fairy");
    }

    private void asignarLogicaBoton(int botonId, String tipoAPI) {
        Button btn = findViewById(botonId);
        btn.setTag(tipoAPI);

        btn.setOnClickListener(v -> {
            String tipo = (String) v.getTag();

            if (tiposSeleccionados.contains(tipo)) {
                tiposSeleccionados.remove(tipo);
                v.setAlpha(0.4f);
            } else {
                if (tiposSeleccionados.size() < 2) {
                    tiposSeleccionados.add(tipo);
                    v.setAlpha(1.0f);
                } else {
                    Toast.makeText(this, "Solo puedes elegir máximo 2 tipos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void actualizarTextoRacha() {
        txtRachaMaestro.setText(String.format(Locale.ROOT, "Racha: %d 🔥  |  Multiplicador: %.1fx", rachaActual, multiplicador));
    }

    private void cargarNuevoPokemon() {
        tiposCorrectos.clear();
        tiposSeleccionados.clear();
        imgPokemonMisterio.setImageDrawable(null);
        pbCargandoPokemon.setVisibility(View.VISIBLE);

        for (int id : botonesTiposIds) {
            Button btn = findViewById(id);
            if (btn != null) btn.setAlpha(0.4f);
        }

        int randomId = new Random().nextInt(898) + 1;

        Call<Pokemon> call = service.getPokemon(String.valueOf(randomId));
        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();
                    nombrePokemonActual = p.getName().toUpperCase();

                    for (Pokemon.TypeSlot slot : p.getTypes()) {
                        tiposCorrectos.add(slot.getType().getName());
                    }

                    if (p.getSprites() != null) {
                        Glide.with(MaestroTiposActivity.this)
                                .load(p.getSprites().getFrontDefault())
                                .into(imgPokemonMisterio);
                    }
                    pbCargandoPokemon.setVisibility(View.GONE);
                } else {
                    cargarNuevoPokemon();
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(MaestroTiposActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void comprobarRespuesta() {
        if (tiposSeleccionados.isEmpty()) {
            Toast.makeText(this, "¡Selecciona al menos un tipo!", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean esVictoria = (tiposSeleccionados.size() == tiposCorrectos.size()) && tiposCorrectos.containsAll(tiposSeleccionados);

        StringBuilder tiposTraducidos = new StringBuilder();
        for (String tipo : tiposCorrectos) {
            tiposTraducidos.append(traducirTipo(tipo)).append(" ");
        }

        if (esVictoria) {
            rachaActual++;
            multiplicador = Math.min(5.0, 1.0 + Math.floor(rachaActual / 10.0) * 0.1);
            int xpGanada = (int) (10 * multiplicador);
            sumarExperiencia(xpGanada);
            actualizarTextoRacha();

            // REGISTRO EN HISTORIAL
            HistorialHelper.registrar("minijuego", "Maestro de Tipos", "Adivinaste a " + nombrePokemonActual + " (+ " + xpGanada + " XP)");

            mostrarDialogoPersonalizado("¡Correcto!", "¡Eres un Maestro!\n" + nombrePokemonActual + " es de tipo:\n" + tiposTraducidos.toString().trim() + "\n\nHas ganado +" + xpGanada + " XP.", true);
        } else {
            rachaActual = 0;
            multiplicador = 1.0;
            actualizarTextoRacha();

            // REGISTRO EN HISTORIAL (También guarda los fallos)
            HistorialHelper.registrar("minijuego", "Maestro de Tipos", "Fallaste intentando adivinar a " + nombrePokemonActual);

            mostrarDialogoPersonalizado("Casi...", "Fallaste. " + nombrePokemonActual + " en realidad es de tipo:\n" + tiposTraducidos.toString().trim() + "\n\nPerdiste tu racha.", false);
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

        if (esVictoria) {
            txtTitulo.setTextColor(Color.parseColor("#4CAF50")); // Verde
        } else {
            txtTitulo.setTextColor(Color.parseColor("#F44336")); // Rojo
        }

        btnSiguiente.setOnClickListener(v -> {
            dialog.dismiss();
            cargarNuevoPokemon();
        });

        dialog.show();
    }

    private void sumarExperiencia(int cantidad) {
        if (xpRef != null) {
            xpRef.get().addOnSuccessListener(snapshot -> {
                int xpActual = snapshot.exists() ? snapshot.getValue(Integer.class) : 0;
                xpRef.setValue(xpActual + cantidad);
            });
        }
    }

    private String traducirTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "normal": return "Normal"; case "fire": return "Fuego"; case "water": return "Agua";
            case "electric": return "Eléctrico"; case "grass": return "Planta"; case "ice": return "Hielo";
            case "fighting": return "Lucha"; case "poison": return "Veneno"; case "ground": return "Tierra";
            case "flying": return "Volador"; case "psychic": return "Psíquico"; case "bug": return "Bicho";
            case "rock": return "Roca"; case "ghost": return "Fantasma"; case "dragon": return "Dragón";
            case "dark": return "Siniestro"; case "steel": return "Acero"; case "fairy": return "Hada";
            default: return tipo;
        }
    }
}