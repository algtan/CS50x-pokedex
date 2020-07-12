package io.github.algtan.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class PokemonActivity extends AppCompatActivity {
    // Create parameters that represent 'View' items in the XML Layout file
    private TextView nameTextView;
    private TextView numberTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pokemon);

        // Get the extra items from the 'intent' that started this activity
        String name = getIntent().getStringExtra("name");
        int number = getIntent().getIntExtra("number", 0);

        // Link the View parameters for this activity class to the items in the XML Layout file
        nameTextView = findViewById(R.id.pokemon_name);
        numberTextView = findViewById(R.id.pokemon_number);

        // Populate the TextViews in the XML Layout file with data from the intent
        nameTextView.setText(name);
        numberTextView.setText(String.format("#%03d", number));
    }
}