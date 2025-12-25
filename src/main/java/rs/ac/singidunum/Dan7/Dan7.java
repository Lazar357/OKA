package rs.ac.singidunum.Dan7;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class Dan7 {

    public static void main(String[] args) throws IOException {
//
//        JFileChooser jfc =  new JFileChooser();
//        jfc.showOpenDialog(jfc);
//        String file = jfc.getSelectedFile().getAbsolutePath();
//        byte[] fileBytes = Files.readAllBytes(Path.of(file));
//
//        double[] podaci = byte2double(fileBytes);
//
//        double entropy = Entropy.calculateEntropy(podaci);
//
//        System.out.println("Entropy: " + entropy);

        ucitajFolderIzracunajEntropiju();
    }

    public static void ucitajFolderIzracunajEntropiju() throws IOException {

        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (jfc.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.out.println("Folder nije izabran.");
            return;
        }

        Path startDir = jfc.getSelectedFile().toPath();

        // Mapa za čuvanje statistika entropije. Koristimo String za ključeve
        // jer želimo da grupišemo rezultate po imenima podfoldera prvog nivoa.
        Map<String, FolderEntropyStats> firstLevelFolderStats = new HashMap<>();

        System.out.println("Počinjem obradu direktorijuma: " + startDir);

        // Korišćenje Files.walk za rekurzivnu pretragu
        try (Stream<Path> walk = Files.walk(startDir)) {
            walk.filter(Files::isRegularFile) // Filtriramo samo regularne datoteke
                    .filter(p -> p.toString().toLowerCase().endsWith(".txt")) // Filtriramo samo .txt fajlove
                    .forEach(filePath -> {
                        try {
                            // Ključna logika: Pronalaženje imena podfoldera prvog nivoa
                            String firstLevelFolderName = getFirstLevelParentName(startDir, filePath);

                            // Ako fajl nije direktno u korenu ili podfolderu (ne bi trebalo), preskačemo ga
                            if (firstLevelFolderName == null) {
                                return;
                            }

                            // Izračunavanje i skladištenje entropije pod ključem prvog nivoa
                            calculateAndStoreEntropy(filePath, firstLevelFolderName, firstLevelFolderStats);

                        } catch (IOException e) {
                            System.err.println("Greška pri čitanju fajla " + filePath + ": " + e.getMessage());
                        }
                    });
        }

        System.out.println("\n--- REZULTATI PROSEČNE ENTROPIJE PO GLAVNIM FOLDERIMA ---");

        // 3. Ispis rezultata
        firstLevelFolderStats.forEach((folderName, stats) -> {
            double avgEntropy = stats.getAverageEntropy();

            System.out.printf("%s: %.4f (Ukupno obrađenih fajlova u podstrukturi: %d)\n",
                    folderName, avgEntropy, stats.fileCount);
        });
    }

    private static String getFirstLevelParentName(Path startDir, Path filePath) {
        Path parent = filePath.getParent();
        Path relativePath = startDir.relativize(parent);

        // Relativna putanja bi bila npr. "SPA/Knjiga1" ili "SPA"

        if (relativePath.getNameCount() == 0) {
            // Fajl je direktno u startDir (korenom folderu)
            return startDir.getFileName().toString() + " (ROOT)";
        }

        // Vraćamo prvi element relativne putanje (npr. "SPA" iz "SPA/Knjiga1")
        return relativePath.getName(0).toString();
    }

    private static void calculateAndStoreEntropy(Path filePath, String folderKey, Map<String, FolderEntropyStats> folderStats) throws IOException {

        byte[] fileBytes = Files.readAllBytes(filePath);
        double[] podaci = byte2double(fileBytes);
        double entropy = Entropy.calculateEntropy(podaci);

        // Skladištenje rezultata pod ključem prvog nivoa
        FolderEntropyStats stats = folderStats.computeIfAbsent(folderKey, k -> new FolderEntropyStats());
        stats.addEntropy(entropy);
    }


    public static double[] byte2double(byte[] podaci) {
        double[] r = new double[podaci.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = podaci[i];
        }
        return r;
    }


}
