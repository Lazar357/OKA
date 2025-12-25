package rs.ac.singidunum.Dan8;

import rs.ac.singidunum.Dan7.Entropy;
import rs.ac.singidunum.Dan7.MutualInformation;
import rs.ac.singidunum.Dan9.Base32;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;

public class TestRandom {
    public static void main(String[] args){

        byte[] x = new byte[10000];
        Random r1 = new Random();
        r1.setSeed(346525);
        r1.nextBytes(x);
        double[] X = byte2double(Base64.getEncoder().encode(x));

        byte[] y = new byte[10000];
        Random r2 = new Random();
        r2.setSeed(87543);
        r2.nextBytes(y);
        double[] Y = byte2double(Base64.getEncoder().encode(y));

        byte[] z = ucitajFajl();
        double[] Z = byte2double(Base64.getEncoder().encode(z));

        double hX = Entropy.calculateEntropy(X);
        double hY = Entropy.calculateEntropy(Y);
        double hZ = Entropy.calculateEntropy(Z);

        System.out.println("Entropy (X): " + hX);
        System.out.println("Entropy (Y): " + hY);
        System.out.println("Entropy (Z): " + hZ);

        // Uslovna entropija

        double hXY = Entropy.calculateConditionalEntropy(X,Y);
        double hXZ = Entropy.calculateConditionalEntropy(X,Z);

        System.out.println("Conditional entropy (X,Y): " + hXY);
        System.out.println("Conditional entropy (X,Z): " + hXZ);

        // Zdruzena entropija
        double HXY = Entropy.calculateJointEntropy(X,Y);
        System.out.println("Joint entropy (X,Y): " + HXY);
        double HXZ = Entropy.calculateJointEntropy(X,Z);
        System.out.println("Joint entropy (X,Z): " + HXZ);

        double miXY = MutualInformation.calculateMutualInformation(X,Y);
        System.out.println("Mutual information between X and Y: " + miXY);
        double miXZ = MutualInformation.calculateMutualInformation(X,Z);
        System.out.println("Mutual information between X and Z: " + miXZ);

        double micXYZ = MutualInformation.calculateConditionalMutualInformation(X,Y,Z);
        System.out.println("Conditional mutual information between X and Y: " + micXYZ);

        double[] x32 = byte2double(Base32.encode(x));
        double[] y32 = byte2double(Base32.encode(y));
        double[] z32 = byte2double(Base32.encode(z));

        double hX32 = Entropy.calculateEntropy(x32);
        double hY32 = Entropy.calculateEntropy(y32);
        double hZ32 = Entropy.calculateEntropy(z32);
        System.out.println("Entropy of x in Base32: " + hX32);
        System.out.println("Entropy of y in Base32: " + hY32);
        System.out.println("Entropy of z in Base32: " + hZ32);

        // Uslovna entropija sa Base32

        double hXY32 = Entropy.calculateConditionalEntropy(x32,y32);
        double hXZ32 = Entropy.calculateConditionalEntropy(x32,z32);
        System.out.println("Conditional entropy (X,Y): " + hXY32);
        System.out.println("Conditional entropy (X,Z): " + hXZ32);

        // uzajamna informacija sa Base32

        double IXY32 = MutualInformation.calculateMutualInformation(x32,y32);
        double IXZ32 = MutualInformation.calculateMutualInformation(x32,z32);
        System.out.println("I base32 (X,Y): " + IXY32);
        System.out.println("I base32 (X,Z): " + IXZ32);

        byte[] shuffledX = shuffle(x);
        double[] shuffledX32 = byte2double(Base32.encode(shuffledX));
        byte[] shuffledY = shuffle(y);
        double[] shuffledY32 = byte2double(Base32.encode(shuffledY));
        double shuffledHX32 = Entropy.calculateEntropy(shuffledX32);
        double shuffledHY32 = Entropy.calculateEntropy(shuffledY32);
        double shuffledIXY32 = MutualInformation.calculateMutualInformation(shuffledX32,y32);
        System.out.println("Mutual information after shuffling: " + shuffledIXY32);
    }

    public static double[] byte2double(byte[] podaci) {
        double[] r = new double[podaci.length];
        for (int i = 0; i < r.length; i++) {
            r[i] = podaci[i];
        }
        return r;
    }

    public static byte[] ucitajFajl(){
        byte[] d = null;
        try {
            d = Files.readAllBytes(Path.of("2025-12-15.bin"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOfRange(d, 0,10000);
    }

    public static byte[] shuffle(byte[] array) {
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            byte temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return array;
    }
}
