package com.example.pokeapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HistorialHelper {

    // Esta función la podrás usar en CUALQUIER pantalla de tu app en el futuro
    public static void registrar(String tipo, String titulo, String detalle) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("Entrenadores")
                    .child(user.getUid())
                    .child("historial");

            String key = ref.push().getKey();
            if (key != null) {
                // System.currentTimeMillis() guarda la fecha y hora exacta del celular
                ItemHistorial item = new ItemHistorial(key, tipo, titulo, detalle, System.currentTimeMillis());
                ref.child(key).setValue(item);
            }
        }
    }
}