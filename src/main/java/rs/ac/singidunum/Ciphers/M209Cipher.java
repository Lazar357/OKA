package rs.ac.singidunum.Ciphers;

public class M209Cipher {

    // 1. Konfiguracija mašine (fiksni parametri)
    private final int[] WHEEL_LENGTHS = {26, 25, 23, 21, 19, 17};
    private final int NUM_WHEELS = 6;

    // Simbolički prikaz A=0, B=1, ... Z=25

    // 2. Stanje mašine (Ključevi: Pin Settings i Wheel Positions)
    private int[] wheelPositions; // Trenutne pozicije točkova (kratkoročni ključ)

    // Pin Settings (Postavke pinova): Primer fiksnog ključa (dugoročni ključ).
    // true = aktivan pin (doprinosi pomaku), false = neaktivan pin.
    // Matrica: [točak][pozicija]
    private final boolean[][] PIN_SETTINGS = {
            //   1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6
            {true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false}, // Točak 1 (26)
            {true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true},    // Točak 2 (25)
            {true, true, true, false, true, true, true, false, true, true, true, false, true, true, true, false, true, true, true, false, true, true, true}, // Točak 3 (23)
            {true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false},       // Točak 4 (21)
            {true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true, false, true, true},                   // Točak 5 (19)
            {true, true, true, false, true, true, true, false, true, true, true, false, true, true, true, false, true},                                // Točak 6 (17)
    };

    // Lug Settings (Postavke izbočina bubnja): Fiksira se koji točak uvek doprinosi.
    // U stvarnosti, ovo se menja. Ovde se uzima da su svi aktivni (svaki točak ima 2 lug-a).
    private final boolean[] LUG_ENABLE = {true, true, true, true, true, true};


    /**
     * Konstruktor - Inicijalizuje mašinu sa početnim pozicijama točkova.
     *
     * @param initialPositions Array od 6 celih brojeva (0-25) koji predstavljaju početne pozicije (A=0).
     */
    public M209Cipher(int[] initialPositions) {
        if (initialPositions.length != NUM_WHEELS) {
            throw new IllegalArgumentException("M-209 mora imati 6 početnih pozicija.");
        }
        this.wheelPositions = initialPositions.clone();
    }

    // --- POMOĆNE METODE ---

    /**
     * Pomera sve točkove za jednu poziciju.
     */
    private void advanceWheels() {
        for (int i = 0; i < NUM_WHEELS; i++) {
            // Pomeranje pozicije mod dužina točka
            wheelPositions[i] = (wheelPositions[i] + 1) % WHEEL_LENGTHS[i];
        }
    }

    /**
     * Izračunava ukupni šifrovni pomak (shift) za trenutno stanje mašine.
     * Pomak je zbir doprinosa aktivnih pinova.
     *
     * @return Ukupni pomak (0-25).
     */
    private int getCipheringShift() {
        int totalShift = 0;

        for (int i = 0; i < NUM_WHEELS; i++) {
            // Provera da li je točak omogućen od strane bubnja (lugovi)
            if (!LUG_ENABLE[i]) {
                continue;
            }

            // Trenutna pozicija na točku 'i'
            int position = wheelPositions[i];

            // Provera da li je pin na toj poziciji aktivan (doprinosi)
            if (PIN_SETTINGS[i][position]) {
                totalShift++; // Dodajemo 1 pomak
            }
        }

        // Pomak se vrši modulo 26 (pošto je M-209 bazirana na abecedi 26 slova)
        return totalShift % 26;
    }

    // --- GLAVNE METODE ZA ŠIFROVANJE/DEŠIFROVANJE ---

    /**
     * Šifruje ili dešifruje tekst pomoću M-209 algoritma.
     * M-209 je samo-recipročna, pa se ista funkcija koristi za oba.
     *
     * @param text      Otvoreni ili šifrovani tekst (samo velika slova A-Z).
     * @param isEncrypt Da li se radi o šifrovanju (true) ili dešifrovanju (false).
     * @return Rezultujući tekst.
     */
    private String processText(String text, boolean isEncrypt) {
        // Normalizacija teksta: uklanjanje razmaka i prebacivanje u velika slova
        text = text.toUpperCase().replaceAll("[^A-Z]", "");
        StringBuilder result = new StringBuilder();

        // Opcionalno: Možete pogledati dijagram M-209 da vidite kako izgleda mehanički proces izračunavanja pomaka.
        //

        for (char character : text.toCharArray()) {
            if (character >= 'A' && character <= 'Z') {
                // 1. Određivanje numeričke vrednosti (A=0, B=1, ...)
                int charValue = character - 'A';

                // 2. Izračunavanje šifrovnog pomaka
                int shift = getCipheringShift();

                int newCharValue;

                if (isEncrypt) {
                    // Šifrovanje: C = (P + S) mod 26
                    newCharValue = (charValue + shift) % 26;
                } else {
                    // Dešifrovanje: P = (C - S) mod 26. Dodajemo 26 da bismo izbegli negativne brojeve pre modula.
                    newCharValue = (charValue - shift + 26) % 26;
                }

                // 3. Konverzija nazad u karakter i dodavanje rezultatu
                result.append((char) (newCharValue + 'A'));

                // 4. Pomeranje točkova za sledeće slovo
                advanceWheels();
            }
            // Svi ostali znaci su ignorisani (zbog normalizacije na početku)
        }

        return result.toString();
    }

    // --- JAVNE METODE ---

    public String encrypt(String plaintext) {
        // Ključna stavka: Resetovanje pozicija pre početka šifrovanja (ako je potrebno)
        // Ovde koristimo trenutne pozicije, ali u praksi bi se koristile inicijalne pozicije za svaku poruku.

        return processText(plaintext, true);
    }

    public String decrypt(String ciphertext) {
        // BITNO: Mašina za dešifrovanje mora imati POTPUNO ISTO STANJE (pozicije točkova)
        // na početku kao što je imala mašina za šifrovanje. Zbog toga se `processText`
        // poziva na ISTIM pozicijama.

        return processText(ciphertext, false);
    }

    // --- MAIN METODA ZA TESTIRANJE ---

    public static void main(String[] args) {
        // Početne pozicije: Npr. 'A' za sve točkove (0, 0, 0, 0, 0, 0)
        int[] initialPosition = {0, 0, 0, 0, 0, 0};

        // 1. Kreiranje mašine
        M209Cipher m209CipherEncrypter = new M209Cipher(initialPosition);

        String originalText = "OVO JE TEST PORUKA ZA M DVESTA DEVET";

        System.out.println("Originalni tekst: " + originalText);

        // 2. Šifrovanje
        String ciphertext = m209CipherEncrypter.encrypt(originalText);
        System.out.println("Sifrovani tekst: " + ciphertext);

        // 3. Dešifrovanje
        // Moramo kreirati NOVU instancu mašine ili resetovati staru
        // na ORIGINALNE POZICIJE da bi dešifrovanje bilo uspešno.
        M209Cipher m209CipherDecrypter = new M209Cipher(initialPosition);

        String decryptedText = m209CipherDecrypter.decrypt(ciphertext);
        System.out.println("Desifrovani tekst: " + decryptedText);

        // Provera
        System.out.println("Da li se podudaraju? " + originalText.replaceAll("[^A-Z]", "").equals(decryptedText));
    }
}
