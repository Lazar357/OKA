package rs.ac.singidunum.Dan7;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.Deflater;

public class Main {
    private static final int TARGET_LENGTH = 1000;
    private static final int BLOCK_SIZE = 100;
    private static final String[] TARGET_CIPHERS = {"eng_cipher.txt", "grc_cipher.txt", "spa_cipher.txt", "dut_cipher.txt"};

    public static void main(String[] args) throws IOException {
        ucitajFolderIPrikaziVerovatnoceCetrigrama(10);
    }

    public static double izracunajEntropijuDivergencije(String block) {
        // Ako već imate funkciju izracunajEntropijuBloka(String block)
        double entropijaBloka = izracunajEntropijuBloka(block);

        // Entropija je uvek u odnosu na JEDAN KARAKTER, pa je alfabet veličine 26.
        final int ALPHABET_SIZE = 26; // A-Z
        double h_max = Math.log(ALPHABET_SIZE) / Math.log(2.0); // log2(26)

        // Formula koja meri koliko je blizu entropiji H_max.
        // Jedna varijanta (Normalizovana Entropija): entropijaBloka / h_max
        // Druga (Divergencija, slična H(C|R)): h_max - entropijaBloka

        // Koristimo Normalizovanu Entropiju, jer je najčešći feature (0.0 do 1.0)
        return entropijaBloka / h_max;
    }

    public static double izracunajEntropijuBloka(String block) {
        if (block == null || block.isEmpty()) {
            return 0.0;
        }

        // 1. Izračunavanje frekvencije svakog karaktera
        Map<Character, Integer> freqMap = new HashMap<>();
        int totalLength = block.length();

        for (int i = 0; i < totalLength; i++) {
            char c = block.charAt(i);
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }

        // 2. Računanje entropije H(X) = - Σ [P(x) * log₂(P(x))]
        double entropy = 0.0;

        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()) {
            int count = entry.getValue();
            double probability = (double) count / totalLength;

            if (probability > 0) {
                // Formula za entropiju: H(X) = - Σ P(x) log₂(P(x))
                // U Javi: log₂(x) = ln(x) / ln(2)
                entropy -= probability * (Math.log(probability) / Math.log(2.0));
            }
        }

        return entropy;
    }

    public static void ucitajFolderIPrikaziVerovatnoceCetrigrama(int n) throws IOException {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setDialogTitle("Izaberite ROOT FOLDER koji sadrži podfoldere jezika (npr. ENG, SPA)");

        if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.out.println("Folder nije izabran.");
            return;
        }

        Path startDir = jfc.getSelectedFile().toPath();

        System.out.println("--- Generisanje Feature-a (Blokovi od " + BLOCK_SIZE + " karaktera) ---");

        try (Stream<Path> walk = Files.walk(startDir)) {

            walk.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".txt"))
                    .forEach(filePath -> { // filePath predstavlja putanju do svakog .txt fajla

                        String fileName = filePath.getFileName().toString();

                        try {
                            if (Files.size(filePath) == 0) {
                                System.out.println("Upozorenje: Fajl je prazan: " + fileName + ". Preskacem.");
                                return;
                            }

                            // *** 1. ODREĐIVANJE IMENA KLASE (JEZIKA) ***
                            // Koristićemo logiku za dobijanje roditelja prvog nivoa u odnosu na startDir.
                            String languageName = getFirstLevelParentName(startDir, filePath);

                            if (languageName == null || languageName.contains("ROOT")) {
                                System.out.println("Upozorenje: Fajl " + fileName + " je direktno u korenu ili se ne može klasifikovati. Preskacem.");
                                return;
                            }

                            // Čitanje i normalizacija teksta
                            String rawText = Files.readString(filePath, StandardCharsets.UTF_8);

                            // *** 2. REGEX ZA UKLANJANJE SVEGA OSIM A-Z I KONVERZIJA U VELIKA SLOVA ***
                            // Stari regex: .replaceAll("[^A-Z]", "") uspešno uklanja sve, uključujući razmake.
                            // Novi regex koji eksplicitno uklanja razmake (i sve ostalo osim A-Z):
                            String normalizedText = rawText.toUpperCase(Locale.ROOT)
                                    .replaceAll("[^A-Z]", "");

                            // Provera, mada je regex već uklonio razmake, [^A-Z] uklanja i ' '
                            // Ako želite da budete apsolutno sigurni da su razmaci uklonjeni, stari regex je OK.
                            // Ako želite da eksplicitno navedete, možete koristiti npr. .replaceAll("[^A-Z\s]", "").replaceAll("\\s+", "");
                            // Ali, u kontekstu kripografske analize gde se koristi samo A-Z, [^A-Z] je najčišći.

                            String sourceText;
                            if (normalizedText.length() >= TARGET_LENGTH) {
                                sourceText = normalizedText.substring(0, TARGET_LENGTH);
                            } else {
                                sourceText = normalizedText;
                            }

                            if (sourceText.length() < BLOCK_SIZE) {
                                System.out.println("Upozorenje: Tekst u fajlu " + fileName + " je prekratak za blok analizu. Preskacem.");
                                return;
                            }

                            // Kreiranje instanci (blokova)
                            for (int i = 0; i < sourceText.length(); i += BLOCK_SIZE) {
                                int end = Math.min(i + BLOCK_SIZE, sourceText.length());
                                String analyzedBlock = sourceText.substring(i, end);

                                if (analyzedBlock.length() < BLOCK_SIZE) continue;

                                // *** IZRAČUNAVANJE FEATURE-a ***
                                List<Double> ver = izracunajVerovatnoceCetirigrama(analyzedBlock, n);
                                double prosecnaUdaljenost = prosecnaUdaljenostNajfrekventnijegKaraktera(analyzedBlock);
                                double stepenKompresije = stepenKompresijeBest(analyzedBlock);
                                double kolmogorovska = kolmogorovskaSpojeni(analyzedBlock, 30);
                                double entropija = izracunajEntropijuBloka(analyzedBlock);
                                // *** NOVI FEATURE OVDE ***
                                double uslovnaEntropijaRandom = izracunajEntropijuDivergencije(analyzedBlock);


                                // *** ISPIS (Instanca = jedan blok) ***

                                // 1. Četirigram verovatnoće
                                for (double p : ver) {
                                    System.out.printf("%.10f, ", p);
                                }

                                // 2. Prosečna udaljenost
                                System.out.printf("%.4f", prosecnaUdaljenost);

                                // 3. Stepen kompresije
                                System.out.printf(", %.4f", stepenKompresije);

                                // 4. Kolmogorovska složenost
                                System.out.printf(", %.4f", kolmogorovska);

                                // 5. Entropija
                                System.out.printf(", %.4f", entropija);

                                // *** NOVI FEATURE ISPIS OVDE ***
                                System.out.printf(", %.4f", uslovnaEntropijaRandom);

                                // 6. Klasa (Jezik: ENG, SPA, DUT...)
                                System.out.println(", " + languageName.toUpperCase());
                            }

                        } catch (IOException e) {
                            System.err.println("Greška pri čitanju fajla " + fileName + ": " + e.getMessage());
                        }
                    });
        }
    }

    private static String getFirstLevelParentName(Path startDir, Path filePath) {

        // Provera da li je fajl unutar startDir
        if (!filePath.startsWith(startDir)) {
            return null;
        }

        Path relativePath = startDir.relativize(filePath);

        // Ako je fajl direktno u startDir: npr. startDir/fajl.txt. Relativna putanja je samo ime fajla.
        if (relativePath.getParent() == null) {
            return startDir.getFileName().toString() + " (ROOT)";
        }

        // Ako je fajl unutar podfoldera (npr. ENG/book1.txt)
        Path firstSegment = relativePath.getName(0);

        // Provera da li je prvi segment fajl ili folder. Ako je folder, to je naš jezik.
        // Ako putanja ima više od jednog segmenta (npr. ENG/book1.txt), prvi segment je "ENG"
        if (relativePath.getNameCount() > 1) {
            return firstSegment.toString();
        }

        // Ako je relativna putanja samo "fajl.txt", vraća se ROOT.
        return startDir.getFileName().toString() + " (ROOT)";
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
