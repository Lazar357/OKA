package rs.ac.singidunum.Dan1;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Unigrami {
    public static void main(String[] args) throws IOException {
        Path putanja = ucitajFajl();
        String original = Files.readString(putanja);

        prikaziUnigrame(original, 20, "Top 20 unigrama");

        String sifrat = cezar(original, 7);

        prikaziUnigrame(sifrat, 20, "Top 20 unigrama sifrata");

        var plainTop = dohvatiTopUnigrame(original, 20);
        var cipherTop = dohvatiTopUnigrame(sifrat, 20);

        JFileChooser jfc = new JFileChooser();
        jfc.showOpenDialog(jfc);
        String dicPath = jfc.getSelectedFile().getAbsolutePath();
        List<String> reci = Files.readAllLines(Path.of(dicPath));

        String delimicno = delimicnoDesifrovanje(sifrat, plainTop, cipherTop);

        List<String> pogodci = nadjiPogotke(delimicno, reci);

        System.out.println("Pronađene reči iz rečnika:");
        pogodci.forEach(System.out::println);

    }

    public static List<String> nadjiPogotke(String tekst, List<String> recnik) {
        // Očisti tekst samo na reči
        String[] reciUTekstu = tekst.toLowerCase().split("[^a-z]+");

        Set<String> set = new HashSet<>(Arrays.asList(reciUTekstu));

        List<String> pogodci = new ArrayList<>();

        for (String rec : recnik) {
            String r = rec.toLowerCase().trim();
            if (set.contains(r)) {
                pogodci.add(r);
            }
        }

        return pogodci;
    }


    public static List<Map.Entry<Character, Long>> dohvatiTopUnigrame(String tekst, int n) {
        String slova = tekst.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z]", "");

        if (slova.isEmpty()) return List.of();

        Map<Character, Long> unigrami = new HashMap<>();
        for (char c : slova.toCharArray()) {
            unigrami.merge(c, 1L, Long::sum);
        }

        return unigrami.entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
                .limit(n)
                .toList();
    }

    public static void prikaziUnigrame(String tekst, int brojNajcescih, String naslov) {
        String slova = tekst.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        long ukupnoSlova = slova.length();

        if (ukupnoSlova == 0) {
            System.out.println("Nema slova za analizu.");
            return;
        }

        var top = dohvatiTopUnigrame(tekst, brojNajcescih);

        System.out.printf("\n=== %s ===\n", naslov);
        System.out.printf("Ukupno slova: %,d\n", ukupnoSlova);
        System.out.println("  #   Slovo   Broj      %");
        System.out.println("─────────────────────────────");

        for (int i = 0; i < top.size(); i++) {
            char slovo = top.get(i).getKey();
            long broj = top.get(i).getValue();
            double procenat = 100.0 * broj / ukupnoSlova;
            System.out.printf("%3d.   %c    %,6d   %6.2f%%\n", i + 1, slovo, broj, procenat);
        }
        System.out.println();
    }

    public static String delimicnoDesifrovanje(String sifrat, List<Map.Entry<Character, Long>> plainTop, List<Map.Entry<Character, Long>> cipherTop) {

        Map<Character, Character> mapa = new HashMap<>();

        int n = Math.min(plainTop.size(), cipherTop.size());
        for (int i = 0; i < n; i++) {
            char cipherChar = cipherTop.get(i).getKey();
            char plainChar = plainTop.get(i).getKey();
            mapa.putIfAbsent(cipherChar, plainChar);
        }

        StringBuilder rez = new StringBuilder();
        for (int i = 0; i < sifrat.length(); i++) {
            char original = sifrat.charAt(i);
            char c = Character.toUpperCase(original);

            if (Character.isLetter(c)) {
                Character zamena = mapa.get(c);
                if (zamena != null) {
                    rez.append(Character.isLowerCase(original)
                            ? Character.toLowerCase(zamena)
                            : zamena);
                } else {
                    rez.append(original);
                }
            } else {
                rez.append(original);
            }
        }
        return rez.toString();
    }

    public static Path ucitajFajl() throws IOException {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        return jfc.getSelectedFile().toPath();
    }

    public static String cezar(String tekst, int pomeraj) {
        StringBuilder sb = new StringBuilder();
        pomeraj = ((pomeraj % 26) + 26) % 26;
        for (char c : tekst.toCharArray()) {
            if (Character.isLetter(c)) {
                char osnova = Character.isUpperCase(c) ? 'A' : 'a';
                char novi = (char) (osnova + (c - osnova + pomeraj) % 26);
                sb.append(novi);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

}
