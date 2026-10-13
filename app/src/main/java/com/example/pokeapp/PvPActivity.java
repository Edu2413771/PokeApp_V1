package com.example.pokeapp;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PvPActivity extends AppCompatActivity {

    private CardView cardSelector;
    private RadioGroup rgModoBatalla;
    private LinearLayout layoutFila2, layoutFila3, panelAtaques, layoutEsperandoRival;
    private EditText etP1_1, etP1_2, etP1_3, etIdRivalPvP;
    private Button btnDesafiar, btnSalirSelector, btnAtq1, btnAtq2, btnAtq3, btnAtq4, btnHuirBatalla;
    private TextView txtNombreEmu1, txtHpNumEmu1, txtNombreEmu2, txtHpNumEmu2, txtLog, txtTimer, txtEstadoMatchmaking;
    private ProgressBar pbHpEmu1, pbHpEmu2;
    private ImageView imgEmu1, imgEmu2;
    private RelativeLayout layoutArena;

    private PokeApiService service;
    private DatabaseReference dbRefPvP;
    private DatabaseReference dbRefEntrenadores;
    private FirebaseAuth mAuth;

    private String miID = "";
    private String miNombreUsuario = "Entrenador";
    private String idSalaActual = "";
    private boolean soyRetador = true;

    private boolean vengoComoRetado = false;
    private String idDelQueMeReto = "";
    private String misPokesString = "";

    private List<PokemonBatalla> miEquipo = new ArrayList<>();
    private List<PokemonBatalla> equipoRival = new ArrayList<>();

    private int miIndice = 0;
    private int rivalIndice = 0;
    private int modoSeleccionado = 1;
    private CountDownTimer timerBatalla;
    private boolean batallaTerminada = false;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pvp);

        try {
            mAuth = FirebaseAuth.getInstance();
            service = ApiClient.getClient().create(PokeApiService.class);
            dbRefPvP = FirebaseDatabase.getInstance().getReference("PvP_Batallas");
            dbRefEntrenadores = FirebaseDatabase.getInstance().getReference("Entrenadores");

            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().getEmail() != null) {
                miNombreUsuario = mAuth.getCurrentUser().getEmail().split("@")[0];
            }

            enlazarVistas();

            vengoComoRetado = getIntent().getBooleanExtra("ES_RETADO", false);
            if (vengoComoRetado) {
                idDelQueMeReto = getIntent().getStringExtra("ID_RETADOR");
                modoSeleccionado = getIntent().getIntExtra("MODO_BATALLA", 1);
                prepararVistaParaRetado();
            }

            rgModoBatalla.setOnCheckedChangeListener((group, checkedId) -> {
                if (!vengoComoRetado) {
                    if (checkedId == R.id.rb1v1) { modoSeleccionado = 1; layoutFila2.setVisibility(View.GONE); layoutFila3.setVisibility(View.GONE); }
                    else if (checkedId == R.id.rb2v2) { modoSeleccionado = 2; layoutFila2.setVisibility(View.VISIBLE); layoutFila3.setVisibility(View.GONE); }
                    else if (checkedId == R.id.rb3v3) { modoSeleccionado = 3; layoutFila2.setVisibility(View.VISIBLE); layoutFila3.setVisibility(View.VISIBLE); }
                }
            });

            btnDesafiar.setOnClickListener(v -> prepararMiEquipo());
            btnSalirSelector.setOnClickListener(v -> finish());
            btnHuirBatalla.setOnClickListener(v -> terminarBatalla("Huiste del combate.", false));

            btnDesafiar.setEnabled(false);
            if (!vengoComoRetado) btnDesafiar.setText("Cargando ID...");
            obtenerMiTrainerId();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void enlazarVistas() {
        cardSelector = findViewById(R.id.cardSelectorPvP);
        rgModoBatalla = findViewById(R.id.rgModoBatalla);
        layoutFila2 = findViewById(R.id.layoutFila2);
        layoutFila3 = findViewById(R.id.layoutFila3);
        layoutEsperandoRival = findViewById(R.id.layoutEsperandoRival);
        txtEstadoMatchmaking = findViewById(R.id.txtEstadoMatchmaking);

        etP1_1 = findViewById(R.id.etP1_1);
        etP1_2 = findViewById(R.id.etP1_2);
        etP1_3 = findViewById(R.id.etP1_3);
        etIdRivalPvP = findViewById(R.id.etIdRivalPvP);

        btnDesafiar = findViewById(R.id.btnDesafiarPvP);
        btnSalirSelector = findViewById(R.id.btnSalirPvP);
        btnHuirBatalla = findViewById(R.id.btnRendirsePvP);

        panelAtaques = findViewById(R.id.panelAtaquesPvP);
        btnAtq1 = findViewById(R.id.btnAtq1PvP); btnAtq2 = findViewById(R.id.btnAtq2PvP);
        btnAtq3 = findViewById(R.id.btnAtq3PvP); btnAtq4 = findViewById(R.id.btnAtq4PvP);

        txtNombreEmu1 = findViewById(R.id.txtNombreJugadorPvP); txtHpNumEmu1 = findViewById(R.id.txtHpNumJugadorPvP);
        txtNombreEmu2 = findViewById(R.id.txtNombreRivalPvP); txtHpNumEmu2 = findViewById(R.id.txtHpNumRivalPvP);
        txtLog = findViewById(R.id.txtLogPvP);
        txtTimer = findViewById(R.id.txtTimerPvP);

        pbHpEmu1 = findViewById(R.id.pbHpJugadorPvP); pbHpEmu2 = findViewById(R.id.pbHpRivalPvP);
        imgEmu1 = findViewById(R.id.imgJugadorPvP); imgEmu2 = findViewById(R.id.imgRivalPvP);
        layoutArena = findViewById(R.id.layoutArenaPvP);
    }

    private void prepararVistaParaRetado() {
        soyRetador = false;
        etIdRivalPvP.setVisibility(View.GONE);
        rgModoBatalla.setVisibility(View.GONE);

        if (modoSeleccionado == 1) { layoutFila2.setVisibility(View.GONE); layoutFila3.setVisibility(View.GONE); }
        else if (modoSeleccionado == 2) { layoutFila2.setVisibility(View.VISIBLE); layoutFila3.setVisibility(View.GONE); }
        else if (modoSeleccionado == 3) { layoutFila2.setVisibility(View.VISIBLE); layoutFila3.setVisibility(View.VISIBLE); }

        btnDesafiar.setText("¡Aceptar y Luchar!");
    }

    private String limpiarID(String idOriginal) {
        if (idOriginal == null) return "";
        return idOriginal.replace("#", "").trim();
    }

    private void obtenerMiTrainerId() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            dbRefEntrenadores.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists() && snapshot.hasChild("trainerId")) {
                        String idBruto = snapshot.child("trainerId").getValue(String.class);
                        miID = limpiarID(idBruto);

                        btnDesafiar.setEnabled(true);
                        if (!vengoComoRetado) btnDesafiar.setText("Lanzar Desafío");
                    } else {
                        Toast.makeText(PvPActivity.this, "No se encontró tu Trainer ID", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) { finish(); }
            });
        } else { finish(); }
    }

    private void prepararMiEquipo() {
        String idRival = "";

        if (soyRetador) {
            idRival = limpiarID(etIdRivalPvP.getText().toString());
            if (idRival.isEmpty()) { Toast.makeText(this, "Ingresa el ID del rival", Toast.LENGTH_SHORT).show(); return; }
        } else {
            idRival = idDelQueMeReto;
        }

        List<String> idsPokes = new ArrayList<>();
        idsPokes.add(etP1_1.getText().toString().trim().toLowerCase());
        if (modoSeleccionado >= 2) idsPokes.add(etP1_2.getText().toString().trim().toLowerCase());
        if (modoSeleccionado == 3) idsPokes.add(etP1_3.getText().toString().trim().toLowerCase());

        for (String s : idsPokes) if (s.isEmpty()) { Toast.makeText(this, "Llena tu equipo", Toast.LENGTH_SHORT).show(); return; }

        misPokesString = TextUtils.join(",", idsPokes);
        miEquipo.clear();
        btnDesafiar.setText("Preparando...");
        btnDesafiar.setEnabled(false);

        descargarMiEquipoAPI(idsPokes, 0, idRival);
    }

    private void descargarMiEquipoAPI(List<String> lista, int index, String idRival) {
        if (index == lista.size()) {
            if (soyRetador) lanzarDesafioFirebase(idRival);
            else aceptarDesafioFirebase();
            return;
        }

        service.getPokemon(lista.get(index)).enqueue(new Callback<Pokemon>() {
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
                    int hp50 = hpBase + 60, atk50 = atkBase + 20, def50 = defBase + 20, spd50 = spdBase + 20;

                    List<String> tiposPoke = new ArrayList<>();
                    if (p.getTypes() != null) for (Pokemon.TypeSlot ts : p.getTypes()) tiposPoke.add(ts.getType().getName());
                    String spriteF = p.getSprites().getFrontDefault(), spriteE = p.getSprites().getBackDefault() != null ? p.getSprites().getBackDefault() : spriteF;

                    PokemonBatalla nuevoPoke = new PokemonBatalla(p.getName().toUpperCase(), hp50, atk50, def50, spd50, spriteF, spriteE, tiposPoke);
                    nuevoPoke.setIdAPI(p.getId());
                    nuevoPoke.setMovimientos(generarMovimientos(tiposPoke));
                    miEquipo.add(nuevoPoke);

                    descargarMiEquipoAPI(lista, index + 1, idRival);
                } else {
                    Toast.makeText(PvPActivity.this, "Pokémon no encontrado", Toast.LENGTH_SHORT).show();
                    btnDesafiar.setText(soyRetador ? "Lanzar Desafío" : "¡Aceptar y Luchar!");
                    btnDesafiar.setEnabled(true);
                }
            }
            @Override public void onFailure(Call<Pokemon> call, Throwable t) {}
        });
    }

    private void lanzarDesafioFirebase(String idRival) {
        idSalaActual = idRival;
        dbRefPvP.child(idSalaActual).child("retador").setValue(miID);
        dbRefPvP.child(idSalaActual).child("nombreRetador").setValue(miNombreUsuario);
        dbRefPvP.child(idSalaActual).child("equipo_retador").setValue(misPokesString);
        dbRefPvP.child(idSalaActual).child("modo").setValue(modoSeleccionado);
        dbRefPvP.child(idSalaActual).child("estado").setValue("pendiente");

        txtEstadoMatchmaking.setText("Esperando a que el rival acepte...");
        layoutEsperandoRival.setVisibility(View.VISIBLE);

        dbRefPvP.child(idSalaActual).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && "aceptado".equals(snapshot.child("estado").getValue(String.class))) {
                    dbRefPvP.child(idSalaActual).removeEventListener(this);
                    String pokesRivalStr = snapshot.child("equipo_retado").getValue(String.class);
                    empezarDescargaRival(pokesRivalStr);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void aceptarDesafioFirebase() {
        idSalaActual = miID;
        dbRefPvP.child(idSalaActual).child("equipo_retado").setValue(misPokesString);
        dbRefPvP.child(idSalaActual).child("estado").setValue("aceptado");

        dbRefPvP.child(idSalaActual).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String pokesRivalStr = snapshot.child("equipo_retador").getValue(String.class);
                empezarDescargaRival(pokesRivalStr);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void empezarDescargaRival(String equipoStr) {
        if (equipoStr == null || equipoStr.isEmpty()) return;
        String[] pokes = equipoStr.split(",");
        List<String> listaRival = new ArrayList<>();
        for (String p : pokes) listaRival.add(p);

        equipoRival.clear();
        txtEstadoMatchmaking.setText("¡Rival encontrado!\nPreparando la arena...");
        layoutEsperandoRival.setVisibility(View.VISIBLE);
        descargarEquipoRivalAPI(listaRival, 0);
    }

    private void descargarEquipoRivalAPI(List<String> lista, int index) {
        if (index == lista.size()) {
            iniciarArenaPvP();
            return;
        }

        service.getPokemon(lista.get(index)).enqueue(new Callback<Pokemon>() {
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
                    int hp50 = hpBase + 60, atk50 = atkBase + 20, def50 = defBase + 20, spd50 = spdBase + 20;

                    List<String> tiposPoke = new ArrayList<>();
                    if (p.getTypes() != null) for (Pokemon.TypeSlot ts : p.getTypes()) tiposPoke.add(ts.getType().getName());
                    String spriteF = p.getSprites().getFrontDefault(), spriteE = p.getSprites().getBackDefault() != null ? p.getSprites().getBackDefault() : spriteF;

                    PokemonBatalla nuevoPokeRival = new PokemonBatalla(p.getName().toUpperCase(), hp50, atk50, def50, spd50, spriteF, spriteE, tiposPoke);
                    nuevoPokeRival.setIdAPI(p.getId());
                    nuevoPokeRival.setMovimientos(generarMovimientos(tiposPoke));
                    equipoRival.add(nuevoPokeRival);

                    descargarEquipoRivalAPI(lista, index + 1);
                }
            }
            @Override public void onFailure(Call<Pokemon> call, Throwable t) {}
        });
    }

    private void iniciarArenaPvP() {
        layoutEsperandoRival.setVisibility(View.GONE);
        cardSelector.setVisibility(View.GONE);
        layoutArena.setVisibility(View.VISIBLE);

        actualizarPantallaArena();
        iniciarReloj();
        txtLog.setText("¡La batalla multijugador ha comenzado!\nElige tu ataque.");
        panelAtaques.setVisibility(View.VISIBLE);

        dbRefPvP.child(idSalaActual).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (batallaTerminada) return;

                if (snapshot.hasChild("atk_retador") && snapshot.hasChild("atk_retado")) {
                    int aRetador = snapshot.child("atk_retador").getValue(Integer.class);
                    int aRetado = snapshot.child("atk_retado").getValue(Integer.class);

                    if (soyRetador) {
                        dbRefPvP.child(idSalaActual).child("atk_retador").removeValue();
                        dbRefPvP.child(idSalaActual).child("atk_retado").removeValue();
                    }

                    Movimiento miMov = soyRetador ? miEquipo.get(miIndice).getMovimientos()[aRetador] : miEquipo.get(miIndice).getMovimientos()[aRetado];
                    Movimiento suMov = soyRetador ? equipoRival.get(rivalIndice).getMovimientos()[aRetado] : equipoRival.get(rivalIndice).getMovimientos()[aRetador];

                    resolverTurnoSincrono(miMov, suMov);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void iniciarReloj() {
        timerBatalla = new CountDownTimer(420000, 1000) {
            public void onTick(long millisUntilFinished) {
                int minutos = (int) (millisUntilFinished / 1000) / 60;
                int segundos = (int) (millisUntilFinished / 1000) % 60;
                txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos));
            }
            public void onFinish() {
                txtTimer.setText("00:00");
                terminarBatalla("¡Se acabó el tiempo!", false);
            }
        }.start();
    }

    private void actualizarPantallaArena() {
        if (batallaTerminada) return;

        PokemonBatalla miPoke = miEquipo.get(miIndice);
        PokemonBatalla suPoke = equipoRival.get(rivalIndice);

        txtNombreEmu1.setText(miPoke.getNombre());
        pbHpEmu1.setMax(miPoke.getHpMax()); pbHpEmu1.setProgress(miPoke.getHpActual());
        txtHpNumEmu1.setText(miPoke.getHpActual() + "/" + miPoke.getHpMax());
        Glide.with(this).load(miPoke.getSpriteEspalda()).into(imgEmu1);

        txtNombreEmu2.setText(suPoke.getNombre());
        pbHpEmu2.setMax(suPoke.getHpMax()); pbHpEmu2.setProgress(suPoke.getHpActual());
        txtHpNumEmu2.setText(suPoke.getHpActual() + "/" + suPoke.getHpMax());
        Glide.with(this).load(suPoke.getSpriteFrente()).into(imgEmu2);

        configurarBotonesAtaque();
        reproducirGrito(miPoke.getIdAPI());
    }

    private void configurarBotonesAtaque() {
        Movimiento[] movs = miEquipo.get(miIndice).getMovimientos();
        configurarBotonIndividual(btnAtq1, movs[0], 0);
        configurarBotonIndividual(btnAtq2, movs[1], 1);
        configurarBotonIndividual(btnAtq3, movs[2], 2);
        configurarBotonIndividual(btnAtq4, movs[3], 3);
    }

    private void configurarBotonIndividual(Button btn, Movimiento mov, int indexDelAtaque) {
        btn.setText(mov.getNombre() + "\n(" + mov.getPoder() + ")");
        btn.setBackgroundColor(Color.parseColor(obtenerColorTipo(mov.getTipo())));
        btn.setOnClickListener(v -> enviarAtaqueFirebase(indexDelAtaque));
    }

    private void enviarAtaqueFirebase(int indexAtaque) {
        if (batallaTerminada) return;
        panelAtaques.setVisibility(View.INVISIBLE);
        txtLog.setText("Esperando a que el rival elija...");

        dbRefPvP.child(idSalaActual).child(soyRetador ? "atk_retador" : "atk_retado").setValue(indexAtaque);
    }

    private void resolverTurnoSincrono(Movimiento miMov, Movimiento suMov) {
        if (batallaTerminada) return;

        PokemonBatalla miPoke = miEquipo.get(miIndice);
        PokemonBatalla suPoke = equipoRival.get(rivalIndice);

        if (miPoke.getVelocidad() >= suPoke.getVelocidad()) {
            boolean rivalSobrevive = ejecutarGolpe(miPoke, suPoke, miMov, true);
            if (rivalSobrevive) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!batallaTerminada) ejecutarGolpe(suPoke, miPoke, suMov, false);
                }, 2000);
            }
        } else {
            boolean yoSobrevivo = ejecutarGolpe(suPoke, miPoke, suMov, false);
            if (yoSobrevivo) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!batallaTerminada) ejecutarGolpe(miPoke, suPoke, miMov, true);
                }, 2000);
            }
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!batallaTerminada) {
                panelAtaques.setVisibility(View.VISIBLE);
                txtLog.setText("¿Qué vas a hacer ahora?");
            }
        }, 4000);
    }

    private boolean ejecutarGolpe(PokemonBatalla atacante, PokemonBatalla defensor, Movimiento mov, boolean atacanteEsJugador) {
        if (batallaTerminada) return false;

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

        txtLog.setText(atacante.getNombre() + " usó " + mov.getNombre() + ".\nCausó " + dano + " de daño.");

        if (!defensor.estaVivo()) {
            manejarDebilitamiento(atacanteEsJugador);
            return false;
        }
        return true;
    }

    private void manejarDebilitamiento(boolean atacanteEsJugador) {
        if (batallaTerminada) return;

        if (atacanteEsJugador) {
            rivalIndice++;
            if (rivalIndice < equipoRival.size()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!batallaTerminada) {
                        txtLog.setText("¡El rival sacó a " + equipoRival.get(rivalIndice).getNombre() + "!");
                        actualizarPantallaArena();
                    }
                }, 2000);
            } else {
                terminarBatalla("¡VICTORIA MULTIJUGADOR!", true);
            }
        } else {
            miIndice++;
            if (miIndice < miEquipo.size()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (!batallaTerminada) {
                        txtLog.setText("¡Ve, " + miEquipo.get(miIndice).getNombre() + "!");
                        actualizarPantallaArena();
                    }
                }, 2000);
            } else {
                terminarBatalla("DERROTA...", false);
            }
        }
    }

    private void terminarBatalla(String mensajeFinal, boolean gane) {
        if (batallaTerminada) return;
        batallaTerminada = true;

        if (timerBatalla != null) timerBatalla.cancel();

        if(!soyRetador) {
            dbRefPvP.child(idSalaActual).removeValue();
        }

        mostrarDialogoGanador(gane);
    }

    // =======================================================
    // NUEVO: EL DIÁLOGO DEL GANADOR RECICLADO
    // =======================================================
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

        // Ocultamos la revancha porque en PvP requiere reconectar Firebase,
        // lo más seguro es regresar a la sala o menú.
        btnRevancha.setVisibility(View.GONE);
        btnNuevoEquipo.setText("Volver a la Sala");

        // Obtenemos al último Pokémon en pie para mostrar su foto
        int indexMi = Math.min(miIndice, miEquipo.size() - 1);
        int indexSu = Math.min(rivalIndice, equipoRival.size() - 1);

        PokemonBatalla miPoke = miEquipo.isEmpty() ? null : miEquipo.get(indexMi);
        PokemonBatalla suPoke = equipoRival.isEmpty() ? null : equipoRival.get(indexSu);

        if(jugadorGano) {
            txtTitulo.setTextColor(Color.parseColor("#4CAF50"));
            txtNombre.setText("¡VICTORIA!");
            if(miPoke != null) Glide.with(this).load(miPoke.getSpriteFrente()).into(imgGanador);
        } else {
            txtTitulo.setTextColor(Color.parseColor("#F44336"));
            txtNombre.setText("¡DERROTA!");
            if(suPoke != null) Glide.with(this).load(suPoke.getSpriteFrente()).into(imgGanador);
        }

        txtStats.setText("Combate PvP " + modoSeleccionado + "vs" + modoSeleccionado + " finalizado.");

        btnNuevoEquipo.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // Sale al menú o sala anterior para volver a invitar
        });

        btnVolverMenu.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
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
                    } catch (Exception e) { }
                });
            } catch (Exception e) { }
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

    private Movimiento[] generarMovimientos(List<String> tipos) {
        Movimiento[] movs = new Movimiento[4];
        String tipoP = tipos.get(0);
        String tipoS = tipos.size() > 1 ? tipos.get(1) : "normal";

        if (tipos.size() == 1) {
            tipoS = obtenerTipoCobertura(tipoP);
        }

        movs[0] = obtenerAtaqueBase(tipoP, true);
        movs[1] = obtenerAtaqueBase(tipoP, false);
        movs[2] = obtenerAtaqueBase(tipoS, true);
        movs[3] = obtenerAtaqueBase(obtenerTipoCobertura(tipoS), false);
        return movs;
    }

    private String obtenerTipoCobertura(String tipo) {
        switch (tipo) {
            case "water": return "ice";
            case "fire": return "ground";
            case "grass": return "poison";
            case "electric": return "steel";
            case "flying": return "dragon";
            case "psychic": return "ghost";
            case "ground": return "rock";
            default: return "fighting";
        }
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