package rs.ac.singidunum.Dan6;

import rs.ac.singidunum.Ciphers.M209Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;


public class CiphertextGenerator {

    private static final int[] FIXED_INITIAL_POSITIONS = {10, 5, 1, 15, 8, 2};
    private static final String KEY = Arrays.toString(FIXED_INITIAL_POSITIONS);
    private static final String[] LANGUAGES = {"ENG", "GRC", "SPA", "DUT"};
    private static final int TARGET_LENGTH = 10000; // Fiksna duzina

    private static String ROOT_PATH_FOR_SAVING = null;


    public static void main(String[] args) {

        String rootPath = selectRootDirectory();

        if (rootPath == null) {
            System.out.println("Obrada je prekinuta.");
            return;
        }

        ROOT_PATH_FOR_SAVING = rootPath;

        System.out.println("Korisceni kljuc (pozicije tockova): " + KEY);
        System.out.println("Roditeljski direktorijum: " + rootPath);
        System.out.println("--- Generisanje sifrovanih poruka (Max " + TARGET_LENGTH + " karaktera) ---");

        for (String lang : LANGUAGES) {
            try {
                String fullPath = Paths.get(rootPath, lang).toString();

                String mergedText = mergeTextsFromFolder(fullPath);

                String normalizedText = mergedText.toUpperCase().replaceAll("[^A-Z]", "");

                String finalTextToEncrypt;
                if (normalizedText.length() >= TARGET_LENGTH) {
                    finalTextToEncrypt = normalizedText.substring(0, TARGET_LENGTH);
                } else {
                    finalTextToEncrypt = normalizedText;
                }

                if (finalTextToEncrypt.isEmpty()) {
                    System.out.println("UPOZORENJE: " + lang + " je prazan nakon normalizacije. Preskacem.");
                    continue;
                }

                // Sifrovanje
                M209Cipher cipher = new M209Cipher(FIXED_INITIAL_POSITIONS);
                String ciphertext = cipher.encrypt(finalTextToEncrypt);

                // 5. Cuvanje sifrovanog teksta U IZABRANOM ROOT FOLDERU!
                String ciphertextPath = Paths.get(ROOT_PATH_FOR_SAVING, lang.toLowerCase() + "_cipher.txt").toString();
                Files.write(Paths.get(ciphertextPath), ciphertext.getBytes());

                System.out.printf("Sifrovana poruka za %s (Duzina: %d) sacuvana u %s\n",
                        lang, finalTextToEncrypt.length(), ciphertextPath);

            } catch (IOException e) {
                System.err.println("Greska pri obradi foldera " + lang + ". Proverite putanju i fajlove: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Greska M209 konfiguracije: " + e.getMessage());
            }
        }

        JOptionPane.showMessageDialog(null, "Generisanje šifrata završeno. Fajlovi su sačuvani u: " + ROOT_PATH_FOR_SAVING);
    }

    private static void findTextFilesRecursively(File directory, List<File> fileList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findTextFilesRecursively(file, fileList);
                } else if (file.isFile()) {
                    fileList.add(file);
                }
            }
        }
    }

    private static String mergeTextsFromFolder(String folderPath) throws IOException {
        File rootFolder = new File(folderPath);
        if (!rootFolder.isDirectory()) {
            throw new IOException("Putanja nije direktorijum: " + folderPath);
        }

        List<File> allFiles = new ArrayList<>();
        findTextFilesRecursively(rootFolder, allFiles);

        if (allFiles.isEmpty()) {
            throw new IOException("Folder " + folderPath + " i njegovi podfolderi ne sadrže fajlove.");
        }

        return allFiles.stream()
                .map(file -> {
                    try {
                        return new String(Files.readAllBytes(file.toPath()));
                    } catch (IOException e) {
                        System.err.println("Nije moguce procitati fajl: " + file.getName());
                        return "";
                    }
                })
                .collect(Collectors.joining(" "));
    }


    private static String selectRootDirectory() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Izaberite FOLDER koji sadrži podfoldere ENG, GRC, SPA, DUT");

        int result = chooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        } else {
            return null;
        }
    }
}