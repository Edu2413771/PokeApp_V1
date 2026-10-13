package com.example.pokeapp;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MenuActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageButton btnMenuHamburguesa, btnPerfilUsuario;
    private TextView txtHolaUsuario, txtNombreAleatorio, txtTrainerId;
    private ImageView imgPokemonAleatorio;

    private TextView txtNivelEntrenador, txtMedallas;
    private ProgressBar pbExperiencia;

    private Button btnPokedex, btnEmulator, btnVersus, btnTorre, btnQuienEs;
    private Button btnJefes, btnMaestro, btnMemory, btnFavoritos, btnHistorial, btnMedallasMenu;

    private PokeApiService service;
    private FirebaseAuth mAuth;
    private DatabaseReference dbRefPvP;
    private DatabaseReference dbRefEntrenadores;

    private String miIDLimpio = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        mAuth = FirebaseAuth.getInstance();
        service = ApiClient.getClient().create(PokeApiService.class);
        dbRefPvP = FirebaseDatabase.getInstance().getReference("PvP_Batallas");
        dbRefEntrenadores = FirebaseDatabase.getInstance().getReference("Entrenadores");

        enlazarVistas();
        configurarMenuSuperior();
        configurarBotonesPanal();

        cargarPokemonAleatorio();
        conectarConPerfilFirebaseYEscucharRetos();
    }

    private void enlazarVistas() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        btnMenuHamburguesa = findViewById(R.id.btnMenuHamburguesa);
        btnPerfilUsuario = findViewById(R.id.btnPerfilUsuario);
        txtHolaUsuario = findViewById(R.id.txtHolaUsuario);
        txtTrainerId = findViewById(R.id.txtTrainerId);

        imgPokemonAleatorio = findViewById(R.id.imgPokemonAleatorio);
        txtNombreAleatorio = findViewById(R.id.txtNombreAleatorio);

        txtNivelEntrenador = findViewById(R.id.txtNivelEntrenador);
        txtMedallas = findViewById(R.id.txtMedallas);
        pbExperiencia = findViewById(R.id.pbExperiencia);

        btnPokedex = findViewById(R.id.btnPokedex);
        btnEmulator = findViewById(R.id.btnEmulator);
        btnVersus = findViewById(R.id.btnVersus);
        btnTorre = findViewById(R.id.btnTorre);
        btnQuienEs = findViewById(R.id.btnQuienEs);
        btnJefes = findViewById(R.id.btnJefes);
        btnMaestro = findViewById(R.id.btnMaestro);
        btnMemory = findViewById(R.id.btnMemory);
        btnFavoritos = findViewById(R.id.btnFavoritos);
        btnHistorial = findViewById(R.id.btnHistorial);
        btnMedallasMenu = findViewById(R.id.btnMedallas);
    }

    private String limpiarID(String idOriginal) {
        if (idOriginal == null) return "";
        return idOriginal.replace("#", "").trim();
    }

    private void conectarConPerfilFirebaseYEscucharRetos() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            DatabaseReference ref = dbRefEntrenadores.child(currentUser.getUid());

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String idEntrenador = snapshot.child("trainerId").getValue(String.class);
                        Integer nivel = snapshot.child("nivel").getValue(Integer.class);
                        Integer xp = snapshot.child("xp").getValue(Integer.class);
                        Integer medallas = snapshot.child("medallas").getValue(Integer.class);

                        if(nivel == null) nivel = 1;
                        if(xp == null) xp = 0;
                        if(medallas == null) medallas = 0;

                        txtTrainerId.setText("ID: " + idEntrenador);

                        miIDLimpio = limpiarID(idEntrenador);
                        iniciarEscuchaDeRetos();

                        String rango = (nivel < 5) ? "Novato" : (nivel < 10) ? "Experimentado" : "Maestro";
                        txtNivelEntrenador.setText("Nivel " + nivel + " - Entrenador " + rango);
                        txtMedallas.setText(medallas + " / 8 Medallas");
                        pbExperiencia.setProgress(xp % 100);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MenuActivity.this, "Error al cargar perfil de Firebase", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void iniciarEscuchaDeRetos() {
        if (miIDLimpio.isEmpty()) return;

        dbRefPvP.child(miIDLimpio).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && "pendiente".equals(snapshot.child("estado").getValue(String.class))) {
                    String idRetador = snapshot.child("retador").getValue(String.class);
                    String nombreRetador = snapshot.child("nombreRetador").getValue(String.class);
                    Integer modo = snapshot.child("modo").getValue(Integer.class);

                    if(nombreRetador == null) nombreRetador = "Un Entrenador";
                    if(modo == null) modo = 1;

                    mostrarDialogoReto(idRetador, nombreRetador, modo);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // EL NUEVO DIÁLOGO PERSONALIZADO CON ESTILO
    private void mostrarDialogoReto(String idRetador, String nombreRetador, int modo) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_reto);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.setCancelable(false);

        TextView txtMensaje = dialog.findViewById(R.id.txtMensajeReto);
        Button btnAceptar = dialog.findViewById(R.id.btnAceptarReto);
        Button btnRechazar = dialog.findViewById(R.id.btnRechazarReto);

        txtMensaje.setText("¡El Entrenador " + nombreRetador.toUpperCase() + " te ha desafiado a un combate " + modo + " vs " + modo + "!\n\n¿Estás listo?");

        btnAceptar.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MenuActivity.this, PvPActivity.class);
            intent.putExtra("ES_RETADO", true);
            intent.putExtra("ID_RETADOR", idRetador);
            intent.putExtra("MODO_BATALLA", modo);
            startActivity(intent);
        });

        btnRechazar.setOnClickListener(v -> {
            dbRefPvP.child(miIDLimpio).removeValue();
            dialog.dismiss();
        });

        dialog.show();
    }


    private void configurarMenuSuperior() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && currentUser.getEmail() != null) {
            String correo = currentUser.getEmail();
            String nombreUsuario = correo.split("@")[0];
            txtHolaUsuario.setText("Hola " + nombreUsuario);
        }

        btnMenuHamburguesa.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        btnPerfilUsuario.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, PerfilActivity.class)));

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_cerrar_sesion) {
                mAuth.signOut();
                startActivity(new Intent(MenuActivity.this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void configurarBotonesPanal() {
        asignarColorHexagono(btnPokedex, "#FFCDD2");
        asignarColorHexagono(btnQuienEs, "#BBDEFB");
        asignarColorHexagono(btnMaestro, "#E1BEE7");
        asignarColorHexagono(btnJefes, "#FFCCBC");
        asignarColorHexagono(btnFavoritos, "#FFF9C4");
        asignarColorHexagono(btnMedallasMenu, "#C8E6C9");
        asignarColorHexagono(btnVersus, "#FFECB3");
        asignarColorHexagono(btnTorre, "#B2DFDB");
        asignarColorHexagono(btnEmulator, "#D1C4E9");
        asignarColorHexagono(btnMemory, "#F8BBD0");
        asignarColorHexagono(btnHistorial, "#CFD8DC");

        btnPokedex.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, MainActivity.class)));
        btnQuienEs.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, QuienEsActivity.class)));
        btnMaestro.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, MaestroTiposActivity.class)));
        btnFavoritos.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, FavoritosActivity.class)));
        btnMemory.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, MemoryActivity.class)));
        btnHistorial.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, HistorialActivity.class)));
        btnVersus.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, PvPActivity.class)));
        btnEmulator.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, EmulatorActivity.class)));
    }

    private void asignarColorHexagono(Button boton, String colorHex) {
        Drawable background = boton.getBackground();
        if (background instanceof LayerDrawable) {
            LayerDrawable layerDrawable = (LayerDrawable) background;
            Drawable fondo = layerDrawable.findDrawableByLayerId(android.R.id.background);
            if (fondo != null) {
                fondo.mutate().setTint(Color.parseColor(colorHex));
            }
        }
    }

    private void cargarPokemonAleatorio() {
        int idAleatorio = new Random().nextInt(151) + 1;
        Call<Pokemon> call = service.getPokemon(String.valueOf(idAleatorio));
        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Pokemon p = response.body();
                    txtNombreAleatorio.setText(p.getName());
                    if (p.getSprites() != null) {
                        Glide.with(MenuActivity.this).load(p.getSprites().getFrontDefault()).into(imgPokemonAleatorio);
                    }
                }
            }
            @Override public void onFailure(Call<Pokemon> call, Throwable t) {}
        });
    }
}