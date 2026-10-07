package com.example.pokeapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistorialActivity extends AppCompatActivity {

    private RecyclerView rvHistorial;
    private Button btnVolverHistorial;

    private HistorialAdapter adapter;
    private List<ItemHistorial> listaHistorial;
    private DatabaseReference historialRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial);

        rvHistorial = findViewById(R.id.rvHistorial);
        btnVolverHistorial = findViewById(R.id.btnVolverHistorial);

        rvHistorial.setLayoutManager(new LinearLayoutManager(this));
        listaHistorial = new ArrayList<>();
        adapter = new HistorialAdapter(listaHistorial, this);
        rvHistorial.setAdapter(adapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            historialRef = FirebaseDatabase.getInstance().getReference("Entrenadores")
                    .child(user.getUid()).child("historial");
            cargarHistorial();
        } else {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnVolverHistorial.setOnClickListener(v -> finish());
    }

    private void cargarHistorial() {
        historialRef.orderByChild("timestamp").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaHistorial.clear();
                for (DataSnapshot item : snapshot.getChildren()) {
                    ItemHistorial evento = item.getValue(ItemHistorial.class);
                    if (evento != null) {
                        listaHistorial.add(evento);
                    }
                }
                // Invertimos la lista para que el evento más reciente (el último que se agregó) salga hasta arriba
                Collections.reverse(listaHistorial);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HistorialActivity.this, "Error al cargar el historial", Toast.LENGTH_SHORT).show();
            }
        });
    }
}