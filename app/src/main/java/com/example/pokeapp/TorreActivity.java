package com.example.pokeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class TorreActivity extends AppCompatActivity {

    private Button btnVolverTorre;
    private LinearLayout[] cardsNiveles = new LinearLayout[8];
    private ImageView[] imgsNiveles = new ImageView[8];
    private TextView[] txtsNiveles = new TextView[8];
    private ArrayList<String> equipoJugador = new ArrayList<>();

    private int nivelActual = 1;

    private final String[] nombresProfes = {
            "Prof. Everardo", "Profa. Rita", "Profa. Angélica",
            "Prof. Edgar", "Prof. Víctor", "Prof. Campa",
            "Coord. Badi", "Directora Myrna"
    };

    private final String[] materiasProfes = {
            "Contabilidad", "Física", "Matemáticas",
            "Compiladores", "Apps Móviles", "Ciencia de Datos",
            "Redes Computacionales", "JEFE FINAL"
    };

    private final int[] fotosProfes = {
            R.drawable.everardo, R.drawable.rita, R.drawable.angelica,
            R.drawable.edgar, R.drawable.victor, R.drawable.campa,
            R.drawable.badi, R.drawable.myrna
    };

    private final int[][] poolsProfesores = {
            {143, 115, 128, 82, 208, 212, 227}, // 1. Everardo
            {68, 56, 106, 76, 95, 112, 237},    // 2. Rita
            {65, 97, 124, 131, 91, 144, 238},   // 3. Angélica
            {49, 123, 127, 24, 34, 31, 89},     // 4. Edgar
            {26, 135, 125, 82, 208, 212, 100},  // 5. Víctor
            {149, 65, 121, 103, 80, 230, 148},  // 6. Campa
            {145, 142, 130, 18, 135, 169, 146}, // 7. Badi
            {150, 151, 144, 145, 146, 249, 250, 384} // 8. Myrna
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_torre);

        if (getIntent().hasExtra("EQUIPO_JUGADOR")) {
            equipoJugador = getIntent().getStringArrayListExtra("EQUIPO_JUGADOR");
        } else {
            Toast.makeText(this, "Error: No se cargó el equipo", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        enlazarVistas();

        btnVolverTorre.setOnClickListener(v -> onBackPressed());

        SharedPreferences prefs = getSharedPreferences("TorreCESBA", MODE_PRIVATE);
        nivelActual = prefs.getInt("nivel_actual", 1);

        actualizarEscaleraVisual();

        for (int i = 0; i < 8; i++) {
            final int nivel = i + 1;
            cardsNiveles[i].setOnClickListener(v -> iniciarCombate(nivel));
        }
    }

    private void enlazarVistas() {
        btnVolverTorre = findViewById(R.id.btnVolverTorre);

        cardsNiveles[0] = findViewById(R.id.cardNivel1);
        cardsNiveles[1] = findViewById(R.id.cardNivel2);
        cardsNiveles[2] = findViewById(R.id.cardNivel3);
        cardsNiveles[3] = findViewById(R.id.cardNivel4);
        cardsNiveles[4] = findViewById(R.id.cardNivel5);
        cardsNiveles[5] = findViewById(R.id.cardNivel6);
        cardsNiveles[6] = findViewById(R.id.cardNivel7);
        cardsNiveles[7] = findViewById(R.id.cardNivel8);

        imgsNiveles[0] = findViewById(R.id.imgNivel1);
        imgsNiveles[1] = findViewById(R.id.imgNivel2);
        imgsNiveles[2] = findViewById(R.id.imgNivel3);
        imgsNiveles[3] = findViewById(R.id.imgNivel4);
        imgsNiveles[4] = findViewById(R.id.imgNivel5);
        imgsNiveles[5] = findViewById(R.id.imgNivel6);
        imgsNiveles[6] = findViewById(R.id.imgNivel7);
        imgsNiveles[7] = findViewById(R.id.imgNivel8);

        txtsNiveles[0] = findViewById(R.id.txtNivel1);
        txtsNiveles[1] = findViewById(R.id.txtNivel2);
        txtsNiveles[2] = findViewById(R.id.txtNivel3);
        txtsNiveles[3] = findViewById(R.id.txtNivel4);
        txtsNiveles[4] = findViewById(R.id.txtNivel5);
        txtsNiveles[5] = findViewById(R.id.txtNivel6);
        txtsNiveles[6] = findViewById(R.id.txtNivel7);
        txtsNiveles[7] = findViewById(R.id.txtNivel8);
    }

    private void actualizarEscaleraVisual() {
        for (int i = 0; i < 8; i++) {
            LinearLayout card = cardsNiveles[i];
            ImageView img = imgsNiveles[i];
            TextView txt = txtsNiveles[i];

            if (i < nivelActual - 1) {
                card.setEnabled(false);
                card.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50")));
                txt.setTextColor(Color.WHITE);
                txt.setText("✅ NIVEL " + (i + 1) + "\n" + nombresProfes[i] + " [ SUPERADO ]");

                img.setImageResource(fotosProfes[i]);
                img.setColorFilter(Color.argb(150, 0, 0, 0));

            } else if (i == nivelActual - 1) {
                card.setEnabled(true);
                card.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935")));
                txt.setTextColor(Color.WHITE);

                if (i == 7) {
                    txt.setText("👑 NIVEL " + (i + 1) + "\n" + nombresProfes[i] + "\n[ " + materiasProfes[i] + " ]");
                } else {
                    txt.setText("⚔️ NIVEL " + (i + 1) + "\n" + nombresProfes[i] + "\n[ " + materiasProfes[i] + " ]");
                }

                img.setImageResource(fotosProfes[i]);
                img.clearColorFilter();

            } else {
                card.setEnabled(false);
                card.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1E1E2C")));
                txt.setTextColor(Color.parseColor("#555566"));
                txt.setText("🔒 NIVEL " + (i + 1) + "\n??? [ DESCONOCIDO ]");

                img.setImageResource(R.drawable.icon_pokeapp);
                img.setColorFilter(Color.argb(200, 0, 0, 0));
            }

            card.setAlpha(0f);
            card.setTranslationX(-100f);
            card.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setStartDelay(i * 120L)
                    .start();
        }
    }

    private void iniciarCombate(int nivel) {
        int index = nivel - 1;
        String profe = nombresProfes[index];
        int fotoProfe = fotosProfes[index];

        ArrayList<String> equipoEnemigo = new ArrayList<>();
        int[] pool = poolsProfesores[index];
        Random rnd = new Random();

        int cantidadPokemons = (nivel == 8) ? 4 : 3;

        while (equipoEnemigo.size() < cantidadPokemons) {
            String idAleatorio = String.valueOf(pool[rnd.nextInt(pool.length)]);
            if (!equipoEnemigo.contains(idAleatorio)) {
                equipoEnemigo.add(idAleatorio);
            }
        }

        Toast.makeText(this, "¡Preparando batalla contra " + profe + "!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(TorreActivity.this, BatallaTorreActivity.class);
        intent.putStringArrayListExtra("EQUIPO_JUGADOR", equipoJugador);
        intent.putStringArrayListExtra("EQUIPO_RIVAL", equipoEnemigo);
        intent.putExtra("NOMBRE_RIVAL", profe);
        intent.putExtra("FOTO_RIVAL", fotoProfe);
        intent.putExtra("NIVEL_ACTUAL", nivel);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SharedPreferences.Editor editor = getSharedPreferences("TorreCESBA", MODE_PRIVATE).edit();
        editor.putInt("nivel_actual", 1);
        editor.apply();
        Toast.makeText(this, "Abandonaste la Torre. Progreso reiniciado.", Toast.LENGTH_SHORT).show();
    }
}