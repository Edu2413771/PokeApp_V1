package com.example.pokeapp;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class BatallaTorreActivity extends AppCompatActivity {

    private CardView cardSelector;
    private LinearLayout panelAtaques;
    private Button btnAtq1, btnAtq2, btnAtq3, btnAtq4, btnHuirBatalla;
    private TextView txtNombreEmu1, txtHpNumEmu1, txtNombreEmu2, txtHpNumEmu2, txtLogEmulador;
    private ProgressBar pbHpEmu1, pbHpEmu2;
    private ImageView imgEmu1, imgEmu2;
    private RelativeLayout layoutArenaEmulador;

    private PokeApiService service;
    private List<PokemonBatalla> equipoJugador = new ArrayList<>();
    private List<PokemonBatalla> equipoRival = new ArrayList<>();

    private List<String> idsJugador;
    private List<String> idsRival;
    private String nombreRival;
    private int nivelActual;
    private int fotoRivalId;

    private int indiceJugador = 0;
    private int indiceRival = 0;
    private boolean batallaTerminada = false;
    private int numeroTurno = 1;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulator);

        idsJugador = getIntent().getStringArrayListExtra("EQUIPO_JUGADOR");
        idsRival = getIntent().getStringArrayListExtra("EQUIPO_RIVAL");
        nombreRival = getIntent().getStringExtra("NOMBRE_RIVAL");
        nivelActual = getIntent().getIntExtra("NIVEL_ACTUAL", 1);
        fotoRivalId = getIntent().getIntExtra("FOTO_RIVAL", R.drawable.icon_pokeapp);

        enlazarVistas();
        service = ApiClient.getClient().create(PokeApiService.class);

        cardSelector.setVisibility(View.GONE);
        layoutArenaEmulador.setVisibility(View.VISIBLE);
        panelAtaques.setVisibility(View.INVISIBLE);

        txtLogEmulador.setText("La batalla contra " + nombreRival + " está por comenzar...\nDescargando datos...");

        btnHuirBatalla.setOnClickListener(v -> {
            Toast.makeText(this, "Huiste acobardado. Progreso reiniciado.", Toast.LENGTH_SHORT).show();
            reiniciarTorre();
            finish();
        });

        descargarRecursivo(0, true);
    }

    private void enlazarVistas() {
        cardSelector = findViewById(R.id.cardSelector);
        panelAtaques = findViewById(R.id.panelAtaques);
        btnAtq1 = findViewById(R.id.btnAtaque1);
        btnAtq2 = findViewById(R.id.btnAtaque2);
        btnAtq3 = findViewById(R.id.btnAtaque3);
        btnAtq4 = findViewById(R.id.btnAtaque4);
        btnHuirBatalla = findViewById(R.id.btnHuirBatalla);

        txtNombreEmu1 = findViewById(R.id.txtNombreEmu1); txtHpNumEmu1 = findViewById(R.id.txtHpNumEmu1);
        txtNombreEmu2 = findViewById(R.id.txtNombreEmu2); txtHpNumEmu2 = findViewById(R.id.txtHpNumEmu2);
        txtLogEmulador = findViewById(R.id.txtLogEmulador);
        pbHpEmu1 = findViewById(R.id.pbHpEmu1); pbHpEmu2 = findViewById(R.id.pbHpEmu2);
        imgEmu1 = findViewById(R.id.imgEmu1); imgEmu2 = findViewById(R.id.imgEmu2);
        layoutArenaEmulador = findViewById(R.id.layoutArenaEmulador);
    }

    private void descargarRecursivo(int index, boolean cargandoJugador) {
        if (!cargandoJugador && index == idsRival.size()) {
            iniciarArena();
            return;
        }

        String pokeBuscado = cargandoJugador ? idsJugador.get(index) : idsRival.get(index);

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

                    int buffNivel = (nivelActual * 5);
                    int hpMax = (hpBase * 3) + (cargandoJugador ? 0 : buffNivel * 2);

                    String spriteFrente = p.getSprites().getFrontDefault();
                    String spriteEspalda = p.getSprites().getBackDefault();
                    if (spriteEspalda == null) spriteEspalda = spriteFrente;

                    PokemonBatalla nuevoPoke = new PokemonBatalla(
                            p.getName().toUpperCase(Locale.ROOT), hpMax,
                            atkBase + (cargandoJugador ? 0 : buffNivel),
                            defBase + (cargandoJugador ? 0 : buffNivel),
                            spdBase + (cargandoJugador ? 0 : buffNivel),
                            spriteFrente, spriteEspalda, tiposPoke
                    );

                    nuevoPoke.setIdAPI(p.getId());
                    nuevoPoke.setMovimientos(generarMovimientos(tiposPoke));

                    if (cargandoJugador) {
                        equipoJugador.add(nuevoPoke);
                        if (index + 1 == idsJugador.size()) descargarRecursivo(0, false);
                        else descargarRecursivo(index + 1, true);
                    } else {
                        equipoRival.add(nuevoPoke);
                        descargarRecursivo(index + 1, false);
                    }
                } else {
                    Toast.makeText(BatallaTorreActivity.this, "Error de red. Saliendo...", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                Toast.makeText(BatallaTorreActivity.this, "Fallo de conexión.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void iniciarArena() {
        txtLogEmulador.setText("¡" + nombreRival + " te desafía a una batalla!");
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
        PokemonBatalla debilitado = atacanteEsJugador ? getPokeRival() : getPokeJugador();
        reproducirGrito(debilitado.getIdAPI());

        if (atacanteEsJugador) {
            indiceRival++;
            if (indiceRival < equipoRival.size()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    txtLogEmulador.setText("¡" + nombreRival + " sacó a " + getPokeRival().getNombre() + "!");
                    actualizarPantallaArena();
                    prepararSiguienteTurno();
                }, 2000);
            } else {
                batallaTerminada = true;
                HistorialHelper.registrar("Torre", "Victoria Nivel " + nivelActual, "Derrotaste a " + nombreRival);
                mostrarDialogoGanador(true);
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
                HistorialHelper.registrar("Torre", "Derrota Nivel " + nivelActual, "Caíste ante " + nombreRival);
                mostrarDialogoGanador(false);
            }
        }
    }

    private void reiniciarTorre() {
        SharedPreferences.Editor editor = getSharedPreferences("TorreCESBA", MODE_PRIVATE).edit();
        editor.putInt("nivel_actual", 1);
        editor.apply();
    }

    // --- LÓGICA DE RECOMPENSAS ACTUALIZADA ---
    private void otorgarRecompensas(boolean gano) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid());
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Integer xp = snapshot.child("xp").getValue(Integer.class);
                    Integer medallas = snapshot.child("medallas").getValue(Integer.class);

                    if (xp == null) xp = 0;
                    if (medallas == null) medallas = 0;

                    int xpGanada;
                    if (gano) {
                        if (nivelActual == 8) {
                            xpGanada = 200; // Jefe Final
                        } else {
                            xpGanada = 50; // Nivel Normal
                        }

                        // ¡CADA PROFESOR ES UN LÍDER DE GIMNASIO!
                        // Te da 1 medalla hasta llegar a las 8 máximas.
                        if (medallas < 8) {
                            ref.child("medallas").setValue(medallas + 1);
                        }
                    } else {
                        xpGanada = 10; // Premio de consuelo por perder
                    }

                    int nuevaXp = xp + xpGanada;
                    int nuevoNivel = (nuevaXp / 100) + 1;

                    ref.child("xp").setValue(nuevaXp);
                    ref.child("nivel").setValue(nuevoNivel);
                }
                @Override public void onCancelled(@NonNull DatabaseError error) {}
            });
        }
    }

    private void mostrarDialogoGanador(boolean jugadorGano) {
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

        btnRevancha.setVisibility(View.GONE);
        btnNuevoEquipo.setVisibility(View.GONE);
        btnVolverMenu.setText("Continuar");

        // ENTREGAMOS LA XP Y LA MEDALLA A FIREBASE
        otorgarRecompensas(jugadorGano);

        if(jugadorGano) {
            Glide.with(this).load(getPokeJugador().getSpriteFrente()).into(imgGanador);
            txtTitulo.setTextColor(Color.parseColor("#4CAF50"));

            if (nivelActual == 8) {
                txtNombre.setText("¡LIGA CONQUISTADA!");
                txtStats.setText("¡Has derrotado a la Directora Myrna!\nGanaste 200 XP y la 8va Medalla 🏅.");
                reiniciarTorre();
            } else {
                txtNombre.setText("¡NIVEL " + nivelActual + " SUPERADO!");
                txtStats.setText("Has derrotado a " + nombreRival + ".\nGanaste 50 XP y una Medalla 🏅.");

                SharedPreferences.Editor editor = getSharedPreferences("TorreCESBA", MODE_PRIVATE).edit();
                editor.putInt("nivel_actual", nivelActual + 1);
                editor.apply();
            }
        } else {
            Glide.with(this).load(fotoRivalId).into(imgGanador);
            txtTitulo.setTextColor(Color.parseColor("#F44336"));
            txtNombre.setText("¡DERROTA!");
            txtStats.setText(nombreRival + " te ha vencido.\nGanaste 10 XP por el esfuerzo, pero la torre se reinicia.");
            reiniciarTorre();
        }

        btnVolverMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void reproducirGrito(int id) {
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/" + id + ".ogg");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) return;
                InputStream input = conn.getInputStream();
                File temp = new File(getCacheDir(), "grito_torre.ogg");
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
                    } catch (Exception e) {}
                });
            } catch (Exception e) {}
        }).start();
    }

    private void animarAtaque(ImageView img, boolean esP1) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(img, "translationX", 0f, esP1 ? 50f : -50f, 0f);
        anim.setDuration(200); anim.start();
    }

    private void animarDanoYParpadeo(ImageView imgDefensor) {
        imgDefensor.setColorFilter(Color.parseColor("#99FF0000"), PorterDuff.Mode.SRC_ATOP);
        imgDefensor.postDelayed(imgDefensor::clearColorFilter, 150);
        imgDefensor.postDelayed(() -> imgDefensor.setColorFilter(Color.parseColor("#99FF0000"), PorterDuff.Mode.SRC_ATOP), 300);
        imgDefensor.postDelayed(imgDefensor::clearColorFilter, 450);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
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