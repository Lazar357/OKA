package rs.ac.singidunum.Dan8;

import rs.ac.singidunum.Dan7.Entropy;
import rs.ac.singidunum.Ciphers.VigenereCipher;
import rs.ac.singidunum.Dan7.MutualInformation;
import rs.ac.singidunum.Weka.FileMaker;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class TestLanguage {
    public static void main(String[] args) throws IOException {
        String eng = ucitajFajl("languages/ENG/1 (1).txt");
        byte[] engBytes = eng.getBytes();
        double[] ENG = byte2double(engBytes);
        double hENG = Entropy.calculateEntropy(ENG);
        System.out.println("English entropy: " + hENG);
        System.out.println("------------------");

        String dut = ucitajFajl("languages/DUT/DE097/known01.txt");
        byte[] dutBytes = dut.getBytes();
        double[] DUT = byte2double(dutBytes);
        double hDUT = Entropy.calculateEntropy(DUT);
        System.out.println("Dutch entropy: " + hDUT);
        System.out.println("------------------");

        String spa = ucitajFajl("languages/SPA/SP101/known01.txt");
        byte[] spaBytes = spa.getBytes();
        double[] SPA = byte2double(spaBytes);
        double hSPA = Entropy.calculateEntropy(SPA);
        System.out.println("Spain entropy: " + hSPA);
        System.out.println("------------------");

        String grc = ucitajFajl("languages/GRC/GR101/known01.txt");
        byte[] grcBytes = grc.getBytes();
        double[] GRC = byte2double(grcBytes);
        double hGRC = Entropy.calculateEntropy(GRC);
        System.out.println("Greece entropy: " + hGRC);
        System.out.println("------------------");

        int minLen = Math.min(
                ENG.length,
                Math.min(SPA.length, Math.min(DUT.length, GRC.length))
        );

        ENG = skrati(ENG, minLen);
        SPA = skrati(SPA, minLen);
        DUT = skrati(DUT, minLen);
        GRC = skrati(GRC, minLen);

        double hEngSpa = Entropy.calculateConditionalEntropy(ENG, SPA);
        double hEngGrc = Entropy.calculateConditionalEntropy(ENG, GRC);
        double hEngDut = Entropy.calculateConditionalEntropy(ENG, DUT);

        System.out.println("ConditionalEntropy (eng,spa): " + hEngSpa);
        System.out.println("ConditionalEntropy (eng,grc): " + hEngGrc);
        System.out.println("ConditionalEntropy (eng,dut): " + hEngDut);
        System.out.println("------------------");

//        String key = "SUPERTAJNIKLJUC";
//        List<TekstPar> parovi = ucitajFolderISifrujParovi("SUPERTAJNIKLJUC");
//
//        for (TekstPar par : parovi) {
//            double[] plain = byte2double(par.plaintext.getBytes());
//            double[] cipher = byte2double(par.ciphertext.getBytes());
//
//            double hCipher = Entropy.calculateEntropy(cipher);
//            double hCond = Entropy.calculateConditionalEntropy(cipher, plain);
//
//            double mutualInfo = hCipher - hCond;
//
//            System.out.println("Mutual information of file " + par.filename + ": " + mutualInfo);
//            System.out.println("Conditional Entropy of file " + par.filename + ": " + hCond);
//            System.out.println("Cipher entropy: " + hCipher);
//            System.out.println("----");
//        }

        double mutualInfoEngSpa = MutualInformation.calculateMutualInformation(ENG, SPA);
        System.out.println("Mutual information between ENG and SPA: " + mutualInfoEngSpa);
    }

    public static double[] skrati(double[] data, int len) {
        double[] r = new double[len];
        System.arraycopy(data, 0, r, 0, len);
        return r;
    }

    public static double[] byte2double(byte[] podaci) {
        double[] r = new double[podaci.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = podaci[i];
        }
        return r;
    }

    public static String ucitajFajl(String path) throws IOException {
        byte[] fileBytes = Files.readAllBytes(Path.of(path));

        int length = Math.min(1000, fileBytes.length);
        byte[] first1000 = new byte[length];
        System.arraycopy(fileBytes, 0, first1000, 0, length);

        // pretvori u String (UTF-8 je default, ali ovako je eksplicitno)
        String text = new String(first1000, java.nio.charset.StandardCharsets.UTF_8);

        // normalizacija kao ranije
        return text.toUpperCase().replaceAll("\\s+", "");
    }

    public static class TekstPar {
        public final String filename;
        public final String plaintext;
        public final String ciphertext;

        public TekstPar(String filename, String plaintext, String ciphertext) {
            this.filename = filename;
            this.plaintext = plaintext;
            this.ciphertext = ciphertext;
        }
    }

    public static List<TekstPar> ucitajFolderISifrujParovi(String key) throws IOException {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Izaberi folder sa plaintext fajlovima");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            System.exit(0);
        }

        File folder = chooser.getSelectedFile();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (files == null || files.length == 0) {
            System.out.println("Nema txt fajlova u folderu.");
            return new ArrayList<>();
        }

        List<TekstPar> parovi = new ArrayList<>();

        for (File f : files) {
            String plaintext = Files.readString(f.toPath());
            String ciphertext = VigenereCipher.encrypt(plaintext, key);

            String outName = f.getName();
            if (!outName.startsWith("enc_")) {
                outName = "enc_" + outName;
            }
            File out = new File(f.getParent(), outName);
            Files.writeString(out.toPath(), ciphertext);

            parovi.add(new TekstPar(f.getName(), plaintext, ciphertext));
            System.out.println("Šifrovan: " + f.getName());
        }

        return parovi;
    }
}



