package com.example.pokeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritosActivity extends AppCompatActivity {

    private RecyclerView rvFavoritos;
    private Button btnVolverFavoritos;

    private FavoritoAdapter adapter;
    private List<PokemonFavorito> lista;
    private DatabaseReference favRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favoritos);

        rvFavoritos = findViewById(R.id.rvFavoritos);
        btnVolverFavoritos = findViewById(R.id.btnVolverFavoritos);

        // Configuramos la cuadrícula de 3 columnas
        rvFavoritos.setLayoutManager(new GridLayoutManager(this, 3));
        lista = new ArrayList<>();
        adapter = new FavoritoAdapter(lista, this);
        rvFavoritos.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            favRef = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid()).child("favoritos");
            cargarFavoritos();
        } else {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnVolverFavoritos.setOnClickListener(v -> finish());
    }

    private void cargarFavoritos() {
        // Leemos la base de datos en tiempo real
        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                lista.clear(); // Limpiamos para evitar duplicados
                for (DataSnapshot item : snapshot.getChildren()) {
                    PokemonFavorito pokemon = item.getValue(PokemonFavorito.class);
                    if (pokemon != null) {
                        lista.add(pokemon);
                    }
                }
                adapter.notifyDataSetChanged(); // Avisamos que hay nuevos datos
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FavoritosActivity.this, "Error al cargar favoritos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}