package rs.ac.singidunum.Dan1;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Bigrami {

    public static void main(String[] args) throws IOException {
        Path putanja = ucitajFajl();
        String original = Files.readString(putanja);

        System.out.println("ANALIZA ORIGINALNOG TEKSTA");
        List<String> plainTop = dohvatiTopBigrame(original, 10);
        prikaziListu(plainTop, "Top bigrami - original");

        String sifrat = cezar(original, 9);
        System.out.println("\nŠIFRAT (ključ +9)");
        List<String> cipherTop = dohvatiTopBigrame(sifrat, 10);
        prikaziListu(cipherTop, "Top bigrami - šifrat");


        System.out.println("ORIGINALNI TEKST\n");
        System.out.println(original.substring(0,Math.min(500, original.length())) + (original.length() > 500 ? "..." : ""));

        System.out.println("-------------------------\n");

        System.out.println("DELIMIČNO DEŠIFROVANJE (samo top 10 bigrama)");
        String delimicno = delimicnoDesifrovanje(sifrat, plainTop, cipherTop);
        System.out.println(delimicno.substring(0, Math.min(500, delimicno.length())) + (delimicno.length() > 500 ? "..." : ""));

        System.out.println("------------------\n");

        System.out.println("DELIMICNO DESIFROVANJE (samo top 50 bigrama)");
         List<String> plain50 = dohvatiTopBigrame(original, 50);
         List<String> cipher50 = dohvatiTopBigrame(sifrat, 50);
         prikaziListu(plain50,"Top bigrami - n = 50");
         String del50 = delimicnoDesifrovanje(sifrat, plain50, cipher50);
        System.out.println(del50.substring(0, Math.min(500, del50.length())) + (del50.length() > 500 ? "..." : ""));
    }

    public static List<String> dohvatiTopBigrame(String tekst, int n) {
        String slova = tekst.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        if (slova.length() < 2) return List.of();

        Map<String, Long> bigrami = new HashMap<>();
        for (int i = 0; i < slova.length() - 1; i++) {
            String bg = slova.substring(i, i + 2);
            bigrami.merge(bg, 1L, Long::sum);
        }

        return bigrami.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(n)
                .map(Map.Entry::getKey)
                .toList();
    }

    public static void prikaziListu(List<String> lista, String naslov) {
        System.out.printf("%s (%d):\n", naslov, lista.size());
        for (int i = 0; i < lista.size(); i++) {
            System.out.printf("%3d. %s\n", i + 1, lista.get(i));
        }
        System.out.println();
    }

    public static String delimicnoDesifrovanje(String sifrat, List<String> plainTop, List<String> cipherTop) {
        Map<Character, Character> mapa = new HashMap<>();

        int broj = Math.min(plainTop.size(), cipherTop.size());
        for (int i = 0; i < broj; i++) {
            String cBigram = cipherTop.get(i);
            String pBigram = plainTop.get(i);

            mapa.putIfAbsent(cBigram.charAt(0), pBigram.charAt(0));
            // Drugo slovo (samo ako se ne kosi sa prethodnim!)
            mapa.putIfAbsent(cBigram.charAt(1), pBigram.charAt(1));
        }

        StringBuilder rez = new StringBuilder();
        for (char c : sifrat.toUpperCase().toCharArray()) {
            if (Character.isLetter(c)) {
                Character zamena = mapa.get(c);
                if (zamena != null) {
                    if (Character.isLowerCase(sifrat.charAt(rez.length()))) {
                        rez.append(Character.toLowerCase(zamena));
                    } else {
                        rez.append(zamena);
                    }
                } else {
                    rez.append(c);
                }
            } else {
                rez.append(c);
            }
        }
        return rez.toString();
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

    public static Path ucitajFajl() throws IOException {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }
        return jfc.getSelectedFile().toPath();
    }
}