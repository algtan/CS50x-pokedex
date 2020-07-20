package io.github.algtan.pokedex;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.PokedexViewHolder> implements Filterable {
    @Override
    public Filter getFilter() {
        return new PokemonFilter();
    }

    // Create a private class for PokemonFilter, which will filter the list of Pokemon by the search parameters
    private class PokemonFilter extends Filter {

        @Override
        protected FilterResults performFiltering(final CharSequence charSequence) {
            // implement your search here!

            // Create a 'List' variable to capture the Pokemon to be shown after using the filter using a Shallow Copy
            List<Pokemon> filteredPokemon = new ArrayList<>(pokemon);
            // Use the 'removeIf' method, which takes a 'Predicate' as a parameter
            filteredPokemon.removeIf(mon -> !mon.getName().toLowerCase().contains(charSequence.toString().toLowerCase()));

            FilterResults results = new FilterResults();
            results.values = filteredPokemon; // you need to create this variable!
            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filtered = (List<Pokemon>) filterResults.values;
            notifyDataSetChanged();
        }
    }

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
                    intent.putExtra("url", current.getUrl()); // Extra's name is 'url'

                    // Start Activity with the intent
                    view.getContext().startActivity(intent);
                }
            });
        }
    }

    // Create a parameter 'pokemon' that stores the list of Pokemon from the API
    private List<Pokemon> pokemon = new ArrayList<>();

    // Create a parameter 'filtered' that stores the list of filtered Pokemon
    private List<Pokemon> filtered = new ArrayList<>();

    // Create a RequestQueue parameter, which will kick off the request so that it starts running
    private RequestQueue requestQueue;

    // Create a PokemonAdapter constructor to gain access to the context from the activity
    PokemonAdapter(Context context) {
        // Create RequestQueue using the Volley library
        requestQueue = Volley.newRequestQueue(context);
        // Run the 'loadPokemon' function
        loadPokemon();
    }

    // Create a method that loads Pokemon into the 'pokemon' list
    public void loadPokemon() {
        // Store the url in a string
        String url = "https://pokeapi.co/api/v2/pokemon?limit=151";

        // Create a JSON request using the Volley library
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Parse the response from the API
                // Need an exception in case there is no key called 'results'
                try {
                    // We want an Array from the JSON, looking for a key called 'results'
                    JSONArray results = response.getJSONArray("results");
                    // Iterate through the array, and add each of those results to our list
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject result = results.getJSONObject(i);

                        // Create a String to store the Pokemon name from the JSON object, which will be in all lowercase
                        String name = result.getString("name");
                        // Add Pokemon to the list by creating a new Pokemon object
                        pokemon.add(new Pokemon(name.substring(0, 1).toUpperCase() + name.substring(1), result.getString("url")));
                        filtered.add(new Pokemon(name.substring(0, 1).toUpperCase() + name.substring(1), result.getString("url")));
                    }

                    // Let the RecyclerView know that the data changed, and it needs to update the View items
                    notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e("cs50", "Json error", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // This gets called if the url doesn't exist
                Log.e("cs50", "Pokemon list error");
            }
        });

        // Make sure our request is added and that it starts
        requestQueue.add(request);
    }

    @NonNull
    @Override
    public PokemonAdapter.PokedexViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Convert our XML file to a Java object of the class 'View'
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pokedex_row, parent, false);

        return new PokedexViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PokemonAdapter.PokedexViewHolder holder, int position) {
        Pokemon current = filtered.get(position);
        holder.textView.setText(current.getName());

        // Pass along data in our adapter to the activity
        holder.containerView.setTag(current);
    }

    @Override
    public int getItemCount() {
        return filtered.size();
    }
}
