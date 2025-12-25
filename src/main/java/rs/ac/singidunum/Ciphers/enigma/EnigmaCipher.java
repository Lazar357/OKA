package rs.ac.singidunum.Ciphers.enigma;

import rs.ac.singidunum.Ciphers.enigma.Reflector;
import rs.ac.singidunum.Ciphers.enigma.Rotor;

public class EnigmaCipher {

    private String ALPHABET;
    private Plugboard plugboard;
    private Reflector reflector;
    private Rotor[] rotors;

    public EnigmaCipher(String ALPHABET, String plugboardMappings, String reflectorMappings, String[] rotors, String key) {
        this.ALPHABET = ALPHABET;
        this.plugboard = new Plugboard(plugboardMappings);
        this.reflector = new Reflector(ALPHABET, reflectorMappings);
        this.rotors = new Rotor[rotors.length];
        for (int i = 0; i < rotors.length; i++) {
            this.rotors[i] = new Rotor(ALPHABET, rotors[i]);
        }
        setRotorPositions(key);
    }

    public EnigmaCipher(String ALPHABET, String reflectorMappings, String[] rotors, String key) {
        this.ALPHABET = ALPHABET;
        this.reflector = new Reflector(ALPHABET, reflectorMappings);
        this.rotors = new Rotor[rotors.length];
        for (int i = 0; i < rotors.length; i++) {
            this.rotors[i] = new Rotor(ALPHABET, rotors[i]);
        }
        setRotorPositions(key);
    }

    public void setRotorPositions(String key) {
        for (int i = 0; i < rotors.length; i++) {
            rotors[i].setPosition(ALPHABET.indexOf(key.charAt(i)));
        }
    }

    public void setKey(String key) {
        setRotorPositions(key);
    }

    public String encrypt(String message) {
        return process(message);
    }

    public String decrypt(String cipher) {
        return process(cipher);
    }

    private String process(String text) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char afterPlugboard = plugboard.encrypt(c);
            int fromALPHABET = this.ALPHABET.indexOf(afterPlugboard);

            // Menjam pozicije rotora za jedan
            stepRotors();

            int cipherCharPosition = fromALPHABET;
            for (int j = rotors.length - 1; j >= 0; j--) {
                cipherCharPosition = rotors[j].forward(cipherCharPosition);
            }

            cipherCharPosition = reflector.reflect(cipherCharPosition);

            for (int j = 0; j < rotors.length; j++) {
                cipherCharPosition = rotors[j].backwords(cipherCharPosition);
            }

            char charBeforeFinalPlugboard = ALPHABET.charAt(cipherCharPosition);

            char finalCipherChar = plugboard.encrypt(charBeforeFinalPlugboard);

            sb.append(finalCipherChar);
        }

        return sb.toString();
    }

    private void stepRotors() {
        boolean carryOver = rotors[rotors.length - 1].step();
        for (int i = rotors.length - 2; i >= 0; i--) {
            if (carryOver) {
                carryOver = rotors[i].step();
            }
        }
    }

}












