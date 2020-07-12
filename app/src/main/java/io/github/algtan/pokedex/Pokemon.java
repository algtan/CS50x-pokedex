package io.github.algtan.pokedex;

public class Pokemon {
    private String name;
    private int number;

    public Pokemon (String name, int number) {
        this.name = name;
        this.number = number;
    }

    public String getName() {
        return this.name;
    }

    public int getNumber() {
        return this.number;
    }
}
