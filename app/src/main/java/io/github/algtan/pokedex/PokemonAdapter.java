package io.github.algtan.pokedex;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokedexViewHolder> {
    // Create a private class for the View Holder
    public static class PokedexViewHolder extends RecyclerView.ViewHolder {
        // Create parameters that represent 'View' items in the XML Layout file
        public LinearLayout containerView;
        public TextView textView;

        PokedexViewHolder(View view) {
            // Execute the superclass
            super(view);

            // Link the class parameters to their respective 'View' items in the XML Layout file
            containerView = view.findViewById(R.id.pokedex_row_layout);
            textView = view.findViewById(R.id.pokedex_row_textview);

            // Add an event listener
            containerView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    // Grab the Pokemon object we want to use in the PokemonActivity
                    // The getTag() method returns an object. We want to cast that Object to be an instance of Pokemon
                    Pokemon current = (Pokemon) containerView.getTag();

                    // Create a new intent object to pass data to PokemonActivity
                    Intent intent = new Intent(view.getContext(), PokemonActivity.class);
                    // Add extra information to the intent containing the Pokemon's information
                    intent.putExtra("name", current.getName()); // Extra's name is 'name'
                    intent.putExtra("number", current.getNumber()); // Extra's name is 'number'

                    // Start Activity with the intent
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    private List<Pokemon> pokemon = Arrays.asList(
            new Pokemon("Bulbasaur", 1),
            new Pokemon("Ivysaur", 2),
            new Pokemon("Venasuar", 3)
    );

    @NonNull
    @Override
    public PokemonAdapter.PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Convert our XML file to a Java object of the class 'View'
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonAdapter.PokedexViewHolder holder, int position) {
        Pokemon current = pokemon.get(position);
        holder.textView.setText(current.getName());

        // Pass along data in our adapter to the activity
        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        return pokemon.size();
    }
}
