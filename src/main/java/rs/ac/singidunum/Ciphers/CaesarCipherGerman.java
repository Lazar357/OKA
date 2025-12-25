package rs.ac.singidunum.Ciphers;

import java.util.Scanner;

public class CaesarCipherGerman {

    // German alphabet (lowercase + uppercase) including umlauts and ß
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyzäöüß";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÜ";

    // Full alphabet length for shift calculations
    private static final int LOWERCASE_LENGTH = LOWERCASE.length(); // 30
    private static final int UPPERCASE_LENGTH = UPPERCASE.length(); // 29 (no ß in uppercase)

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the text (German): ");
        String plaintext = scanner.nextLine();

        System.out.print("Enter shift value (1-30 recommended): ");
        int shift = scanner.nextInt();

        String encrypted = encrypt(plaintext, shift);
        String decrypted = decrypt(encrypted, shift);

        System.out.println("\nOriginal:  " + plaintext);
        System.out.println("Encrypted: " + encrypted);
        System.out.println("Decrypted: " + decrypted);

        scanner.close();
    }

    // Encrypt the text with Caesar cipher
    public static String encrypt(String text, int shift) {
        return transform(text, shift);
    }

    // Decrypt the text with Caesar cipher
    public static String decrypt(String text, int shift) {
        return transform(text, LOWERCASE_LENGTH - (shift % LOWERCASE_LENGTH));
    }

    // Core transformation method (used by both encrypt and decrypt)
    private static String transform(String text, int shift) {
        StringBuilder result = new StringBuilder();

        for (char c : text.toCharArray()) {
            if (LOWERCASE.indexOf(c) != -1) {
                // Lowercase German letters
                int originalPos = LOWERCASE.indexOf(c);
                int newPos = (originalPos + shift) % LOWERCASE_LENGTH;
                result.append(LOWERCASE.charAt(newPos));
            }
            else if (UPPERCASE.indexOf(c) != -1) {
                // Uppercase German letters (ÄÖÜ)
                int originalPos = UPPERCASE.indexOf(c);
                int newPos = (originalPos + shift) % UPPERCASE_LENGTH;
                result.append(UPPERCASE.charAt(newPos));
            }
            else {
                // Non-letter characters (space, punctuation, numbers...) stay unchanged
                result.append(c);
            }
        }
        return result.toString();
    }
}
