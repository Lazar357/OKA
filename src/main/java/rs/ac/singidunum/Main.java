package rs.ac.singidunum;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.Deflater;

public class Main {
    public static void main(String[] args) {
    }

    public static class Main6 {
        public static void main(String[] args) throws IOException {
            ucitajFolderIPrikaziVerovatnoceCetrigrama(10);
        }

        public static double prosecnaUdaljenostNajfrekventnijegKaraktera(String tekst) {
            // Dobij najfrekventniji karakter koristeći dohvatiTopUnigrame
            List<String> top = dohvatiTopUnigrame(tekst, 1);
            if (top.isEmpty()) return 0.0;

            // Prvi element sadrži najfrekventniji karakter na početku stringa
            char najfrekventniji = top.get(0).charAt(0);

            // Pretvori tekst u samo velika slova A-Z
            String slova = tekst.toUpperCase(Locale.ROOT).replaceAll("[^A-Z]", "");

            // Pronađi indekse pojavljivanja tog karaktera
            List<Integer> indeksi = new ArrayList<>();
            for (int i = 0; i < slova.length(); i++) {
                if (slova.charAt(i) == najfrekventniji) {
                    indeksi.add(i);
                }
            }

            // Ako postoji manje od 2 pojavljivanja, nema udaljenosti
            if (indeksi.size() < 2) return 0.0;

            // Izračunaj sumu udaljenosti
            int sumaUdaljenosti = 0;
            for (int i = 1; i < indeksi.size(); i++) {
                sumaUdaljenosti += (indeksi.get(i) - indeksi.get(i - 1));
            }
            // Prosečna udaljenost
            return sumaUdaljenosti / (double) (indeksi.size() - 1);
        }

        public static double kolmogorovska(File fajl, int velicinaBlokaBajtova) throws IOException {
            byte[] fajlBytes = Files.readAllBytes(fajl.toPath());

            double sumaKC = 0.0;
            int brojBlokova = 0;

            for (int i = 0; i < fajlBytes.length; i += velicinaBlokaBajtova) {
                int duzina = Math.min(velicinaBlokaBajtova, fajlBytes.length - i);
                byte[] blok = Arrays.copyOfRange(fajlBytes, i, i + duzina);
                double kc = stepenKompresijeBestBlock(blok);
                sumaKC += kc;
                brojBlokova++;
            }

            return brojBlokova > 0 ? sumaKC / brojBlokova : 0.0;
        }


        public static double stepenKompresijeBestBlock(byte[] block) {
            if (block.length == 0) return 0.0;

            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
            deflater.setInput(block);
            deflater.finish();

            byte[] buffer = new byte[block.length * 2]; // dovoljno veliko za kompresovane podatke
            int compressedSize = deflater.deflate(buffer);
            deflater.end();

            // KC između 0 i 1
            double kc = 1.0 - ((double) compressedSize / block.length);
            kc = Math.max(0.0, Math.min(1.0, kc));

            return kc;
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

        public static String tekstUBinarniString(String tekst) {
            StringBuilder sb = new StringBuilder();
            for (char c : tekst.toCharArray()) {
                // uzimamo samo donjih 8 bitova da bude kompatibilno sa ASCII
                sb.append(String.format("%8s", Integer.toBinaryString(c & 0xFF)).replace(' ', '0'));
            }
            return sb.toString();
        }

        public static void ucitajFolderIPrikaziVerovatnoceCetrigrama(int n) throws IOException {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                System.out.println("Folder nije izabran.");
                return;
            }

            File folder = jfc.getSelectedFile();
            File[] podfolderi = folder.listFiles(File::isDirectory);

            if (podfolderi == null || podfolderi.length == 0) {
                System.out.println("Nema podfoldera.");
                return;
            }

            for (File podfolder : podfolderi) {

                File[] fajlovi = podfolder.listFiles((dir, name) ->
                        name.toLowerCase().endsWith(".txt"));

                if (fajlovi == null || fajlovi.length == 0) {
                    continue;
                }

                for (File fajl : fajlovi) {

                    String text = Files.readString(fajl.toPath());
                    List<Double> ver = izracunajVerovatnoceCetirigrama(text, n);

                    // Ispis četirigram verovatnoća
                    for (double p : ver) {
                        System.out.printf("%.10f, ", p);
                    }

                    // Ispis prosečne udaljenosti najfrekventnijeg karaktera
                    double prosecnaUdaljenost = prosecnaUdaljenostNajfrekventnijegKaraktera(text);
                    System.out.print(prosecnaUdaljenost);

                    double stepenKompresije = stepenKompresijeBest(text);
                    System.out.print(", " + stepenKompresije);

                    double kolmogorovska = kolmogorovska(fajl, 300);
                    System.out.print(", " + kolmogorovska);

                    // Ispis imena podfoldera fajla
                    System.out.println(", " + podfolder.getName());
                }
                System.out.println();
            }

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

            // pretvaranje u verovatnoće
            double[] ver = new double[count.length];
            for (int i = 0; i < count.length; i++) {
                ver[i] = count[i] / (double) total;
            }

            // izdvajanje najvećih n
            return Arrays.stream(ver)
                    .boxed()
                    .sorted(Collections.reverseOrder())
                    .limit(n)
                    .toList();
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
}