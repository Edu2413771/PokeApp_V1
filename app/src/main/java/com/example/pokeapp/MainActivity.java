package com.example.pokeapp;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText etPokemon;
    private ImageButton btnConsultar, btnFavorito;
    private Button btnVolverMenu, btnToggleShiny, btnGrito;
    private ImageView imgPokemon;
    private TextView txtNombrePokemon, txtIdPokemon, txtDescripcion, txtMovimientos;

    private TextView txtHpVal, txtAtaqueVal, txtDefensaVal, txtAtaqueEspVal, txtDefensaEspVal, txtVelocidadVal;
    private ProgressBar pbHp, pbAtaque, pbDefensa, pbAtaqueEsp, pbDefensaEsp, pbVelocidad;

    private LinearLayout btnTabStats, btnTabInfo, btnTabMoves;
    private TextView txtTabStats, txtTabInfo, txtTabMoves;
    private View lineTabStats, lineTabInfo, lineTabMoves;

    private LinearLayout layoutEstadisticas, layoutInfo, layoutMovimientos, layoutEvolucionesContainer, layoutTipos;

    private LinearLayout layoutFormas;
    private Button btnFormaNormal, btnFormaMega, btnFormaMegaX, btnFormaMegaY, btnFormaMegaZ, btnFormaGmax;

    private PokeApiService service;
    private MediaPlayer mediaPlayer;

    private DatabaseReference favRef;
    private boolean esFavoritoLocal = false;
    private String formaActual = "";

    private boolean mostrandoShiny = false;
    private boolean forzarShinyOnLoad = false;

    private String urlNormal = "";
    private String urlShiny = "";
    private int currentPokemonId = 0;

    private String pokemonBaseActual = "";

    private final List<String> megasComunes = Arrays.asList("venusaur", "blastoise", "alakazam", "gengar", "kangaskhan", "pinsir", "gyarados", "aerodactyl", "ampharos", "scizor", "heracross", "houndoom", "tyranitar", "sceptile", "blaziken", "swampert", "gardevoir", "sableye", "mawile", "aggron", "medicham", "manectric", "banette", "absol", "garchomp", "lucario", "abomasnow", "beedrill", "pidgeot", "slowbro", "steelix", "audino", "diancie");
    private final List<String> megasXY = Arrays.asList("charizard", "mewtwo");
    private final List<String> megasZ = Arrays.asList("absol", "lucario");
    private final List<String> gmax = Arrays.asList("charizard", "butterfree", "pikachu", "meowth", "machamp", "gengar", "kingler", "lapras", "eevee", "snorlax", "garbodor", "melmetal", "corviknight", "orbeetle", "drednaw", "coalossal", "flapple", "appletun", "sandaconda", "toxtricity", "centiskorch", "hatterene", "grimmsnarl", "alcremie", "copperajah", "duraludon", "urshifu", "venusaur", "blastoise", "rillaboom", "cinderace", "inteleon");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        enlazarVistas();
        configurarPestanas();
        configurarBotonesFormas();

        service = ApiClient.getClient().create(PokeApiService.class);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            favRef = FirebaseDatabase.getInstance().getReference("Entrenadores").child(user.getUid()).child("favoritos");
        }

        btnConsultar.setOnClickListener(v -> {
            String busqueda = etPokemon.getText().toString().trim().toLowerCase(Locale.ROOT);
            if (busqueda.isEmpty()) {
                Toast.makeText(this, "Escribe un nombre o ID", Toast.LENGTH_SHORT).show();
                return;
            }
            pokemonBaseActual = busqueda;
            consultarPokemon(busqueda);
        });

        btnToggleShiny.setOnClickListener(v -> {
            if (urlNormal.isEmpty() || urlShiny.isEmpty()) return;
            mostrandoShiny = !mostrandoShiny;
            btnToggleShiny.setText(mostrandoShiny ? "Normal" : "✨ Shiny");
            Glide.with(this).load(mostrandoShiny ? urlShiny : urlNormal).into(imgPokemon);
            verificarSiEsFavorito();
        });

        btnGrito.setOnClickListener(v -> {
            if (currentPokemonId > 0) reproducirGritoSeguro(currentPokemonId);
            else Toast.makeText(this, "Primero debes buscar un Pokémon", Toast.LENGTH_SHORT).show();
        });

        btnFavorito.setOnClickListener(v -> toggleFavorito());
        btnVolverMenu.setOnClickListener(v -> finish());

        String pokemonDeFavoritos = getIntent().getStringExtra("POKEMON_BUSQUEDA");
        forzarShinyOnLoad = getIntent().getBooleanExtra("ES_SHINY", false);

        if (pokemonDeFavoritos != null && !pokemonDeFavoritos.isEmpty()) {
            etPokemon.setText(pokemonDeFavoritos);
            pokemonBaseActual = pokemonDeFavoritos;
            consultarPokemon(pokemonDeFavoritos);
        }
    }

    private void enlazarVistas() {
        etPokemon = findViewById(R.id.etPokemon);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnVolverMenu = findViewById(R.id.btnVolverMenu);
        btnToggleShiny = findViewById(R.id.btnToggleShiny);
        btnGrito = findViewById(R.id.btnGrito);
        btnFavorito = findViewById(R.id.btnFavorito);

        imgPokemon = findViewById(R.id.imgPokemon);
        txtNombrePokemon = findViewById(R.id.txtNombrePokemon);
        txtIdPokemon = findViewById(R.id.txtIdPokemon);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtMovimientos = findViewById(R.id.txtMovimientos);

        txtHpVal = findViewById(R.id.txtHpVal);
        txtAtaqueVal = findViewById(R.id.txtAtaqueVal);
        txtDefensaVal = findViewById(R.id.txtDefensaVal);
        txtAtaqueEspVal = findViewById(R.id.txtAtaqueEspVal);
        txtDefensaEspVal = findViewById(R.id.txtDefensaEspVal);
        txtVelocidadVal = findViewById(R.id.txtVelocidadVal);

        pbHp = findViewById(R.id.pbHp);
        pbAtaque = findViewById(R.id.pbAtaque);
        pbDefensa = findViewById(R.id.pbDefensa);
        pbAtaqueEsp = findViewById(R.id.pbAtaqueEsp);
        pbDefensaEsp = findViewById(R.id.pbDefensaEsp);
        pbVelocidad = findViewById(R.id.pbVelocidad);

        btnTabStats = findViewById(R.id.btnTabStats);
        btnTabInfo = findViewById(R.id.btnTabInfo);
        btnTabMoves = findViewById(R.id.btnTabMoves);
        txtTabStats = findViewById(R.id.txtTabStats);
        txtTabInfo = findViewById(R.id.txtTabInfo);
        txtTabMoves = findViewById(R.id.txtTabMoves);
        lineTabStats = findViewById(R.id.lineTabStats);
        lineTabInfo = findViewById(R.id.lineTabInfo);
        lineTabMoves = findViewById(R.id.lineTabMoves);

        layoutEstadisticas = findViewById(R.id.layoutEstadisticas);
        layoutInfo = findViewById(R.id.layoutInfo);
        layoutMovimientos = findViewById(R.id.layoutMovimientos);
        layoutEvolucionesContainer = findViewById(R.id.layoutEvolucionesContainer);
        layoutTipos = findViewById(R.id.layoutTipos);

        layoutFormas = findViewById(R.id.layoutFormas);
        btnFormaNormal = findViewById(R.id.btnFormaNormal);
        btnFormaMega = findViewById(R.id.btnFormaMega);
        btnFormaMegaX = findViewById(R.id.btnFormaMegaX);
        btnFormaMegaY = findViewById(R.id.btnFormaMegaY);
        btnFormaMegaZ = findViewById(R.id.btnFormaMegaZ);
        btnFormaGmax = findViewById(R.id.btnFormaGmax);
    }

    private void verificarSiEsFavorito() {
        if (favRef == null || formaActual.isEmpty()) return;
        String favKey = formaActual + (mostrandoShiny ? "_shiny" : "_normal");

        favRef.child(favKey).get().addOnSuccessListener(snapshot -> {
            esFavoritoLocal = snapshot.exists();
            btnFavorito.setColorFilter(esFavoritoLocal ? Color.parseColor("#FFCB05") : Color.parseColor("#888888"));
        });
    }

    private void toggleFavorito() {
        if (favRef == null || formaActual.isEmpty()) return;
        String favKey = formaActual + (mostrandoShiny ? "_shiny" : "_normal");

        if (esFavoritoLocal) {
            favRef.child(favKey).removeValue();
            esFavoritoLocal = false;
            btnFavorito.setColorFilter(Color.parseColor("#888888"));
            Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();

            // REGISTRO EN HISTORIAL
            HistorialHelper.registrar("sistema", "Favoritos", "Eliminaste a " + txtNombrePokemon.getText().toString());
        } else {
            HashMap<String, Object> favData = new HashMap<>();
            favData.put("idAPI", currentPokemonId);
            favData.put("nombreVisible", txtNombrePokemon.getText().toString());
            favData.put("formaAPI", formaActual);
            favData.put("esShiny", mostrandoShiny);
            favData.put("urlImagen", mostrandoShiny ? urlShiny : urlNormal);

            favRef.child(favKey).setValue(favData);
            esFavoritoLocal = true;
            btnFavorito.setColorFilter(Color.parseColor("#FFCB05"));
            Toast.makeText(this, "¡Guardado en Favoritos!", Toast.LENGTH_SHORT).show();

            // REGISTRO EN HISTORIAL
            HistorialHelper.registrar("sistema", "Favoritos", "Agregaste a " + txtNombrePokemon.getText().toString());
        }
    }

    private void configurarBotonesFormas() {
        btnFormaNormal.setOnClickListener(v -> consultarPokemon(pokemonBaseActual));
        btnFormaMega.setOnClickListener(v -> consultarPokemon(pokemonBaseActual + "-mega"));
        btnFormaMegaX.setOnClickListener(v -> consultarPokemon(pokemonBaseActual + "-mega-x"));
        btnFormaMegaY.setOnClickListener(v -> consultarPokemon(pokemonBaseActual + "-mega-y"));
        btnFormaGmax.setOnClickListener(v -> consultarPokemon(pokemonBaseActual + "-gmax"));
        btnFormaMegaZ.setOnClickListener(v -> Toast.makeText(this, "PokéAPI aún no actualiza los sprites de Legends Z-A", Toast.LENGTH_LONG).show());
    }

    private void consultarPokemon(String busqueda) {
        Call<Pokemon> call = service.getPokemon(busqueda);
        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null) mostrarPokemon(response.body());
                else { limpiarResultado(); Toast.makeText(MainActivity.this, "Forma no encontrada", Toast.LENGTH_SHORT).show(); }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {
                limpiarResultado(); Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarPokemon(Pokemon pokemon) {
        reiniciarStats(false);
        layoutTipos.removeAllViews();
        layoutEvolucionesContainer.removeAllViews();

        currentPokemonId = pokemon.getId();
        formaActual = pokemon.getName().toLowerCase(Locale.ROOT);

        txtNombrePokemon.setText(pokemon.getName().toUpperCase(Locale.ROOT).replace("-", " "));
        txtIdPokemon.setText(String.format(Locale.ROOT, "#%03d", pokemon.getId()));

        // REGISTRO EN HISTORIAL (¡El gran cambio!)
        HistorialHelper.registrar("sistema", "Pokédex", "Buscaste a " + txtNombrePokemon.getText().toString());

        if (pokemon.getTypes() != null) {
            for (Pokemon.TypeSlot slot : pokemon.getTypes()) {
                String tipoIngles = slot.getType().getName();
                String tipoEspanol = traducirTipo(tipoIngles);
                String colorHex = obtenerColorTipo(tipoIngles);

                TextView txtTipo = new TextView(this);
                txtTipo.setText(tipoEspanol.toUpperCase(Locale.ROOT));
                txtTipo.setTextColor(Color.WHITE);
                txtTipo.setTextSize(12f);
                txtTipo.setTypeface(null, Typeface.BOLD);
                txtTipo.setPadding(40, 12, 40, 12);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 0, 16, 0);
                txtTipo.setLayoutParams(params);

                GradientDrawable gd = new GradientDrawable();
                gd.setColor(Color.parseColor(colorHex));
                gd.setCornerRadius(50f);
                txtTipo.setBackground(gd);

                layoutTipos.addView(txtTipo);
            }
        }

        if (pokemon.getSprites() != null) {
            urlNormal = pokemon.getSprites().getFrontDefault();
            urlShiny = pokemon.getSprites().getFrontShiny();

            mostrandoShiny = forzarShinyOnLoad;
            btnToggleShiny.setText(mostrandoShiny ? "Normal" : "✨ Shiny");
            Glide.with(this).load(mostrandoShiny ? urlShiny : urlNormal).into(imgPokemon);

            forzarShinyOnLoad = false;
        }

        verificarSiEsFavorito();

        if (pokemon.getStats() != null) {
            for (PokemonStat item : pokemon.getStats()) {
                if (item.getStat() == null) continue;
                String nombre = item.getStat().getName();
                int valor = item.getBaseStat();
                switch (nombre) {
                    case "hp": txtHpVal.setText(String.valueOf(valor)); pbHp.setProgress(valor); break;
                    case "attack": txtAtaqueVal.setText(String.valueOf(valor)); pbAtaque.setProgress(valor); break;
                    case "defense": txtDefensaVal.setText(String.valueOf(valor)); pbDefensa.setProgress(valor); break;
                    case "special-attack": txtAtaqueEspVal.setText(String.valueOf(valor)); pbAtaqueEsp.setProgress(valor); break;
                    case "special-defense": txtDefensaEspVal.setText(String.valueOf(valor)); pbDefensaEsp.setProgress(valor); break;
                    case "speed": txtVelocidadVal.setText(String.valueOf(valor)); pbVelocidad.setProgress(valor); break;
                }
            }
        }

        if (pokemon.getMoves() != null && !pokemon.getMoves().isEmpty()) {
            StringBuilder movimientosStr = new StringBuilder();
            int limite = Math.min(pokemon.getMoves().size(), 15);
            for (int i = 0; i < limite; i++) {
                String nombreMov = pokemon.getMoves().get(i).getMove().getName().replace("-", " ");
                movimientosStr.append(i + 1).append(". ").append(nombreMov.toUpperCase(Locale.ROOT)).append("\n");
            }
            txtMovimientos.setText(movimientosStr.toString());
        }

        String nombrePuro = formaActual;
        if (nombrePuro.contains("-mega") || nombrePuro.contains("-gmax")) {
            pokemonBaseActual = nombrePuro.split("-mega")[0].split("-gmax")[0];
        } else {
            pokemonBaseActual = nombrePuro;
        }

        actualizarBotonesFormas(pokemonBaseActual);
        txtDescripcion.setText("Buscando descripción...");
        obtenerDescripcionPokemon(pokemonBaseActual);
    }

    private void actualizarBotonesFormas(String base) {
        btnFormaNormal.setVisibility(View.GONE);
        btnFormaMega.setVisibility(View.GONE);
        btnFormaMegaX.setVisibility(View.GONE);
        btnFormaMegaY.setVisibility(View.GONE);
        btnFormaMegaZ.setVisibility(View.GONE);
        btnFormaGmax.setVisibility(View.GONE);

        boolean tieneFormas = false;

        if (megasComunes.contains(base)) { btnFormaNormal.setVisibility(View.VISIBLE); btnFormaMega.setVisibility(View.VISIBLE); tieneFormas = true; }
        if (megasXY.contains(base)) { btnFormaNormal.setVisibility(View.VISIBLE); btnFormaMegaX.setVisibility(View.VISIBLE); btnFormaMegaY.setVisibility(View.VISIBLE); tieneFormas = true; }
        if (megasZ.contains(base)) { btnFormaNormal.setVisibility(View.VISIBLE); btnFormaMegaZ.setVisibility(View.VISIBLE); tieneFormas = true; }
        if (gmax.contains(base)) { btnFormaNormal.setVisibility(View.VISIBLE); btnFormaGmax.setVisibility(View.VISIBLE); tieneFormas = true; }

        layoutFormas.setVisibility(tieneFormas ? View.VISIBLE : View.GONE);
    }

    private void configurarPestanas() {
        btnTabStats.setOnClickListener(v -> activarPestana(1));
        btnTabInfo.setOnClickListener(v -> activarPestana(2));
        btnTabMoves.setOnClickListener(v -> activarPestana(3));
        activarPestana(1);
    }

    private void activarPestana(int tab) {
        txtTabStats.setTextColor(Color.parseColor("#888888")); txtTabStats.setTypeface(null, Typeface.NORMAL); lineTabStats.setBackgroundColor(Color.TRANSPARENT); layoutEstadisticas.setVisibility(View.GONE);
        txtTabInfo.setTextColor(Color.parseColor("#888888")); txtTabInfo.setTypeface(null, Typeface.NORMAL); lineTabInfo.setBackgroundColor(Color.TRANSPARENT); layoutInfo.setVisibility(View.GONE);
        txtTabMoves.setTextColor(Color.parseColor("#888888")); txtTabMoves.setTypeface(null, Typeface.NORMAL); lineTabMoves.setBackgroundColor(Color.TRANSPARENT); layoutMovimientos.setVisibility(View.GONE);

        if (tab == 1) { txtTabStats.setTextColor(Color.parseColor("#4CAF50")); txtTabStats.setTypeface(null, Typeface.BOLD); lineTabStats.setBackgroundColor(Color.parseColor("#4CAF50")); layoutEstadisticas.setVisibility(View.VISIBLE); }
        else if (tab == 2) { txtTabInfo.setTextColor(Color.parseColor("#4CAF50")); txtTabInfo.setTypeface(null, Typeface.BOLD); lineTabInfo.setBackgroundColor(Color.parseColor("#4CAF50")); layoutInfo.setVisibility(View.VISIBLE); }
        else if (tab == 3) { txtTabMoves.setTextColor(Color.parseColor("#4CAF50")); txtTabMoves.setTypeface(null, Typeface.BOLD); lineTabMoves.setBackgroundColor(Color.parseColor("#4CAF50")); layoutMovimientos.setVisibility(View.VISIBLE); }
    }

    private void reproducirGritoSeguro(int id) {
        Toast.makeText(this, "Descargando grito...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                URL url = new URL("https://raw.githubusercontent.com/PokeAPI/cries/main/cries/pokemon/latest/" + id + ".ogg");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) throw new Exception();
                InputStream input = conn.getInputStream();
                File temp = new File(getCacheDir(), "grito.ogg");
                FileOutputStream output = new FileOutputStream(temp);
                byte[] data = new byte[1024]; int count;
                while ((count = input.read(data)) != -1) output.write(data, 0, count);
                output.flush(); output.close(); input.close();
                runOnUiThread(() -> {
                    try {
                        if (mediaPlayer != null) mediaPlayer.release();
                        mediaPlayer = new MediaPlayer();
                        mediaPlayer.setDataSource(temp.getAbsolutePath());
                        mediaPlayer.prepare(); mediaPlayer.start();
                    } catch (Exception e) {}
                });
            } catch (Exception e) { runOnUiThread(() -> Toast.makeText(MainActivity.this, "Grito no disponible", Toast.LENGTH_SHORT).show()); }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) { mediaPlayer.release(); mediaPlayer = null; }
    }

    private void obtenerDescripcionPokemon(String baseName) {
        Call<PokemonSpecies> call = service.getPokemonSpeciesStr(baseName);
        call.enqueue(new Callback<PokemonSpecies>() {
            @Override
            public void onResponse(Call<PokemonSpecies> call, Response<PokemonSpecies> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getFlavorTextEntries() != null) {
                        for (PokemonSpecies.FlavorTextEntry entrada : response.body().getFlavorTextEntries()) {
                            if (entrada.getLanguage() != null && entrada.getLanguage().getName().equals("es")) {
                                txtDescripcion.setText(entrada.getFlavorText().replace("\n", " ").replace("\f", " "));
                                break;
                            }
                        }
                    }
                    if (response.body().getEvolutionChain() != null && response.body().getEvolutionChain().getUrl() != null) {
                        String[] partes = response.body().getEvolutionChain().getUrl().split("/");
                        procesarCadenaEvolutiva(Integer.parseInt(partes[partes.length - 1]));
                    }
                }
            }
            @Override
            public void onFailure(Call<PokemonSpecies> call, Throwable t) { txtDescripcion.setText("Error al cargar descripción."); }
        });
    }

    private void procesarCadenaEvolutiva(int idCadena) {
        Call<EvolutionChain> call = service.getEvolutionChain(idCadena);
        call.enqueue(new Callback<EvolutionChain>() {
            @Override
            public void onResponse(Call<EvolutionChain> call, Response<EvolutionChain> response) {
                if (response.isSuccessful() && response.body() != null) recorrerArbolEvolutivo(response.body().getChain());
            }
            @Override
            public void onFailure(Call<EvolutionChain> call, Throwable t) {}
        });
    }

    private void recorrerArbolEvolutivo(EvolutionChain.ChainLink link) {
        if (link == null || link.getSpecies() == null) return;
        String nombre = link.getSpecies().getName();

        LinearLayout itemLayout = new LinearLayout(this);
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setGravity(Gravity.CENTER);
        itemLayout.setPadding(16, 0, 16, 0);

        ImageView imgEvo = new ImageView(this);
        imgEvo.setLayoutParams(new LinearLayout.LayoutParams(250, 250));
        TextView txtEvo = new TextView(this);
        txtEvo.setText(nombre.toUpperCase(Locale.ROOT));
        txtEvo.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        txtEvo.setTextColor(Color.parseColor("#888888"));

        itemLayout.addView(imgEvo); itemLayout.addView(txtEvo);
        layoutEvolucionesContainer.addView(itemLayout);

        Call<Pokemon> call = service.getPokemon(nombre);
        call.enqueue(new Callback<Pokemon>() {
            @Override
            public void onResponse(Call<Pokemon> call, Response<Pokemon> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getSprites() != null) {
                    Glide.with(MainActivity.this).load(response.body().getSprites().getFrontDefault()).into(imgEvo);
                }
            }
            @Override
            public void onFailure(Call<Pokemon> call, Throwable t) {}
        });

        if (link.getEvolvesTo() != null) {
            for (EvolutionChain.ChainLink siguienteEvo : link.getEvolvesTo()) recorrerArbolEvolutivo(siguienteEvo);
        }
    }

    private String traducirTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "normal": return "Normal"; case "fire": return "Fuego"; case "water": return "Agua";
            case "electric": return "Eléctrico"; case "grass": return "Planta"; case "ice": return "Hielo";
            case "fighting": return "Lucha"; case "poison": return "Veneno"; case "ground": return "Tierra";
            case "flying": return "Volador"; case "psychic": return "Psíquico"; case "bug": return "Bicho";
            case "rock": return "Roca"; case "ghost": return "Fantasma"; case "dragon": return "Dragón";
            case "dark": return "Siniestro"; case "steel": return "Acero"; case "fairy": return "Hada";
            default: return tipo;
        }
    }

    private String obtenerColorTipo(String tipo) {
        switch (tipo.toLowerCase()) {
            case "normal": return "#A8A878"; case "fire": return "#F08030"; case "water": return "#6890F0";
            case "electric": return "#F8D030"; case "grass": return "#78C850"; case "ice": return "#98D8D8";
            case "fighting": return "#C03028"; case "poison": return "#A040A0"; case "ground": return "#E0C068";
            case "flying": return "#A890F0"; case "psychic": return "#F85888"; case "bug": return "#A8B820";
            case "rock": return "#B8A038"; case "ghost": return "#705898"; case "dragon": return "#7038F8";
            case "dark": return "#705848"; case "steel": return "#B8B8D0"; case "fairy": return "#EE99AC";
            default: return "#68A090";
        }
    }

    private void reiniciarStats(boolean limpiarId) {
        if (limpiarId) { currentPokemonId = 0; pokemonBaseActual = ""; formaActual = ""; }

        txtHpVal.setText("--"); pbHp.setProgress(0);
        txtAtaqueVal.setText("--"); pbAtaque.setProgress(0);
        txtDefensaVal.setText("--"); pbDefensa.setProgress(0);
        txtAtaqueEspVal.setText("--"); pbAtaqueEsp.setProgress(0);
        txtDefensaEspVal.setText("--"); pbDefensaEsp.setProgress(0);
        txtVelocidadVal.setText("--"); pbVelocidad.setProgress(0);

        btnFavorito.setColorFilter(Color.parseColor("#888888"));
    }

    private void limpiarResultado() {
        txtNombrePokemon.setText("POKÉMON");
        txtIdPokemon.setText("#---");
        imgPokemon.setImageDrawable(null);
        txtDescripcion.setText("Cargando datos...");
        txtMovimientos.setText("-");
        layoutTipos.removeAllViews();
        layoutEvolucionesContainer.removeAllViews();
        layoutFormas.setVisibility(View.GONE);
        reiniciarStats(true);
    }
}