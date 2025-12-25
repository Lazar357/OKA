package rs.ac.singidunum.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class FakeFileGenerator {

    /**
     * Generiše Base64 fajlove sa PRAVIM Magic Numbers za ML trening
     *
     * @param outputDir Osnovni direktorijum (npr. "dataset")
     * @param filesPerType Broj fajlova po tipu (preporuka: 100+)
     */
    public static void generateFakeFiles(String outputDir, int filesPerType) throws Exception {
        Random rnd = new Random();

        // Magic Numbers za svaki tip fajla
        Map<String, MagicInfo> fileTypes = new HashMap<>();

        // ZIP fajlovi - PK signature
        fileTypes.put("zip", new MagicInfo(
                new byte[]{0x50, 0x4B, 0x03, 0x04},
                "Local file header signature"
        ));

        // PNG - PNG signature
        fileTypes.put("png", new MagicInfo(
                new byte[]{(byte)0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A},
                "PNG image"
        ));

        // JPEG - JFIF signature
        fileTypes.put("jpg", new MagicInfo(
                new byte[]{(byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0},
                "JPEG/JFIF image"
        ));

        // EXE - DOS MZ executable
        fileTypes.put("exe", new MagicInfo(
                new byte[]{0x4D, 0x5A, (byte)0x90, 0x00, 0x03, 0x00, 0x00, 0x00},
                "Windows executable"
        ));

        // DLL - takođe MZ (isti kao EXE)
        fileTypes.put("dll", new MagicInfo(
                new byte[]{0x4D, 0x5A, (byte)0x90, 0x00, 0x03, 0x00, 0x00, 0x00},
                "Windows DLL library"
        ));

        // PDF - %PDF-
        fileTypes.put("pdf", new MagicInfo(
                new byte[]{0x25, 0x50, 0x44, 0x46, 0x2D, 0x31, 0x2E},
                "PDF document"
        ));

        System.out.println("=== GENERISANJE LAŽNIH FAJLOVA ===\n");

        for (var entry : fileTypes.entrySet()) {
            String type = entry.getKey();
            MagicInfo info = entry.getValue();
            byte[] magic = info.magicBytes;

            System.out.println("Generišem " + filesPerType + " " + type.toUpperCase() + " fajlova...");
            System.out.println("  Magic: " + bytesToHex(magic));
            System.out.println("  Opis: " + info.description);

            // Kreiraj folder
            String folderPath = outputDir + "/" + type;
            new File(folderPath).mkdirs();

            for (int i = 1; i <= filesPerType; i++) {
                // Varijabilna veličina (1KB - 10KB)
                int size = 1000 + rnd.nextInt(9000);
                byte[] fakeFile = new byte[size];

                // Popuni random podacima
                rnd.nextBytes(fakeFile);

                // KRITIČNO: Upiši PRAVI Magic Number na početak
                System.arraycopy(magic, 0, fakeFile, 0, magic.length);

                // Dodatna karakteristika: simuliraj različitu entropiju
                if (type.equals("zip") || type.equals("exe")) {
                    // ZIP i EXE obično imaju VISOKU entropiju (kompresovani)
                    // Random bajtovi su već dovoljni
                } else if (type.equals("pdf")) {
                    // PDF ima SREDNJU entropiju (delimično kompresovan)
                    for (int j = magic.length; j < size; j += 50) {
                        fakeFile[j] = (byte)rnd.nextInt(128); // Delimično ASCII
                    }
                }

                // Base64 enkodovanje
                String b64 = Base64.getEncoder().encodeToString(fakeFile);

                // Sačuvaj u .txt fajl
                String filename = String.format("%s/1 (%d).txt", folderPath, i);
                Files.write(Paths.get(filename), b64.getBytes());
            }

            System.out.println("  ✓ Generisano: " + filesPerType + " fajlova\n");
        }

        System.out.println("=== ZAVRŠENO ===");
        System.out.println("Ukupno: " + (filesPerType * fileTypes.size()) + " fajlova");
        System.out.println("Lokacija: " + new File(outputDir).getAbsolutePath());
    }

    // Helper klasa za čuvanje informacija
    static class MagicInfo {
        byte[] magicBytes;
        String description;

        MagicInfo(byte[] magicBytes, String description) {
            this.magicBytes = magicBytes;
            this.description = description;
        }
    }

    // Konverzija bajtova u HEX string
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    // MAIN metoda za pokretanje
    public static void main(String[] args) {
        try {
            // Generiši 100 fajlova po tipu u "dataset" folderu
            generateFakeFiles("dataset", 100);

            System.out.println("\n✅ Sada možeš koristiti 'dataset' folder u GUI-ju!");

        } catch (Exception e) {
            System.err.println("❌ Greška: " + e.getMessage());
            e.printStackTrace();
        }
    }
}