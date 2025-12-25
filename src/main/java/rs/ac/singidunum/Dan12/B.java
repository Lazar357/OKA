//package rs.ac.singidunum.Dan12;
//
//import rs.ac.singidunum.Ciphers.enigma.EnigmaCipher;
//import rs.ac.singidunum.Weka.FileMaker;
//import weka.core.Instances;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//public class B {
//    static void main(String[] args) throws IOException {
//        String ALPHABET = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz 0123456789,.<>;':\"\\|`~!@#$%^&*()_+-=";
//        int brojRotora = 25;
//        String[] rotors = new String[brojRotora];
//        String reflectorMappings = reflectorGenerator(ALPHABET);
//        String key = "SuperTajniKljucBBB1234567";
//        String plugboard = "bq cr";
//
//        for (int i = 0; i < brojRotora; i++) {
//            rotors[i] = shuffleString(ALPHABET);
//        }
//
//        EnigmaCipher ec = new EnigmaCipher(
//                ALPHABET,
//                plugboard,
//                reflectorMappings,
//                rotors,
//                key
//        );
//
//        Instances dataset = FileMaker.getFinalDataset();
//        byte[] allBytes = Files.readAllBytes(Path.of("shakespeare 2.txt"));
//
//        for (int i = 0; i < allBytes.length; i += 1024) {
//            int end = Math.min(i + 1024, allBytes.length);
//            byte[] block = Arrays.copyOfRange(allBytes, i, end);
//
//            // 1. Pretvori bajtove u String
//            String rawString = new String(block, StandardCharsets.ISO_8859_1);
//
//            // 2. FILTRIRANJE: Zadrži samo one karaktere koji su u tvom ALPHABET-u
//            StringBuilder filtered = new StringBuilder();
//            for (char c : rawString.toCharArray()) {
//                if (ALPHABET.indexOf(c) != -1) {
//                    filtered.append(c);
//                }
//            }
//
//            // 3. Šifruj samo pročišćen tekst
//            String encrypted = ec.encrypt(filtered.toString());
//
//            dataset.add(FileMaker.extractFeaturesFromBytes(encrypted.getBytes(StandardCharsets.ISO_8859_1), dataset));
//        }
//        FileMaker.saveArff(dataset, "Kriptoanaliza/Dataset_A.arff");
//    }
//
//    public static String shuffleString(String text) {
//        List<Character> chars = new ArrayList<>();
//        for (char c : text.toCharArray()) {
//            chars.add(c);
//        }
//
//        Collections.shuffle(chars);
//
//        StringBuilder result = new StringBuilder();
//        for (char c : chars) {
//            result.append(c);
//        }
//
//        return result.toString();
//    }
//
//    public static String reflectorGenerator(String ALPHABET) {
//        int alphabetLength = ALPHABET.length();
//
//        List<Integer> indexes = new ArrayList<>();
//        for (int i = 0; i < alphabetLength; i++) {
//            indexes.add(i);
//        }
//        Collections.shuffle(indexes);
//
//        int[] map = new int[alphabetLength];
//        for (int i = 0; i < alphabetLength; i += 2) {
//            int charIndex1 = indexes.get(i);
//            if (i + 1 == alphabetLength) {
//                map[charIndex1] = charIndex1;
//                break;
//            }
//            int charIndex2 = indexes.get(i + 1);
//            map[charIndex1] = charIndex2;
//            map[charIndex2] = charIndex1;
//
//
//        }
//        StringBuilder sb = new StringBuilder();
//        for (int j : map) {
//            sb.append(ALPHABET.charAt(j));
//        }
//        return sb.toString();
//    }
//}
