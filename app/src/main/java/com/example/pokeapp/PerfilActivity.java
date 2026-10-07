package com.example.pokeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PerfilActivity extends AppCompatActivity {

    private TextView txtNombrePerfil, txtIdPerfil, txtNivelPerfil, txtMedallasPerfil, txtXpExacta;
    private ProgressBar pbXpPerfil;
    private Button btnVolverPerfil;

    private FirebaseAuth mAuth;
    private DatabaseReference perfilRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        enlazarVistas();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Extraer nombre del correo
            if (user.getEmail() != null) {
                String nombreUsuario = user.getEmail().split("@")[0].toUpperCase();
                txtNombrePerfil.setText(nombreUsuario);
            }

            perfilRef = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid());
            cargarDatosPerfil();
        }

        btnVolverPerfil.setOnClickListener(v -> finish());
    }

    private void enlazarVistas() {
        txtNombrePerfil = findViewById(R.id.txtNombrePerfil);
        txtIdPerfil = findViewById(R.id.txtIdPerfil);
        txtNivelPerfil = findViewById(R.id.txtNivelPerfil);
        txtMedallasPerfil = findViewById(R.id.txtMedallasPerfil);
        txtXpExacta = findViewById(R.id.txtXpExacta);
        pbXpPerfil = findViewById(R.id.pbXpPerfil);
        btnVolverPerfil = findViewById(R.id.btnVolverPerfil);
    }

    private void cargarDatosPerfil() {
        perfilRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String idEntrenador = snapshot.child("trainerId").getValue(String.class);
                    Integer nivel = snapshot.child("nivel").getValue(Integer.class);
                    Integer xpTotal = snapshot.child("xp").getValue(Integer.class);
                    Integer medallas = snapshot.child("medallas").getValue(Integer.class);

                    if(nivel == null) nivel = 1;
                    if(xpTotal == null) xpTotal = 0;
                    if(medallas == null) medallas = 0;

                    // Mostrar ID
                    txtIdPerfil.setText(idEntrenador);

                    // Sistema de Títulos
                    String titulo = "Novato";
                    if (nivel >= 5) titulo = "Promesa";
                    if (nivel >= 10) titulo = "Líder";
                    if (nivel >= 20) titulo = "Maestro";
                    if (nivel >= 50) titulo = "Campeón";

                    txtNivelPerfil.setText("Nivel " + nivel + " (" + titulo + ")");
                    txtMedallasPerfil.setText("🏅 " + medallas);

                    // Calcular XP de la barra
                    int xpActualNivel = xpTotal % 100;
                    pbXpPerfil.setProgress(xpActualNivel);
                    txtXpExacta.setText(xpActualNivel + " / 100 XP");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PerfilActivity.this, "Error al cargar perfil", Toast.LENGTH_SHORT).show();
            }
        });
    }
}