package com.example.pokeapp;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmulatorActivity extends AppCompatActivity {

    private CardView cardSelector;
    private RadioGroup rgModoBatalla;
    private LinearLayout layoutFila2, layoutFila3, panelAtaques;
    private EditText etP1_1, etR1_1, etP1_2, etR1_2, etP1_3, etR1_3;
    private Button btnCargar, btnAleatorio, btnAtq1, btnAtq2, btnAtq3, btnAtq4, btnHuirBatalla, btnSalirSelector;
    private TextView txtNombreEmu1, txtHpNumEmu1, txtNombreEmu2, txtHpNumEmu2, txtLogEmulador;
    private ProgressBar pbHpEmu1, pbHpEmu2;
    private ImageView imgEmu1, imgEmu2;
    private RelativeLayout layoutArenaEmulador;

    private PokeApiService service;
    private List<PokemonBatalla> equipoJugador = new ArrayList<>();
    private List<PokemonBatalla> equipoRival = new ArrayList<>();

    private int indiceJugador = 0;
    private int indiceRival = 0;
    private int modoSeleccionado = 1;

    private boolean batallaTerminada = false;
    private int numeroTurno = 1;

    // NUEVO: El reproductor de gritos
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulator);

        enlazarVistas();
        service = ApiClient.getClient().create(PokeApiService.class);

        rgModoBatalla.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb1v1) { modoSeleccionado = 1; layoutFila2.setVisibility(View.GONE); layoutFila3.setVisibility(View.GONE); }
            else if (checkedId == R.id.rb2v2) { modoSeleccionado = 2; layoutFila2.setVisibility(View.VISIBLE); layoutFila3.setVisibility(View.GONE); }
            else if (checkedId == R.id.rb3v3) { modoSeleccionado = 3; layoutFila2.setVisibility(View.VISIBLE); layoutFila3.setVisibility(View.VISIBLE); }
        });

        btnCargar.setOnClickListener(v -> prepararDescarga());
        btnAleatorio.setOnClickListener(v -> llenarEquipoAleatorio());

        btnHuirBatalla.setOnClickListener(v -> {
            Toast.makeText(this, "Huiste del combate...", Toast.LENGTH_SHORT).show();
            resetearASeleccion();
        });

        btnSalirSelector.setOnClickListener(v -> finish());
    }

    private void enlazarVistas() {
        cardSelector = findViewById(R.id.cardSelector);
        rgModoBatalla = findViewById(R.id.rgModoBatalla);
        layoutFila2 = findViewById(R.id.layoutFila2);
        layoutFila3 = findViewById(R.id.layoutFila3);

        etP1_1 = findViewById(R.id.etP1_1); etR1_1 = findViewById(R.id.etR1_1);
        etP1_2 = findViewById(R.id.etP1_2); etR1_2 = findViewById(R.id.etR1_2);
        etP1_3 = findViewById(R.id.etP1_3); etR1_3 = findViewById(R.id.etR1_3);

        btnCargar = findViewById(R.id.btnCargarEmulador);
        btnAleatorio = findViewById(R.id.btnAleatorio);
        btnHuirBatalla = findViewById(R.id.btnHuirBatalla);
        btnSalirSelector = findViewById(R.id.btnSalirSelector);

        panelAtaques = findViewById(R.id.panelAtaques);
        btnAtq1 = findViewById(R.id.btnAtaque1); btnAtq2 = findViewById(R.id.btnAtaque2);
        btnAtq3 = findViewById(R.id.btnAtaque3); btnAtq4 = findViewById(R.id.btnAtaque4);

        txtNombreEmu1 = findViewById(R.id.txtNombreEmu1); txtHpNumEmu1 = findViewById(R.id.txtHpNumEmu1);
        txtNombreEmu2 = findViewById(R.id.txtNombreEmu2); txtHpNumEmu2 = findViewById(R.id.txtHpNumEmu2);
        txtLogEmulador = findViewById(R.id.txtLogEmulador);
        pbHpEmu1 = findViewById(R.id.pbHpEmu1); pbHpEmu2 = findViewById(R.id.pbHpEmu2);
        imgEmu1 = findViewById(R.id.imgEmu1); imgEmu2 = findViewById(R.id.imgEmu2);
        layoutArenaEmulador = findViewById(R.id.layoutArenaEmulador);
    }

    private void llenarEquipoAleatorio() {
        Random rnd = new Random();
        etP1_1.setText(String.valueOf(rnd.nextInt(898) + 1));
        etR1_1.setText(String.valueOf(rnd.nextInt(898) + 1));

        if (modoSeleccionado >= 2) {
            etP1_2.setText(String.valueOf(rnd.nextInt(898) + 1));
            etR1_2.setText(String.valueOf(rnd.nextInt(898) + 1));
        } else { etP1_2.setText(""); etR1_2.setText(""); }

        if (modoSeleccionado == 3) {
            etP1_3.setText(String.valueOf(rnd.nextInt(898) + 1));
            etR1_3.setText(String.valueOf(rnd.nextInt(898) + 1));
        } else { etP1_3.setText(""); etR1_3.setText(""); }
    }

    private void prepararDescarga() {
        List<String> pokesJugador = new ArrayList<>();
        List<String> pokesRival = new ArrayList<>();

        pokesJugador.add(etP1_1.getText().toString().trim().toLowerCase());
        pokesRival.add(etR1_1.getText().toString().trim().toLowerCase());

        if (modoSeleccionado >= 2) {
            pokesJugador.add(etP1_2.getText().toString().trim().toLowerCase());
            pokesRival.add(etR1_2.getText().toString().trim().toLowerCase());
        }
        if (modoSeleccionado == 3) {
            pokesJugador.add(etP1_3.getText().toString().trim().toLowerCase());
            pokesRival.add(etR1_3.getText().toString().trim().toLowerCase());
        }

        for (String s : pokesJugador) if (s.isEmpty()) { Toast.makeText(this, "Llena tu equipo", Toast.LENGTH_SHORT).show(); return; }
        for (String s : pokesRival) if (s.isEmpty()) { Toast.makeText(this, "Llena el equipo rival", Toast.LENGTH_SHORT).show(); return; }

        equipoJugador.clear(); equipoRival.clear();
        indiceJugador = 0; indiceRival = 0;

        btnCargar.setText("Descargando...");
        btnCargar.setEnabled(false);
        btnAleatorio.setEnabled(false);
        txtLogEmulador.setText("Reclutando equipos desde la PokéAPI...");

        descargarRecursivo(pokesJugador, pokesRival, 0, true);
    }

    private void descargarRecursivo(List<String> listJugador, List<String> listRival, int index, boolean cargandoJugador) {
        if (!cargandoJugador && index == listRival.size()) {
            iniciarArena();
            return;
        }

        String pokeBuscado = cargandoJugador ? listJugador.get(index) : listRival.get(index);

        service.getPokemon(pokeBuscado).enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();

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

                    List<String> tiposPoke = new ArrayList<>();
                    if (p.getTypes() != null) {
                        for (Pokemon.TypeSlot ts : p.getTypes()) tiposPoke.add(ts.getType().getName());
                    }

                    int hpMax = hpBase * 3;
                    String spriteFrente = p.getSprites().getFrontDefault();
                    String spriteEspalda = p.getSprites().getBackDefault();
                    if (spriteEspalda == null) spriteEspalda = spriteFrente;

                    PokemonBatalla nuevoPoke = new PokemonBatalla(
                            p.getName().toUpperCase(Locale.ROOT), hpMax, atkBase, defBase, spdBase, spriteFrente, spriteEspalda, tiposPoke
                    );
                    // NUEVO: Guardamos el ID de la API en el Pokémon para poder descargar su grito
                    nuevoPoke.setIdAPI(p.getId());
                    nuevoPoke.setMovimientos(generarMovimientos(tiposPoke));

                    if (cargandoJugador) {
                        equipoJugador.add(nuevoPoke);
                        if (index + 1 == listJugador.size()) descargarRecursivo(listJugador, listRival, 0, false);
                        else descargarRecursivo(listJugador, listRival, index + 1, true);
                    } else {
                        equipoRival.add(nuevoPoke);
                        descargarRecursivo(listJugador, listRival, index + 1, false);
                    }
                } else {
                    Toast.makeText(EmulatorActivity.this, pokeBuscado + " no existe.", Toast.LENGTH_SHORT).show();
                    resetearVentanaSeleccion();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(EmulatorActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
                resetearVentanaSeleccion();
            }
        });
    }

    private void resetearVentanaSeleccion() {
        btnCargar.setText("¡A Luchar!");
        btnCargar.setEnabled(true);
        btnAleatorio.setEnabled(true);
        txtLogEmulador.setText("Selecciona tus equipos arriba.");
    }

    private void iniciarArena() {
        cardSelector.setVisibility(View.GONE);
        layoutArenaEmulador.setVisibility(View.VISIBLE);
        panelAtaques.setVisibility(View.INVISIBLE);

        batallaTerminada = false;
        numeroTurno = 1;

        actualizarPantallaArena();
        decidirPrimerTurno();
    }

    private PokemonBatalla getPokeJugador() { return equipoJugador.get(indiceJugador); }
    private PokemonBatalla getPokeRival() { return equipoRival.get(indiceRival); }

    private void actualizarPantallaArena() {
        PokemonBatalla miPoke = getPokeJugador();
        PokemonBatalla suPoke = getPokeRival();

        txtNombreEmu1.setText(miPoke.getNombre());
        pbHpEmu1.setMax(miPoke.getHpMax());
        pbHpEmu1.setProgress(miPoke.getHpActual());
        txtHpNumEmu1.setText(miPoke.getHpActual() + "/" + miPoke.getHpMax());
        Glide.with(this).load(miPoke.getSpriteEspalda()).into(imgEmu1);

        txtNombreEmu2.setText(suPoke.getNombre());
        pbHpEmu2.setMax(suPoke.getHpMax());
        pbHpEmu2.setProgress(suPoke.getHpActual());
        txtHpNumEmu2.setText(suPoke.getHpActual() + "/" + suPoke.getHpMax());
        Glide.with(this).load(suPoke.getSpriteFrente()).into(imgEmu2);

        configurarBotonesAtaque();

        // GRITAN AL ENTRAR A LA ARENA
        reproducirGrito(miPoke.getIdAPI());
        new Handler(Looper.getMainLooper()).postDelayed(() -> reproducirGrito(suPoke.getIdAPI()), 1000);
    }

    private void configurarBotonesAtaque() {
        Movimiento[] movs = getPokeJugador().getMovimientos();
        configurarBotonIndividual(btnAtq1, movs[0]);
        configurarBotonIndividual(btnAtq2, movs[1]);
        configurarBotonIndividual(btnAtq3, movs[2]);
        configurarBotonIndividual(btnAtq4, movs[3]);
    }

    private void configurarBotonIndividual(Button btn, Movimiento mov) {
        btn.setText(mov.getNombre() + "\n(" + mov.getPoder() + ")");
        btn.setBackgroundColor(Color.parseColor(obtenerColorTipo(mov.getTipo())));
        btn.setOnClickListener(v -> gestionarTurno(mov));
    }

    private void decidirPrimerTurno() {
        if (getPokeJugador().getVelocidad() >= getPokeRival().getVelocidad()) {
            txtLogEmulador.setText("¡" + getPokeJugador().getNombre() + " es más rápido!\n¿Qué vas a hacer?");
            panelAtaques.setVisibility(View.VISIBLE);
        } else {
            txtLogEmulador.setText("¡El rival es más rápido!\n" + getPokeRival().getNombre() + " ataca primero.");
            panelAtaques.setVisibility(View.INVISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> realizarAtaqueRival(null), 1500);
        }
    }

    private void gestionarTurno(Movimiento movJugador) {
        if (batallaTerminada) return;
        panelAtaques.setVisibility(View.INVISIBLE);

        if (getPokeJugador().getVelocidad() >= getPokeRival().getVelocidad()) {
            boolean rivalSobrevive = ejecutarGolpe(getPokeJugador(), getPokeRival(), movJugador, true);
            if (rivalSobrevive) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> realizarAtaqueRival(null), 2000);
            }
        } else {
            boolean rivalSobrevive = ejecutarGolpe(getPokeJugador(), getPokeRival(), movJugador, true);
            if (rivalSobrevive) prepararSiguienteTurno();
        }
    }

    private void realizarAtaqueRival(Movimiento movJugadorParaDespues) {
        if (batallaTerminada) return;

        Movimiento movRival = getPokeRival().getMovimientos()[new Random().nextInt(4)];
        boolean jugadorSobrevive = ejecutarGolpe(getPokeRival(), getPokeJugador(), movRival, false);

        if (jugadorSobrevive) {
            if (getPokeRival().getVelocidad() > getPokeJugador().getVelocidad()) {
                panelAtaques.setVisibility(View.VISIBLE);
                txtLogEmulador.setText("Turno " + numeroTurno + ".\n¿Qué hará " + getPokeJugador().getNombre() + "?");
            } else {
                prepararSiguienteTurno();
            }
        }
    }

    private void prepararSiguienteTurno() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            numeroTurno++;
            decidirPrimerTurno();
        }, 2000);
    }

    private boolean ejecutarGolpe(PokemonBatalla atacante, PokemonBatalla defensor, Movimiento mov, boolean atacanteEsJugador) {
        double efectividad = 1.0;
        for (String tipoDef : defensor.getTipos()) efectividad *= obtenerMultiplicador(mov.getTipo(), tipoDef);

        int dano = (int) (((atacante.getAtaque() * mov.getPoder() * 0.4) / defensor.getDefensa()) * efectividad);
        if (dano < 1 && efectividad > 0) dano = 1;
        if (efectividad == 0) dano = 0;

        defensor.recibirDano(dano);

        animarAtaque(atacanteEsJugador ? imgEmu1 : imgEmu2, atacanteEsJugador);
        animarDanoYParpadeo(atacanteEsJugador ? imgEmu2 : imgEmu1);

        // REPRODUCIR GRITO AL ATACAR
        reproducirGrito(atacante.getIdAPI());

        actualizarPantallaArena();

        String efecTexto = "";
        if (efectividad > 1.0) efecTexto = "¡Es súper efectivo!\n";
        else if (efectividad < 1.0 && efectividad > 0) efecTexto = "No es muy efectivo...\n";
        else if (efectividad == 0) efecTexto = "¡No le afectó!\n";

        txtLogEmulador.setText("Turno " + numeroTurno + "\n" + atacante.getNombre() + " usó " + mov.getNombre() + ".\n" + efecTexto + "Causó " + dano + " de daño.");

        if (!defensor.estaVivo()) {
            manejarDebilitamiento(atacanteEsJugador);
            return false;
        }
        return true;
    }

    private void manejarDebilitamiento(boolean atacanteEsJugador) {
        // REPRODUCIR GRITO DEL POKÉMON DEBILITADO
        PokemonBatalla debilitado = atacanteEsJugador ? getPokeRival() : getPokeJugador();
        reproducirGrito(debilitado.getIdAPI());

        if (atacanteEsJugador) {
            indiceRival++;
            if (indiceRival < equipoRival.size()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    txtLogEmulador.setText("¡El rival sacó a " + getPokeRival().getNombre() + "!");
                    actualizarPantallaArena();
                    prepararSiguienteTurno();
                }, 2000);
            } else {
                batallaTerminada = true;
                HistorialHelper.registrar("combate", "Emulador " + modoSeleccionado + "v" + modoSeleccionado, "¡Victoria frente al Rival!");
                mostrarDialogoGanador(getPokeJugador(), true);
            }
        } else {
            indiceJugador++;
            if (indiceJugador < equipoJugador.size()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    txtLogEmulador.setText("¡Ve, " + getPokeJugador().getNombre() + "!");
                    actualizarPantallaArena();
                    prepararSiguienteTurno();
                }, 2000);
            } else {
                batallaTerminada = true;
                HistorialHelper.registrar("combate", "Emulador " + modoSeleccionado + "v" + modoSeleccionado, "Derrota frente al Rival.");
                mostrarDialogoGanador(getPokeRival(), false);
            }
        }
    }

    // --- NUEVO: SISTEMA DE AUDIO PARA REPRODUCIR EL GRITO ---
    private void reproducirGrito(int id) {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/" + id + ".ogg");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return;

                InputStream input = conn.getInputStream();
                File temp = new File(getCacheDir(), "grito_batalla.ogg");
                FileOutputStream output = new FileOutputStream(temp);
                byte[] data = new byte[1024]; int count;
                while ((count = input.read(data)) != -1) output.write(data, 0, count);
                output.flush(); output.close(); input.close();

                runOnUiThread(() -> {
                    try {
                        if (mediaPlayer != null) mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(temp.getAbsolutePath());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (Exception e) { /* Ignorar si el audio falla para no detener el juego */ }
                });
            } catch (Exception e) { /* Ignorar errores de red en el audio */ }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void animarAtaque(ImageView img, boolean esP1) {
        float movimiento = esP1 ? 50f : -50f;
        ObjectAnimator anim = ObjectAnimator.ofFloat(img, "translationX", 0f, movimiento, 0f);
        anim.setDuration(200);
        anim.start();
    }

    private void animarDanoYParpadeo(ImageView imgDefensor) {
        int duracion = 150;
        imgDefensor.setColorFilter(Color.parseColor("#99FF0000"), PorterDuff.Mode.SRC_ATOP);
        imgDefensor.postDelayed(imgDefensor::clearColorFilter, duracion);
        imgDefensor.postDelayed(() -> imgDefensor.setColorFilter(Color.parseColor("#99FF0000"), PorterDuff.Mode.SRC_ATOP), duracion * 2);
        imgDefensor.postDelayed(imgDefensor::clearColorFilter, duracion * 3);
    }

    private void mostrarDialogoGanador(PokemonBatalla ganadorUltimo, boolean jugadorGano) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_ganador);
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);

        ImageView imgGanador = dialog.findViewById(R.id.imgGanadorDialog);
        TextView txtTitulo = dialog.findViewById(R.id.txtDialogTitulo);
        TextView txtNombre = dialog.findViewById(R.id.txtNombreGanadorDialog);
        TextView txtStats = dialog.findViewById(R.id.txtStatsGanadorDialog);
        Button btnRevancha = dialog.findViewById(R.id.btnRevancha);
        Button btnNuevoEquipo = dialog.findViewById(R.id.btnNuevoEquipo);
        Button btnVolverMenu = dialog.findViewById(R.id.btnVolverMenuBatalla);

        Glide.with(this).load(ganadorUltimo.getSpriteFrente()).into(imgGanador);

        if(jugadorGano) {
            txtTitulo.setTextColor(Color.parseColor("#4CAF50"));
            txtNombre.setText("¡GANASTE LA BATALLA!");
        } else {
            txtTitulo.setTextColor(Color.parseColor("#F44336"));
            txtNombre.setText("¡EL RIVAL GANÓ!");
        }

        txtStats.setText("Combate " + modoSeleccionado + "vs" + modoSeleccionado + " finalizado.\nDuró " + numeroTurno + " turnos.");

        btnRevancha.setOnClickListener(v -> {
            dialog.dismiss();
            for (PokemonBatalla p : equipoJugador) p.recibirDano(-p.getHpMax());
            for (PokemonBatalla p : equipoRival) p.recibirDano(-p.getHpMax());
            indiceJugador = 0;
            indiceRival = 0;
            iniciarArena();
        });

        btnNuevoEquipo.setOnClickListener(v -> {
            dialog.dismiss();
            resetearASeleccion();
        });

        btnVolverMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void resetearASeleccion() {
        layoutArenaEmulador.setVisibility(View.GONE);
        cardSelector.setVisibility(View.VISIBLE);
        panelAtaques.setVisibility(View.GONE);

        btnCargar.setText("¡A Luchar!");
        btnCargar.setEnabled(true);
        btnAleatorio.setEnabled(true);
        txtLogEmulador.setText("Selecciona tus equipos arriba.");
    }

    private Movimiento[] generarMovimientos(List<String> tipos) {
        Movimiento[] movs = new Movimiento[4];
        String tipoP = tipos.get(0);
        String tipoS = tipos.size() > 1 ? tipos.get(1) : "normal";
        movs[0] = obtenerAtaqueBase(tipoP, true);
        movs[1] = obtenerAtaqueBase(tipoP, false);
        movs[2] = obtenerAtaqueBase(tipoS, true);
        movs[3] = obtenerAtaqueBase("fighting", false);
        return movs;
    }

    private Movimiento obtenerAtaqueBase(String tipo, boolean debil) {
        switch (tipo) {
            case "fire": return debil ? new Movimiento("Ascuas", "fire", 40) : new Movimiento("Lanzallamas", "fire", 90);
            case "water": return debil ? new Movimiento("Pistola Agua", "water", 40) : new Movimiento("Surf", "water", 90);
            case "grass": return debil ? new Movimiento("Látigo Cepa", "grass", 45) : new Movimiento("Rayo Solar", "grass", 120);
            case "electric": return debil ? new Movimiento("Impactrueno", "electric", 40) : new Movimiento("Rayo", "electric", 90);
            case "ice": return debil ? new Movimiento("Canto Helado", "ice", 40) : new Movimiento("Rayo Hielo", "ice", 90);
            case "fighting": return debil ? new Movimiento("Golpe Karate", "fighting", 50) : new Movimiento("A Bocajarro", "fighting", 120);
            case "poison": return debil ? new Movimiento("Picotazo Ven.", "poison", 30) : new Movimiento("Bomba Lodo", "poison", 90);
            case "ground": return debil ? new Movimiento("Disparo Lodo", "ground", 55) : new Movimiento("Terremoto", "ground", 100);
            case "flying": return debil ? new Movimiento("Tornado", "flying", 40) : new Movimiento("Pájaro Osado", "flying", 120);
            case "psychic": return debil ? new Movimiento("Confusión", "psychic", 50) : new Movimiento("Psíquico", "psychic", 90);
            case "bug": return debil ? new Movimiento("Picadura", "bug", 60) : new Movimiento("Zumbido", "bug", 90);
            case "rock": return debil ? new Movimiento("Lanzarrocas", "rock", 50) : new Movimiento("Roca Afilada", "rock", 100);
            case "ghost": return debil ? new Movimiento("Impresionar", "ghost", 30) : new Movimiento("Bola Sombra", "ghost", 80);
            case "dragon": return debil ? new Movimiento("Ciclón", "dragon", 40) : new Movimiento("Garra Dragón", "dragon", 80);
            case "dark": return debil ? new Movimiento("Mordisco", "dark", 60) : new Movimiento("Pulso Umbrío", "dark", 80);
            case "steel": return debil ? new Movimiento("Garra Metal", "steel", 50) : new Movimiento("Cabezazo Hierro", "steel", 80);
            case "fairy": return debil ? new Movimiento("Viento Feérico", "fairy", 40) : new Movimiento("Fuerza Lunar", "fairy", 95);
            default: return debil ? new Movimiento("Placaje", "normal", 40) : new Movimiento("Hiperrayo", "normal", 150);
        }
    }

    private double obtenerMultiplicador(String ataque, String defensa) {
        if (ataque.equals(defensa)) return 0.5;
        switch (ataque) {
            case "fire":
                if (defensa.equals("grass") || defensa.equals("ice") || defensa.equals("bug") || defensa.equals("steel")) return 2.0;
                if (defensa.equals("water") || defensa.equals("rock") || defensa.equals("dragon")) return 0.5;
                break;
            case "water":
                if (defensa.equals("fire") || defensa.equals("ground") || defensa.equals("rock")) return 2.0;
                if (defensa.equals("grass") || defensa.equals("dragon")) return 0.5;
                break;
            case "grass":
                if (defensa.equals("water") || defensa.equals("ground") || defensa.equals("rock")) return 2.0;
                if (defensa.equals("fire") || defensa.equals("grass") || defensa.equals("poison") || defensa.equals("flying") || defensa.equals("bug") || defensa.equals("dragon") || defensa.equals("steel")) return 0.5;
                break;
            case "electric":
                if (defensa.equals("water") || defensa.equals("flying")) return 2.0;
                if (defensa.equals("grass") || defensa.equals("dragon")) return 0.5;
                if (defensa.equals("ground")) return 0.0;
                break;
            case "ice":
                if (defensa.equals("grass") || defensa.equals("ground") || defensa.equals("flying") || defensa.equals("dragon")) return 2.0;
                if (defensa.equals("fire") || defensa.equals("water") || defensa.equals("ice") || defensa.equals("steel")) return 0.5;
                break;
            case "fighting":
                if (defensa.equals("normal") || defensa.equals("ice") || defensa.equals("rock") || defensa.equals("dark") || defensa.equals("steel")) return 2.0;
                if (defensa.equals("poison") || defensa.equals("flying") || defensa.equals("psychic") || defensa.equals("bug") || defensa.equals("fairy")) return 0.5;
                if (defensa.equals("ghost")) return 0.0;
                break;
            case "ground":
                if (defensa.equals("fire") || defensa.equals("electric") || defensa.equals("poison") || defensa.equals("rock") || defensa.equals("steel")) return 2.0;
                if (defensa.equals("grass") || defensa.equals("bug")) return 0.5;
                if (defensa.equals("flying")) return 0.0;
                break;
            case "psychic":
                if (defensa.equals("fighting") || defensa.equals("poison")) return 2.0;
                if (defensa.equals("psychic") || defensa.equals("steel")) return 0.5;
                if (defensa.equals("dark")) return 0.0;
                break;
            case "ghost":
                if (defensa.equals("psychic") || defensa.equals("ghost")) return 2.0;
                if (defensa.equals("dark")) return 0.5;
                if (defensa.equals("normal")) return 0.0;
                break;
            case "dragon":
                if (defensa.equals("dragon")) return 2.0;
                if (defensa.equals("steel")) return 0.5;
                if (defensa.equals("fairy")) return 0.0;
                break;
        }
        return 1.0;
    }

    private String obtenerColorTipo(String tipo) {
        switch (tipo) {
            case "normal": return "#A8A878"; case "fire": return "#F08030"; case "water": return "#6890F0";
            case "electric": return "#F8D030"; case "grass": return "#78C850"; case "ice": return "#98D8D8";
            case "fighting": return "#C03028"; case "poison": return "#A040A0"; case "ground": return "#E0C068";
            case "flying": return "#A890F0"; case "psychic": return "#F85888"; case "bug": return "#A8B820";
            case "rock": return "#B8A038"; case "ghost": return "#705898"; case "dragon": return "#7038F8";
            case "dark": return "#705848"; case "steel": return "#B8B8D0"; case "fairy": return "#EE99AC";
            default: return "#68A090";
        }
    }
}