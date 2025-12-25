package rs.ac.singidunum.Weka;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.zip.Deflater;

public class FileMaker {
    public static Instances datasetStructure;
    public static Instances finalDataset;

    /**
     * Setup za detekciju ekstenzija - 10 atributa + klasa
     */
    public static void setupExtensionAttributes(List<String> klase) {
        ArrayList<Attribute> attrs = new ArrayList<>();

        // Atributi 0-7: Prvih 8 bajtova (Magic Numbers)
        for (int i = 1; i <= 8; i++) {
            attrs.add(new Attribute("byte_" + i));
        }

        // Atribut 8: Entropija
        attrs.add(new Attribute("entropy"));

        // Atribut 9: Veličina fajla
        attrs.add(new Attribute("file_size"));

        // Atribut 10: Klasa (ekstenzija)
        attrs.add(new Attribute("klasa", klase));

        finalDataset = new Instances("ExtensionDataset", attrs, 0);
        finalDataset.setClassIndex(finalDataset.numAttributes() - 1);
    }

    /**
     * Ekstrakcija features za detekciju ekstenzija
     * AUTOMATSKI DEKODUJE Base64 ako detektuje format
     */
    public static Instance extractExtensionFeatures(File f, String label) throws Exception {
        byte[] rawData = Files.readAllBytes(f.toPath());
        byte[] processedData;

        // DETEKCIJA: Da li je fajl Base64 TEXT ili BINARNI fajl?
        boolean isTextFile = f.getName().endsWith(".txt") || f.getName().endsWith(".b64");

        if (isTextFile) {
            // Pokušaj dekodiranje Base64
            String content = new String(rawData, StandardCharsets.ISO_8859_1).trim();

            // Čisti Base64 format
            if (content.matches("^[A-Za-z0-9+/=\\s]+$") && content.length() > 20) {
                try {
                    String cleanB64 = content.replaceAll("\\s+", "");
                    processedData = Base64.getDecoder().decode(cleanB64);
                    System.out.println("[Base64] ✓ Dekodovano: " + f.getName() + " → " + processedData.length + " bytes");
                } catch (IllegalArgumentException e) {
                    System.err.println("[Base64] ✗ Nevažeći format: " + f.getName());
                    processedData = rawData;
                }
            } else {
                processedData = rawData;
            }
        } else {
            // Direktni binarni fajl (JPG, PNG, ZIP, EXE...)
            processedData = rawData;
            System.out.println("[Binary] ✓ Direktno učitan: " + f.getName() + " → " + processedData.length + " bytes");
        }

        double[] values = new double[finalDataset.numAttributes()];

        // 1. MAGIC NUMBERS (Prvih 8 bajtova = "DNK" fajla)
        for (int i = 0; i < 8; i++) {
            if (i < processedData.length) {
                values[i] = (double) (processedData[i] & 0xFF);
            } else {
                values[i] = 0;
            }
        }

        // 2. ENTROPIJA (Index 8)
        values[8] = izracunajEntropijuBajtova(processedData);

        // 3. VELIČINA (Index 9)
        values[9] = (double) processedData.length;

        Instance inst = new DenseInstance(1.0, values);
        inst.setDataset(finalDataset);

        if (label != null) {
            inst.setValue(finalDataset.numAttributes() - 1, label);
        }

        return inst;
    }

    /**
     * Setup za kriptografsku analizu - 26 frekv. + dodatni atributi
     */
    public static void setupEnigmaAttributes(int n, List<String> classNames) {
        ArrayList<Attribute> attributes = new ArrayList<>();

        // n-grami
        for (int i = 0; i < n; i++) {
            attributes.add(new Attribute("ngram_" + i));
        }

        // Statistički atributi
        attributes.add(new Attribute("dist_frekvencija"));
        attributes.add(new Attribute("kompresija"));
        attributes.add(new Attribute("kolmogorov"));
        attributes.add(new Attribute("entropija"));
        attributes.add(new Attribute("divergencija"));

        // NOVI atributi za bolje razlikovanje
        attributes.add(new Attribute("std_deviation")); // Standardna devijacija frekvencija
        attributes.add(new Attribute("max_freq"));      // Maksimalna frekvencija slova
        attributes.add(new Attribute("ic_index"));      // Index of Coincidence

        // KLASA
        attributes.add(new Attribute("klasa", classNames));

        finalDataset = new Instances("EnigmaDataset", attributes, 0);
        finalDataset.setClassIndex(finalDataset.numAttributes() - 1);
    }

    /**
     * Ekstrakcija features za šifre (Enigma, Vigenere, M209...)
     */
    public static Instance extractCipherFeatures(byte[] data, String label, int n) {
        String text = new String(data, StandardCharsets.ISO_8859_1);
        double[] values = new double[finalDataset.numAttributes()];

        // 1. n-grami (verovatnoće četirigrama)
        List<Double> ngrams = izracunajVerovatnoceCetirigrama(text, n);
        for (int i = 0; i < n; i++) {
            values[i] = ngrams.get(i);
        }

        // 2. Napredni statistički atributi
        int offset = n;
        values[offset++] = prosecnaUdaljenostNajfrekventnijegKaraktera(text);
        values[offset++] = stepenKompresijeBestBlock(data);
        values[offset++] = kolmogorovskaSpojeni(text, 256);
        values[offset++] = izracunajEntropijuBajtova(data);
        values[offset++] = izracunajEntropijuDivergencije(text);

        Instance inst = new DenseInstance(1.0, values);
        inst.setDataset(finalDataset);

        if (label != null) {
            inst.setValue(finalDataset.numAttributes() - 1, label);
        }

        return inst;
    }

    // ===== POMOĆNE FUNKCIJE =====

    public static double izracunajEntropijuBajtova(byte[] data) {
        if (data.length == 0) return 0;
        Map<Byte, Integer> counts = new HashMap<>();
        for (byte b : data) {
            counts.put(b, counts.getOrDefault(b, 0) + 1);
        }
        double entropy = 0;
        for (int count : counts.values()) {
            double p = (double) count / data.length;
            if (p > 0) {
                entropy -= p * (Math.log(p) / Math.log(2));
            }
        }
        return entropy;
    }

    public static double izracunajEntropijuBloka(String block) {
        return izracunajEntropijuBajtova(block.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static double izracunajEntropijuDivergencije(String block) {
        return izracunajEntropijuBloka(block) / 8.0;
    }

    public static double stepenKompresijeBestBlock(byte[] block) {
        if (block.length == 0) return 1.0;

        Deflater def = new Deflater(Deflater.BEST_COMPRESSION);
        def.setInput(block);
        def.finish();

        byte[] buffer = new byte[block.length + 100];
        int size = def.deflate(buffer);
        def.end();

        return (double) size / block.length;
    }

    public static double stepenKompresijeBest(String tekst) {
        return stepenKompresijeBestBlock(tekst.getBytes(StandardCharsets.ISO_8859_1));
    }

    public static double kolmogorovskaSpojeni(String tekst, int blockSize) {
        byte[] bytes = tekst.getBytes(StandardCharsets.ISO_8859_1);
        if (bytes.length == 0) return 0;

        double total = 0;
        int count = 0;

        for (int i = 0; i < bytes.length; i += blockSize) {
            int len = Math.min(blockSize, bytes.length - i);
            total += stepenKompresijeBestBlock(Arrays.copyOfRange(bytes, i, i + len));
            count++;
        }

        return count > 0 ? total / count : 0;
    }

    public static List<Double> izracunajVerovatnoceCetirigrama(String tekst, int n) {
        String clean = tekst.toUpperCase().replaceAll("[^A-Z]", "");
        if (clean.length() < 4) {
            return new ArrayList<>(Collections.nCopies(n, 0.0));
        }

        Map<String, Integer> counts = new HashMap<>();
        for (int i = 0; i < clean.length() - 3; i++) {
            String gram = clean.substring(i, i + 4);
            counts.put(gram, counts.getOrDefault(gram, 0) + 1);
        }

        List<Double> probs = new ArrayList<>();
        for (int c : counts.values()) {
            probs.add((double) c / (clean.length() - 3));
        }

        probs.sort(Collections.reverseOrder());

        while (probs.size() < n) {
            probs.add(0.0);
        }

        return probs.subList(0, n);
    }

    public static double prosecnaUdaljenostNajfrekventnijegKaraktera(String tekst) {
        if (tekst.isEmpty()) return 0;

        Map<Character, Integer> freq = new HashMap<>();
        for (char c : tekst.toCharArray()) {
            freq.put(c, freq.getOrDefault(c, 0) + 1);
        }

        char top = ' ';
        int max = -1;
        for (var e : freq.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                top = e.getKey();
            }
        }

        List<Integer> pos = new ArrayList<>();
        for (int i = 0; i < tekst.length(); i++) {
            if (tekst.charAt(i) == top) {
                pos.add(i);
            }
        }

        if (pos.size() < 2) return 0;

        double sum = 0;
        for (int i = 1; i < pos.size(); i++) {
            sum += (pos.get(i) - pos.get(i - 1));
        }

        return sum / (pos.size() - 1);
    }

    public static void saveArff(Instances dataset, String path) {
        try {
            ArffSaver saver = new ArffSaver();
            saver.setInstances(dataset);
            File f = new File(path);
            f.getParentFile().mkdirs();
            saver.setFile(f);
            saver.writeBatch();
            System.out.println("[FileMaker] ✓ ARFF sačuvan: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}