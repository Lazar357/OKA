package rs.ac.singidunum.Ciphers.enigma;

import java.util.HashMap;
import java.util.Map;

public class Plugboard {
    private Map<Character, Character> mapping;

    public Plugboard(String pairs) {
        this.mapping = new HashMap<>();

        String noSpacePairs = pairs.replaceAll("\\s", "");

        if (noSpacePairs.length() % 2 != 0) {
            System.out.println("Unesite parove!");
        }
        for (int i = 0; i < noSpacePairs.length(); i += 2) {
            char char1 = noSpacePairs.charAt(i);
            char char2 = noSpacePairs.charAt(i + 1);

            this.mapping.put(char1, char2);
            this.mapping.put(char2, char1);
        }
    }
    public char encrypt(char input) {

        return this.mapping.getOrDefault(input, input);
    }
}
