package io.github.algtan.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class PokemonActivity extends AppCompatActivity {
    // Create parameters that represent 'View' items in the XML Layout file
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private RequestQueue speciesRequestQueue;
    private Button capturedButton;
    private ImageView pokemonImageView;
    private TextView descTextView;
    private ImageView pokeballImageView;

    // Create an 'isCaught' Boolean to record the capture state of the Pokemon
    private Boolean isCaught;

    // Create a String 'pokemonName' to record the Pokemon's name
    private String pokemonName;

    // Instantiate an instance of SharedPreferences
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        // Create RequestQueue from the Application context
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        speciesRequestQueue = Volley.newRequestQueue(getApplicationContext());

        // Get the extra items from the 'intent' that started this activity
        pokemonName = getIntent().getStringExtra("name");
        url = getIntent().getStringExtra("url");

        // Link the View parameters for this activity class to the items in the XML Layout file
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        capturedButton = findViewById(R.id.catch_button);
        pokemonImageView = findViewById(R.id.pokemon_sprite);
        descTextView = findViewById(R.id.pokemon_desc);
        pokeballImageView = findViewById(R.id.pokeball_caught);

        // Call load() method as the activity is being created to make the API request
        load();

        // Check SharedPreferences for 'isCaught' state
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("CaughtList", Context.MODE_PRIVATE);
        // If no state is available or previously created, then set the 'isCaught' variable to FALSE initially
        isCaught = sharedPreferences.getBoolean(pokemonName, Boolean.FALSE);

        // Set the text for the capturedButton based on 'isCaught' variable
        if (isCaught == Boolean.FALSE) {
            capturedButton.setText("Catch");
            pokeballImageView.setImageDrawable(null);
        } else {
            capturedButton.setText("Release");
            pokeballImageView.setImageResource(R.drawable.pokeball);
        }
    }

    // Method to load data about the selected Pokemon from the API
    public void load() {
        // Initialize Type TextViews as empty
        type1TextView.setText("");
        type2TextView.setText("");

        // Create a JSON request using the Volley library
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Parse the response from the API
                // Need an exception in case there is no key called 'results'
                try {
                    // Set the Name TextView, which has the key 'name' form the Json results
                    String name = response.getString("name");
                    nameTextView.setText(name.substring(0, 1).toUpperCase() + name.substring(1));
                    // Set the Number TextView, which has the key 'id' from the Json results
                    numberTextView.setText(String.format("#%03d", response.getInt("id")));

                    // Grab the array of 'Types' from the Json results
                    JSONArray typeEntries = response.getJSONArray("types");
                    // Store the types for how many type entries that Pokemon has
                    for (int i = 0; i < typeEntries.length(); i++) {
                        JSONObject typeEntry = typeEntries.getJSONObject(i);
                        // Read the information from the Json object with the key 'slot'
                        int slot = typeEntry.getInt("slot");
                        // Read the information from the Json object with the key 'name'
                        String type = typeEntry.getJSONObject("type").getString("name");

                        // If in the first type slot, use the Type1 TextView
                        if (slot == 1) {
                            type1TextView.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
                        }
                        else if (slot == 2) {
                            type2TextView.setText(type.substring(0, 1).toUpperCase() + type.substring(1));
                        }
                    }

                    // Grab the URL of the 'front_default' sprite from the 'Sprites' JSONObject
                    String spriteFrontDefaultUrl = response.getJSONObject("sprites").getString("front_default");
                    new DownloadSpriteTask().execute(spriteFrontDefaultUrl);

                    // Grab the URL of the 'flavor_text'
                    String speciesUrl = response.getJSONObject("species").getString("url");

                    JsonObjectRequest speciesRequest = new JsonObjectRequest(Request.Method.GET, speciesUrl, null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject speciesResponse) {
                            // Parse the response from the API
                            // Need an exception in case there is no key called 'results'
                            try {
                                JSONArray flavorTextEntries = speciesResponse.getJSONArray("flavor_text_entries");
                                for (int i = 9; i < flavorTextEntries.length(); i++) {
                                    JSONObject flavorTextEntry = flavorTextEntries.getJSONObject(i);
                                    String lang = flavorTextEntry.getJSONObject("language").getString("name");
                                    String ver = flavorTextEntry.getJSONObject("version").getString("name");
                                    String desc = flavorTextEntry.getString("flavor_text");
                                    Log.d("SPECIES", "onResponse: " + desc);
                                    if (lang.equals("en") && ver.equals("heartgold")) {
                                        descTextView.setText(desc);
                                        break;
                                    }
                                }
                            } catch (JSONException e) {
                                Log.e("cs50", "Pokemon Json error", e);
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // This gets called if the url doesn't exist
                            Log.e("cs50", "Pokemon details error");
                        }
                    });

                    // Use RequestQueue
                    requestQueue.add(speciesRequest);

                } catch (JSONException e) {
                    Log.e("cs50", "Pokemon Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // This gets called if the url doesn't exist
                Log.e("cs50", "Pokemon details error");
            }
        });

        // Use RequestQueue
        requestQueue.add(request);
    }

    public void toggleCatch(View view) {
        sharedPreferences = getSharedPreferences("CaughtList", Context.MODE_PRIVATE);
        // Create an Editor instance of SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // If 'isCaught' variable was FALSE when clicked, the Pokemon was captured
        if (isCaught == Boolean.FALSE) {
            // Change the Boolean to TRUE as the Pokemon is now caught
            isCaught = Boolean.TRUE;
            // Change the button text to "Release" since the Pokemon was caught
            capturedButton.setText("Release");
            pokeballImageView.setImageResource(R.drawable.pokeball);
        } else { // Otherwise, the Pokemon was already captured, and is being released
            // Change the Boolean to FALSE as the Pokemon is no longer caught
            isCaught = Boolean.FALSE;
            // Change the button text to "Catch" since the Pokemon was released
            capturedButton.setText("Catch");
            pokeballImageView.setImageDrawable(null);
        }

        // Update the SharedPreferences file
        editor.putBoolean(pokemonName, isCaught);
        editor.apply();
    }

    private class DownloadSpriteTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String... strings) {
            try {
                URL url = new URL(strings[0]);
                return BitmapFactory.decodeStream(url.openStream());
            }
            catch (IOException e) {
                Log.e("cs50", "Download sprite error", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            // load the bitmap into the ImageView!
            pokemonImageView.setImageBitmap(bitmap);
        }
    }
}