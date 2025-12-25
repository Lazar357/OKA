package rs.ac.singidunum.Dan9;

public class Base32 {

    public static byte[] encode(byte[] input) {
        // Tvoj alfabet definisan kao niz bajtova (ASCII vrednosti)
        byte[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefg".getBytes();

        // Računanje dužine izlaznog niza: svaki bajt je 8 bitova, delimo sa 5.
        // (8 * len / 5) zaokruženo naviše.
        int outputLength = (input.length * 8 + 4) / 5;
        byte[] output = new byte[outputLength];

        int bitBuffer = 0;
        int bitCount = 0;
        int outPtr = 0;

        for (byte b : input) {
            // Ubacujemo 8 bita u bafer
            bitBuffer = (bitBuffer << 8) | (b & 0xFF);
            bitCount += 8;

            // Dokle god imamo bar 5 bita, vadimo ih
            while (bitCount >= 5) {
                int index = (bitBuffer >> (bitCount - 5)) & 0x1F;
                output[outPtr++] = ALPHABET[index];
                bitCount -= 5;
            }
        }

        // Obrada ostatka bitova na kraju
        if (bitCount > 0) {
            int index = (bitBuffer << (5 - bitCount)) & 0x1F;
            output[outPtr++] = ALPHABET[index];
        }

        return output;
    }
}
