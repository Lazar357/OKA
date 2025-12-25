package rs.ac.singidunum.Ciphers.enigma;

public class Rotor {
    private String ALPHABET;
    private int alphabetSize;
    private int[] forwardMapping;
    private int[] backwordsMapping;
    private int position;

    public Rotor(String ALPHABET, String mapping) {
        this.ALPHABET = ALPHABET;
        this.alphabetSize = ALPHABET.length();
        this.forwardMapping = new int[alphabetSize];
        this.backwordsMapping = new int[alphabetSize];
        generateMappings(mapping);
    }

    private void generateMappings(String mapping) {
        for (int i = 0; i < alphabetSize; i++) {
            char fromMapper = mapping.charAt(i);
            int fromALPHABET = ALPHABET.indexOf(fromMapper);
            forwardMapping[i] = fromALPHABET;
        }

        for (int i = 0; i < alphabetSize; i++) {
            backwordsMapping[forwardMapping[i]] = i;
        }
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean step() {
        position = (position + 1) % alphabetSize;
        return position == 0;
    }

    public int forward(int inputPosition) {
        int shiftedPosition = (inputPosition + position) % alphabetSize;
        int mapperPosition = forwardMapping[shiftedPosition];
        return (mapperPosition - position + alphabetSize) % alphabetSize;
    }

    public int backwords(int inputPosition) {
        int shiftedPosition = (inputPosition + position) % alphabetSize;
        int mapperPosition = backwordsMapping[shiftedPosition];
        return (mapperPosition - position + alphabetSize) % alphabetSize;
    }
}
