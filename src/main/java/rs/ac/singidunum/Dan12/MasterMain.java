package rs.ac.singidunum.Dan12;

import rs.ac.singidunum.Ciphers.enigma.EnigmaCipher;
import rs.ac.singidunum.Weka.FileMaker;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MasterMain {
    public static void main(String[] args) throws Exception {
        String ALPHABET = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz 0123456789,.<>;':\"\\|`~!@#$%^&*()_+-=";
        int n = 10;
        int brojRotora = 25;

        // INICIJALIZACIJA ZA ENIGMU
//        FileMaker.setupEnigmaAttributes(n);

        // ... generisanje rotora i reflektora (isto kao pre) ...
        String[] rotors = new String[brojRotora];
        for (int i = 0; i < brojRotora; i++) rotors[i] = shuffleString(ALPHABET);
        String reflector = reflectorGenerator(ALPHABET);

        String keyA = "KljucZaPrviDatasetAAAAAA_123".substring(0, brojRotora);
        String keyB = "KljucZaDrugiDatasetBBBBBB_123".substring(0, brojRotora);

        EnigmaCipher ec1 = new EnigmaCipher(ALPHABET, "bq cr", reflector, rotors, keyA);
        EnigmaCipher ec2 = new EnigmaCipher(ALPHABET, "bq cr", reflector, rotors, keyB);

        byte[] allBytes = Files.readAllBytes(Path.of("shakespeare 2.txt"));

        for (int i = 0; i < allBytes.length; i += 1024) {
            int end = Math.min(i + 1024, allBytes.length);
            byte[] block = Arrays.copyOfRange(allBytes, i, end);
            String rawString = new String(block, StandardCharsets.ISO_8859_1);

            // Filtriranje da bi se izbegao IndexOutOfBounds -1
            StringBuilder filtered = new StringBuilder();
            for (char c : rawString.toCharArray()) {
                if (ALPHABET.indexOf(c) != -1) filtered.append(c);
            }
            if (filtered.length() < 100) continue;

            // Ekstrakcija sa novom metodom
            String cA = ec1.encrypt(filtered.toString());
            FileMaker.finalDataset.add(FileMaker.extractCipherFeatures(cA.getBytes(StandardCharsets.ISO_8859_1), "k1", n));

            String cB = ec2.encrypt(filtered.toString());
            FileMaker.finalDataset.add(FileMaker.extractCipherFeatures(cB.getBytes(StandardCharsets.ISO_8859_1), "k2", n));
        }

        FileMaker.saveArff(FileMaker.finalDataset, "Kriptoanaliza/Enigma_Master.arff");


    }

    public static String shuffleString(String text) {
        List<Character> chars = new ArrayList<>();
        for (char c : text.toCharArray()) chars.add(c);
        Collections.shuffle(chars);
        StringBuilder sb = new StringBuilder();
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    public static String reflectorGenerator(String ALPHABET) {
        List<Integer> idx = new ArrayList<>();
        for (int i = 0; i < ALPHABET.length(); i++) idx.add(i);
        Collections.shuffle(idx);
        char[] map = new char[ALPHABET.length()];
        for (int i = 0; i < idx.size(); i += 2) {
            if (i + 1 == idx.size()) {
                map[idx.get(i)] = ALPHABET.charAt(idx.get(i));
                break;
            }
            map[idx.get(i)] = ALPHABET.charAt(idx.get(i + 1));
            map[idx.get(i + 1)] = ALPHABET.charAt(idx.get(i));
        }
        return new String(map);
    }
}