package rs.ac.singidunum.Ciphers.enigma;

public class Reflector {
    private String ALPHABET;
    private int alphabetSize;
    private int[] mapper;


    public Reflector(String ALPHABET, String mappings) {
        this.ALPHABET = ALPHABET;
        this.alphabetSize = ALPHABET.length();
        this.mapper = new int[alphabetSize];
        generateMappings(mappings);
    }

    private void generateMappings(String mappings) {
        for (int i = 0; i < alphabetSize; i++) {
            char fromMapper = mappings.charAt(i);
            int fromALPHABET = ALPHABET.indexOf(fromMapper);
            mapper[i] = fromALPHABET;
        }
    }

    public int reflect(int inputPosition) {
        return mapper[inputPosition];
    }
}


