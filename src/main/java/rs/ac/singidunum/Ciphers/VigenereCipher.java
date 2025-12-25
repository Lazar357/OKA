package rs.ac.singidunum.Ciphers;

public class VigenereCipher {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String encrypt(String text, String key) {
        text = text.toUpperCase();
        key = key.toUpperCase();

        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (ALPHABET.indexOf(c) != -1) {
                int textVal = c - 'A';
                int keyVal = key.charAt(keyIndex % key.length()) - 'A';

                char encrypted = (char) ((textVal + keyVal) % 26 + 'A');
                result.append(encrypted);
                keyIndex++;
            } else {
                result.append(c); // čuva razmake i simbole
            }
        }
        return result.toString();
    }

    public static String decrypt(String text, String key) {
        text = text.toUpperCase();
        key = key.toUpperCase();

        StringBuilder result = new StringBuilder();
        int keyIndex = 0;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (ALPHABET.indexOf(c) != -1) {
                int textVal = c - 'A';
                int keyVal = key.charAt(keyIndex % key.length()) - 'A';

                char decrypted = (char) ((textVal - keyVal + 26) % 26 + 'A');
                result.append(decrypted);
                keyIndex++;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static void main(String[] args) {
        String plaintext = "OVO JE TEST PORUKA";
        String key = "KLJUC";

        String encrypted = encrypt(plaintext, key);
        String decrypted = decrypt(encrypted, key);

        System.out.println("Plaintext : " + plaintext);
        System.out.println("Encrypted : " + encrypted);
        System.out.println("Decrypted : " + decrypted);
    }
}
