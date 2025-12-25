package rs.ac.singidunum.Dan6;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.Deflater;

public class Main {
    private static final int TARGET_LENGTH = 1000;
    private static final int BLOCK_SIZE = 100;
    private static final String[] TARGET_CIPHERS = {"eng_cipher.txt", "grc_cipher.txt", "spa_cipher.txt", "dut_cipher.txt"};

    public static void main(String[] args) throws IOException {
        ucitajFolderIPrikaziVerovatnoceCetrigrama(10);
    }

    public static void ucitajFolderIPrikaziVerovatnoceCetrigrama(int n) throws IOException {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle("Izaberite FOLDER koji sadrži fajlove šifrata (*_cipher.txt)");

        if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.out.println("Folder nije izabran.");
            return;
        }

        File folder = jfc.getSelectedFile();

        System.out.println("--- Generisanje Feature-a (Blokovi od " + BLOCK_SIZE + " karaktera) ---");

        // Glavna petlja: Prolazak kroz očekivane fajlove šifrata
        for (String cipherFileName : TARGET_CIPHERS) {

            Path cipherFilePath = Paths.get(folder.getAbsolutePath(), cipherFileName);
            File cipherFile = cipherFilePath.toFile();

            if (!cipherFile.exists() || cipherFile.length() == 0) {
                System.out.println("Upozorenje: Fajl šifrata ne postoji ili je prazan: " + cipherFileName + ". Preskacem.");
                continue;
            }

            String rawCiphertext = Files.readString(cipherFilePath, StandardCharsets.UTF_8);
            String normalizedCiphertext = rawCiphertext.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");

            // Ograničavanje na prvih 1000 karaktera za analizu
            String sourceText;
            if (normalizedCiphertext.length() >= TARGET_LENGTH) {
                sourceText = normalizedCiphertext.substring(0, TARGET_LENGTH);
            } else {
                sourceText = normalizedCiphertext;
            }

            if (sourceText.length() < BLOCK_SIZE) {
                System.out.println("Upozorenje: Šifrat " + cipherFileName + " je prekratak za blok analizu. Preskacem.");
                continue;
            }

            String languageName = cipherFileName.split("_")[0];

            for (int i = 0; i < sourceText.length(); i += BLOCK_SIZE) {
                int end = Math.min(i + BLOCK_SIZE, sourceText.length());
                String analyzedBlock = sourceText.substring(i, end);

                if (analyzedBlock.length() < BLOCK_SIZE) continue;


                // 1. Četirigrami
                List<Double> ver = izracunajVerovatnoceCetirigrama(analyzedBlock, n);

                // 2. Prosečna udaljenost
                double prosecnaUdaljenost = prosecnaUdaljenostNajfrekventnijegKaraktera(analyzedBlock);

                // 3. Stepen kompresije
                double stepenKompresije = stepenKompresijeBest(analyzedBlock);

                // 4. Kolmogorovska složenost
                double kolmogorovska = kolmogorovskaSpojeni(analyzedBlock, 30);

                // *** ISPIS (Instanca = jedan blok) ***

                // Ispis četirigram verovatnoća (Prvi deo)
                for (double p : ver) {
                    System.out.printf("%.10f, ", p);
                }

                // Ispis prosečne udaljenosti
                System.out.printf("%.4f", prosecnaUdaljenost);

                // Ispis stepena kompresije
                System.out.printf(", %.4f", stepenKompresije);

                // Ispis kolmogorovske složenosti
                System.out.printf(", %.4f", kolmogorovska);

                // Ispis klase (Jezika)
                System.out.println(", " + languageName.toUpperCase());
            }
        }
    }

    public static double kolmogorovskaSpojeni(String tekst, int velicinaBlokaBajtova) {
        byte[] tekstBytes = tekst.getBytes(StandardCharsets.UTF_8);

        double sumaKC = 0.0;
        int brojBlokova = 0;

        for (int i = 0; i < tekstBytes.length; i += velicinaBlokaBajtova) {
            int duzina = Math.min(velicinaBlokaBajtova, tekstBytes.length - i);
            byte[] blok = Arrays.copyOfRange(tekstBytes, i, i + duzina);

            // Kc sada vraća Kompresioni Ratio (~1.0)
            double kc = stepenKompresijeBestBlock(blok);
            sumaKC += kc;
            brojBlokova++;
        }

        return brojBlokova > 0 ? sumaKC / brojBlokova : 0.0;
    }

    public static List<Double> izracunajVerovatnoceCetirigrama(String tekst, int n) {
        String slova = tekst.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");
        int total = slova.length() - 3;

        if (total <= 0) return List.of();

        long[] count = new long[26 * 26 * 26 * 26];

        for (int i = 0; i < slova.length() - 3; i++) {
            int c1 = slova.charAt(i) - 'A';
            int c2 = slova.charAt(i + 1) - 'A';
            int c3 = slova.charAt(i + 2) - 'A';
            int c4 = slova.charAt(i + 3) - 'A';

            int idx = ((c1 * 26 + c2) * 26 + c3) * 26 + c4;
            count[idx]++;
        }

        double[] ver = new double[count.length];
        for (int i = 0; i < count.length; i++) {
            ver[i] = count[i] / (double) total;
        }

        return Arrays.stream(ver).boxed().sorted(Collections.reverseOrder()).limit(n).toList();
    }

    public static double prosecnaUdaljenostNajfrekventnijegKaraktera(String tekst) {
        List<String> top = dohvatiTopUnigrame(tekst, 1);
        if (top.isEmpty()) return 0.0;

        char najfrekventniji = top.get(0).charAt(0);

        String slova = tekst.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");

        List<Integer> indeksi = new ArrayList<>();
        for (int i = 0; i < slova.length(); i++) {
            if (slova.charAt(i) == najfrekventniji) {
                indeksi.add(i);
            }
        }

        if (indeksi.size() < 2) return 0.0;

        int sumaUdaljenosti = 0;
        for (int i = 1; i < indeksi.size(); i++) {
            sumaUdaljenosti += (indeksi.get(i) - indeksi.get(i - 1));
        }
        return sumaUdaljenosti / (double) (indeksi.size() - 1);
    }


    public static double stepenKompresijeBestBlock(byte[] block) {
        if (block.length == 0) return 0.0;

        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        deflater.setInput(block);
        deflater.finish();

        byte[] buffer = new byte[block.length * 2];
        int compressedSize = deflater.deflate(buffer);
        deflater.end();

        int originalSize = block.length;

        // Vraćamo Kompresioni Ratio (Compressed Size / Original Size)
        // Za šifrat će ova vrednost biti blizu 1.0 (npr. 0.95 do 1.05)
        double ratio = (double) compressedSize / originalSize;

        // Za Weka je bolje imati vrednost koja nije nula
        return ratio;
    }


    public static double stepenKompresijeBest(String tekst) {
        try {
            byte[] input = tekst.getBytes(StandardCharsets.UTF_8);

            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(input);
            deflater.finish();

            byte[] buffer = new byte[input.length];
            int compressedSize = deflater.deflate(buffer);
            deflater.end();

            int originalSize = input.length;

            if (originalSize == 0) return 0.0;

            return (1.0 - (compressedSize / (double) originalSize)) * 100.0;

        } catch (Exception e) {
            return 0.0;
        }
    }

    public static List<String> dohvatiTopUnigrame(String tekst, int n) {
        String slova = tekst.toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z]", "");

        if (slova.isEmpty()) return List.of();

        Map<Character, Long> unigrami = new HashMap<>();
        for (char c : slova.toCharArray()) {
            unigrami.merge(c, 1L, Long::sum);
        }

        long total = slova.length();

        return unigrami.entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
                .limit(n)
                .map(e -> e.getKey() + "  p=" + String.format("%.5f", e.getValue() / (double) total))
                .toList();
    }
}
