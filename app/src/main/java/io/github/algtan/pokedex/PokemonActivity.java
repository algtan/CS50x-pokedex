package io.github.algtan.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class PokemonActivity extends AppCompatActivity {
    // Create parameters that represent 'View' items in the XML Layout file
    private TextView nameTextView;
    private TextView numberTextView;
    private TextView type1TextView;
    private TextView type2TextView;
    private String url;
    private RequestQueue requestQueue;
    private Button capturedButton;

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

        // Get the extra items from the 'intent' that started this activity
        pokemonName = getIntent().getStringExtra("name");
        url = getIntent().getStringExtra("url");

        // Link the View parameters for this activity class to the items in the XML Layout file
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);
        type1TextView = findViewById(R.id.pokemon_type1);
        type2TextView = findViewById(R.id.pokemon_type2);
        capturedButton = findViewById(R.id.catch_button);

        // Call load() method as the activity is being created to make the API request
        load();

        // Check SharedPreferences for 'isCaught' state
        // Initialize SharedPreferences
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        // If no state is available or previously created, then set the 'isCaught' variable to FALSE initially
        isCaught = sharedPreferences.getBoolean(pokemonName, Boolean.FALSE);

        // Set the text for the capturedButton based on 'isCaught' variable
        if (isCaught == Boolean.FALSE) {
            capturedButton.setText("Catch");
        } else {
            capturedButton.setText("Release");
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
        sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        // Create an Editor instance of SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // If 'isCaught' variable was FALSE when clicked, the Pokemon was captured
        if (isCaught == Boolean.FALSE) {
            // Change the Boolean to TRUE as the Pokemon is now caught
            isCaught = Boolean.TRUE;
            // Change the button text to "Release" since the Pokemon was caught
            capturedButton.setText("Release");
        } else { // Otherwise, the Pokemon was already captured, and is being released
            // Change the Boolean to FALSE as the Pokemon is no longer caught
            isCaught = Boolean.FALSE;
            // Change the button text to "Catch" since the Pokemon was released
            capturedButton.setText("Catch");
        }

        // Update the SharedPreferences file
        editor.putBoolean(pokemonName, isCaught);
        editor.apply();
    }
}