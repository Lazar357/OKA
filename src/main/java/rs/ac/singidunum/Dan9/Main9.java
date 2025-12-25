package rs.ac.singidunum.Dan9;

import rs.ac.singidunum.Dan7.Entropy;
import rs.ac.singidunum.Dan7.MutualInformation;
import rs.ac.singidunum.Dan8.TestRandom;
import rs.ac.singidunum.Weka.FileMaker;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class Main9 {

    static void main(String[] args) throws Exception {

//        double[] engCipher = TestRandom.byte2double(Base32.encode(ucitajENG()));
//        double[] dutCipher = TestRandom.byte2double(Base32.encode(ucitajDUT()));
//        double[] grcCipher = TestRandom.byte2double(Base32.encode(ucitajGRC()));
//        double[] spaCipher = TestRandom.byte2double(Base32.encode(ucitajSPA()));
//
//        double IEngDut = MutualInformation.calculateMutualInformation(engCipher,dutCipher);
//        double IEngGrc = MutualInformation.calculateMutualInformation(engCipher,grcCipher);
//        double IEngSpa = MutualInformation.calculateMutualInformation(engCipher,spaCipher);
//
//        double hENG = Entropy.calculateEntropy(engCipher);
//        double hDUT = Entropy.calculateEntropy(dutCipher);
//        double hGRC = Entropy.calculateEntropy(grcCipher);
//        double hSPA = Entropy.calculateEntropy(spaCipher);
//
//        System.out.println("H (eng): " + hENG);
//        System.out.println("H (dut): " + hDUT);
//        System.out.println("H (grc): " + hGRC);
//        System.out.println("H (spa): " + hSPA);
//
//        System.out.println("I (eng,dut): " + IEngDut);
//        System.out.println("I (eng,grc): " + IEngGrc);
//        System.out.println("I (eng,spa): " + IEngSpa);

//        FileMaker.makeFeature(10);
//        FileMaker.makeBinaryFeatures();


    }

    public static byte[] ucitajENG() {
        byte[] d = null;
        try {
            d = Files.readAllBytes(Path.of("eng_cipher.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOfRange(d, 0, Math.min(d.length, 10000));
    }

    public static byte[] ucitajDUT() {
        byte[] d = null;
        try {
            d = Files.readAllBytes(Path.of("dut_cipher.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOfRange(d, 0, Math.min(d.length, 10000));
    }

    public static byte[] ucitajGRC() {
        byte[] d = null;
        try {
            d = Files.readAllBytes(Path.of("grc_cipher.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOfRange(d, 0, Math.min(d.length, 10000));
    }

    public static byte[] ucitajSPA() {
        byte[] d = null;
        try {
            d = Files.readAllBytes(Path.of("spa_cipher.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOfRange(d, 0, Math.min(d.length, 10000));
    }
}
