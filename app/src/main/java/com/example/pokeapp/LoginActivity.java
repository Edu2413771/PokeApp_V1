package com.example.pokeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnIniciarSesion, btnRegistrar;
    private TextView txtRecuperarPass, tabLogin, tabRegistro;
    private LinearLayout layoutLoginActions, layoutRegistroActions;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtRecuperarPass = findViewById(R.id.txtRecuperarPass);

        tabLogin = findViewById(R.id.tabLogin);
        tabRegistro = findViewById(R.id.tabRegistro);
        layoutLoginActions = findViewById(R.id.layoutLoginActions);
        layoutRegistroActions = findViewById(R.id.layoutRegistroActions);

        tabLogin.setOnClickListener(v -> {
            layoutLoginActions.setVisibility(View.VISIBLE);
            layoutRegistroActions.setVisibility(View.GONE);
            tabLogin.setTextColor(Color.parseColor("#4CAF50"));
            tabLogin.setTypeface(null, Typeface.BOLD);
            tabRegistro.setTextColor(Color.parseColor("#888888"));
            tabRegistro.setTypeface(null, Typeface.NORMAL);
        });

        tabRegistro.setOnClickListener(v -> {
            layoutLoginActions.setVisibility(View.GONE);
            layoutRegistroActions.setVisibility(View.VISIBLE);
            tabRegistro.setTextColor(Color.parseColor("#FFCB05"));
            tabRegistro.setTypeface(null, Typeface.BOLD);
            tabLogin.setTextColor(Color.parseColor("#888888"));
            tabLogin.setTypeface(null, Typeface.NORMAL);
        });

        btnIniciarSesion.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            irAlMenuPrincipal();
                        } else {
                            Toast.makeText(this, "Error: Revisa tus credenciales", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // ==========================================
        // LA FASE 1: CREAR PERFIL EN BASE DE DATOS
        // ==========================================
        btnRegistrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Llena los campos para crear cuenta", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Generamos un ID como en los juegos oficiales
                                String trainerId = "#TRN-" + generarIdAleatorio(5);

                                // Apuntamos a la base de datos "Entrenadores"
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid());

                                // Creamos el expediente del jugador
                                HashMap<String, Object> datosEntrenador = new HashMap<>();
                                datosEntrenador.put("trainerId", trainerId);
                                datosEntrenador.put("nivel", 1);
                                datosEntrenador.put("xp", 0);
                                datosEntrenador.put("medallas", 0);

                                // Guardamos y avanzamos
                                ref.setValue(datosEntrenador).addOnCompleteListener(dbTask -> {
                                    Toast.makeText(this, "Cuenta creada y perfil guardado", Toast.LENGTH_SHORT).show();
                                    irAlMenuPrincipal();
                                });
                            }
                        } else {
                            Toast.makeText(this, "Error al crear la cuenta", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        txtRecuperarPass.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(this, "Escribe tu correo arriba para recuperar la contraseña", Toast.LENGTH_LONG).show();
                return;
            }
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Correo de recuperación enviado", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private String generarIdAleatorio(int longitud) {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        Random rnd = new Random();
        for (int i = 0; i < longitud; i++) {
            sb.append(caracteres.charAt(rnd.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    private void irAlMenuPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
        startActivity(intent);
        finish();
    }
}