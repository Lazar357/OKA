package rs.ac.singidunum.ispit;

import rs.ac.singidunum.Ciphers.M209Cipher;
import rs.ac.singidunum.Ciphers.VigenereCipher;
import rs.ac.singidunum.Ciphers.enigma.EnigmaCipher;
import rs.ac.singidunum.Dan12.MasterMain;
import rs.ac.singidunum.Weka.FileMaker;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static rs.ac.singidunum.Weka.FileMaker.finalDataset;

public class GUI extends JFrame {
    private JTextField txtK1, txtK2, txtDatasetName;
    private JCheckBox chkEncrypt;
    private JComboBox<String> comboMode, comboCipherType;
    private JTextArea logTab1, logTab2, txtPredictInput;
    private JProgressBar progressBar;
    private File selectedRoot, saveLocation;
    private JComboBox<Integer> comboTrees;
    private JLabel lblPredictionResult;
    private weka.classifiers.Classifier poslednjiModel;

    private String currentTrainingMode = "";

    public GUI() {
        setTitle("Ispitni zadatak");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        inicijalizujKomponente();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("1. Dataset Creator", createTab1());
        tabs.addTab("2. Evaluation", createTab2());
        tabs.addTab("3. Prediction", createTab3());
        add(tabs);
    }

    private void inicijalizujKomponente() {
        txtK1 = new JTextField("ABCDEFGHIJ");
        txtK2 = new JTextField("");
        txtDatasetName = new JTextField("Nije izabrano...");
        txtDatasetName.setEditable(false);
        chkEncrypt = new JCheckBox("Aktiviraj šifrovanje", true);

        comboMode = new JComboBox<>(new String[]{
                "Automatski mod (Folder = Klasa)",
                "Cipher Detection Mode",
                "Extension Detection Mode (ZIP, PNG, JPG, EXE, DLL, PDF)"
        });

        comboCipherType = new JComboBox<>(new String[]{"Enigma", "Vigenere", "M209", "Plaintext"});

        logTab1 = new JTextArea();
        logTab2 = new JTextArea();
        txtPredictInput = new JTextArea();

        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);

        comboTrees = new JComboBox<>(new Integer[]{10, 50, 100, 300});
        comboTrees.setSelectedItem(100);

        lblPredictionResult = new JLabel("Rezultat: Na čekanju...", SwingConstants.CENTER);
    }

    private JPanel createTab1() {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

        leftPanel.add(createFormRow("Mod rada:", comboMode));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(createFormRow("Tip šifre:", comboCipherType));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(createFormRow("Ključ:", txtK1));
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkPanel.add(chkEncrypt);
        leftPanel.add(checkPanel);

        JButton btnSelectInput = new JButton("1. Izaberi Izvorne Tekstove");
        JButton btnSelectOutput = new JButton("2. Odredi .arff destinaciju");

        btnSelectInput.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedRoot = jfc.getSelectedFile();
                logTab1.append("Ulaz postavljen: " + selectedRoot.getName() + "\n");
            }
        });

        btnSelectOutput.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                saveLocation = jfc.getSelectedFile();
                if (!saveLocation.getName().endsWith(".arff")) {
                    saveLocation = new File(saveLocation.getAbsolutePath() + ".arff");
                }
                txtDatasetName.setText(saveLocation.getAbsolutePath());
            }
        });

        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        btnPanel.add(btnSelectInput);
        btnPanel.add(btnSelectOutput);
        leftPanel.add(btnPanel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(new JLabel("Putanja:"));
        leftPanel.add(txtDatasetName);

        logTab1.setBackground(Color.BLACK);
        logTab1.setForeground(Color.GREEN);
        logTab1.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scroll = new JScrollPane(logTab1);
        scroll.setBorder(BorderFactory.createTitledBorder("Konzola"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, scroll);
        split.setDividerLocation(350);

        JButton btnStart = new JButton("POKRENI EKSTRAKCIJU I GENERISANJE DATASETA");
        btnStart.setPreferredSize(new Dimension(0, 50));
        btnStart.addActionListener(e -> new Thread(this::pokreniProcesGenerisanja).start());

        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.add(progressBar, BorderLayout.NORTH);
        bottom.add(btnStart, BorderLayout.SOUTH);

        mainPanel.add(split, BorderLayout.CENTER);
        mainPanel.add(bottom, BorderLayout.SOUTH);
        return mainPanel;
    }

    private JPanel createFormRow(String labelText, JComponent component) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.add(new JLabel(labelText), BorderLayout.WEST);
        row.add(component, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(1000, 30));
        return row;
    }

    private void pokreniProcesGenerisanja() {
        try {
            if (selectedRoot == null || saveLocation == null) {
                JOptionPane.showMessageDialog(this, "Izaberite ulaz i izlaz!");
                return;
            }

            progressBar.setIndeterminate(true);
            String mod = (String) comboMode.getSelectedItem();
            currentTrainingMode = mod;
            List<String> imenaKlasa = new ArrayList<>();

            // KLJUČNA RAZLIKA: Za Cipher Detection mod, klase su ALGORITMI, ne folderi!
            if (mod.contains("Cipher Detection Mode")) {
                // Klase su algoritmi šifrovanja
                imenaKlasa = Arrays.asList("Enigma", "Vigenere", "M209", "Plaintext");
                FileMaker.setupEnigmaAttributes(10, imenaKlasa);
                logTab1.append(">>> MOD: Detekcija šifri (Enigma, Vigenere, M209, Plaintext)\n");
            } else {
                // Klase su imena foldera
                File[] targetFolders = selectedRoot.listFiles(File::isDirectory);
                if (targetFolders != null) {
                    for (File f : targetFolders) imenaKlasa.add(f.getName());
                }

                if (mod.contains("Extension Detection Mode")) {
                    FileMaker.setupExtensionAttributes(imenaKlasa);
                    logTab1.append(">>> MOD: Detekcija ekstenzija (Magic Numbers)\n");
                } else {
                    FileMaker.setupEnigmaAttributes(10, imenaKlasa);
                    logTab1.append(">>> MOD: Automatski (Folder = Klasa)\n");
                }
            }

            // Alati za šifrovanje
            String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String[] rotors = new String[5];
            for (int i = 0; i < 5; i++) rotors[i] = MasterMain.shuffleString(ALPHABET);
            EnigmaCipher enigma = new EnigmaCipher(ALPHABET, "bq cr hl to", MasterMain.reflectorGenerator(ALPHABET), rotors, "ABCDE");
            M209Cipher m209 = new M209Cipher(new int[]{0, 0, 0, 0, 0, 0});

            // PROCESIRANJE PODATAKA
            File[] targetFolders = selectedRoot.listFiles(File::isDirectory);
            if (targetFolders == null || targetFolders.length == 0) {
                logTab1.append("GREŠKA: Nema foldera u izabranom direktorijumu!\n");
                return;
            }

            for (File folder : targetFolders) {
                List<File> files = new ArrayList<>();
                collectFiles(folder, files);
                logTab1.append("Folder: " + folder.getName() + " → " + files.size() + " fajlova.\n");

                for (File f : files) {
                    try {
                        if (mod.contains("Extension Detection Mode")) {
                            // MOD 1: Detekcija ekstenzija po Magic Numbers
                            finalDataset.add(FileMaker.extractExtensionFeatures(f, folder.getName()));

                        } else if (mod.contains("Cipher Detection Mode")) {
                            // MOD 2: Detekcija algoritama šifrovanja
                            byte[] bytes = Files.readAllBytes(f.toPath());
                            String content = new String(bytes, StandardCharsets.ISO_8859_1);
                            String clean = content.toUpperCase().replaceAll("[^A-Z]", "");

                            if (clean.length() < 500) { // POVEĆANO sa 100 na 500
                                logTab1.append("  ⚠ Preskoči " + f.getName() + " (premalo teksta: " + clean.length() + " slova)\n");
                                continue;
                            }

                            // Generiši 4 instance: jedan tekst -> sve 4 šifre
                            finalDataset.add(FileMaker.extractCipherFeatures(
                                    enigma.encrypt(clean).getBytes(), "Enigma", 10));
                            finalDataset.add(FileMaker.extractCipherFeatures(
                                    VigenereCipher.encrypt(clean, "KEY").getBytes(), "Vigenere", 10));
                            finalDataset.add(FileMaker.extractCipherFeatures(
                                    m209.encrypt(clean).getBytes(), "M209", 10));
                            finalDataset.add(FileMaker.extractCipherFeatures(
                                    clean.getBytes(), "Plaintext", 10));

                        } else {
                            // MOD 3: Automatski (Folder = Klasa)
                            byte[] bytes = Files.readAllBytes(f.toPath());
                            String content = new String(bytes, StandardCharsets.ISO_8859_1);
                            String clean = content.toUpperCase().replaceAll("[^A-Z]", "");

                            if (clean.length() < 100) continue;

                            String res = applySelectedCipher(clean, (String) comboCipherType.getSelectedItem(), m209, "KEY", enigma);
                            finalDataset.add(FileMaker.extractCipherFeatures(res.getBytes(), folder.getName(), 10));
                        }
                    } catch (Exception ex) {
                        logTab1.append("  ✗ Greška u fajlu " + f.getName() + ": " + ex.getMessage() + "\n");
                    }
                }
            }

            if (finalDataset.isEmpty()) {
                logTab1.append("GREŠKA: Dataset je prazan! Proveri fajlove.\n");
                return;
            }

            FileMaker.saveArff(finalDataset, saveLocation.getAbsolutePath());
            progressBar.setIndeterminate(false);
            progressBar.setValue(100);
            logTab1.append("✓ ZAVRŠENO: " + saveLocation.getName() + " (" + finalDataset.size() + " instanci)\n");

        } catch (Exception ex) {
            logTab1.append("GRESKA: " + ex.getMessage() + "\n");
            ex.printStackTrace();
            progressBar.setIndeterminate(false);
        }
    }

    private String applySelectedCipher(String in, String type, M209Cipher m, String k, EnigmaCipher e) {
        if (!chkEncrypt.isSelected()) return in;
        switch (type) {
            case "Vigenere":
                return VigenereCipher.encrypt(in, k);
            case "M209":
                return m.encrypt(in);
            case "Enigma":
                return e.encrypt(in);
            default:
                return in;
        }
    }

    private JPanel createTab2() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnLoad = new JButton("Učitaj ARFF i Treniraj Model");
        top.add(new JLabel("Broj stabala:"));
        top.add(comboTrees);
        top.add(btnLoad);

        logTab2.setBackground(new Color(40, 44, 52));
        logTab2.setForeground(Color.WHITE);

        btnLoad.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                new Thread(() -> runML(jfc.getSelectedFile(), (int) comboTrees.getSelectedItem())).start();
            }
        });

        panel.add(top, BorderLayout.NORTH);
        panel.add(new JScrollPane(logTab2), BorderLayout.CENTER);
        return panel;
    }

    private void runML(File arff, int trees) {
        try {
            logTab2.setText("Učitavanje podataka...\n");
            DataSource source = new DataSource(arff.getAbsolutePath());
            Instances data = source.getDataSet();
            data.setClassIndex(data.numAttributes() - 1);

            RandomForest rf = new RandomForest();
            rf.setNumIterations(trees);

            logTab2.append("Pokretanje unakrsne validacije (10-fold)...\n");
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(rf, data, 10, new Random(1));

            rf.buildClassifier(data);
            this.poslednjiModel = rf;

            logTab2.append("\n=== REZULTATI TRENINGA ===\n");
            logTab2.append(eval.toSummaryString());
            logTab2.append("\n");
            logTab2.append(eval.toMatrixString("Konfuziona Matrica"));
            logTab2.append("\n>>> MODEL JE SPREMAN ZA PREDIKCIJU.");
        } catch (Exception ex) {
            logTab2.append("Greška: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private JPanel createTab3() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton btnLoadArff = new JButton("Učitaj .arff za učenje");
        JButton btnBuildModel = new JButton("NAUČI MODELE (Train)");

        btnBuildModel.setBackground(new Color(34, 139, 34));
        btnBuildModel.setForeground(Color.WHITE);

        topPanel.add(btnLoadArff);
        topPanel.add(btnBuildModel);

        JPanel midPanel = new JPanel(new BorderLayout(10, 10));
        txtPredictInput = new JTextArea();
        txtPredictInput.setEditable(false);
        txtPredictInput.setBackground(new Color(30, 30, 30));
        txtPredictInput.setForeground(Color.CYAN);
        midPanel.add(new JScrollPane(txtPredictInput), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        JButton btnPredictFile = new JButton("IZABERI FAJL ZA PREDVIĐANJE");
        btnPredictFile.setFont(new Font("Arial", Font.BOLD, 14));

        lblPredictionResult = new JLabel("STATUS: Čekam trening...", SwingConstants.CENTER);
        lblPredictionResult.setFont(new Font("Consolas", Font.BOLD, 20));

        bottomPanel.add(btnPredictFile, BorderLayout.NORTH);
        bottomPanel.add(lblPredictionResult, BorderLayout.SOUTH);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(midPanel, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        btnLoadArff.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    DataSource source = new DataSource(jfc.getSelectedFile().getAbsolutePath());
                    FileMaker.finalDataset = source.getDataSet();
                    FileMaker.finalDataset.setClassIndex(FileMaker.finalDataset.numAttributes() - 1);

                    int numAttrs = FileMaker.finalDataset.numAttributes() - 1;
                    if (numAttrs == 10) {
                        currentTrainingMode = "Extension Detection Mode";
                        txtPredictInput.append("✓ Detektovan Extension Dataset (10 atributa)\n");
                    } else {
                        currentTrainingMode = "Cipher Detection Mode";
                        txtPredictInput.append("✓ Detektovan Cipher Dataset (" + numAttrs + " atributa)\n");
                    }

                    txtPredictInput.append("Dataset učitan: " + jfc.getSelectedFile().getName() + "\n");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Greška pri učitavanju: " + ex.getMessage());
                }
            }
        });

        btnBuildModel.addActionListener(e -> {
            try {
                if (FileMaker.finalDataset == null) {
                    JOptionPane.showMessageDialog(this, "Prvo učitajte .arff fajl!");
                    return;
                }
                poslednjiModel = new RandomForest();
                ((RandomForest) poslednjiModel).setNumIterations(100);
                poslednjiModel.buildClassifier(FileMaker.finalDataset);
                lblPredictionResult.setText("STATUS: Model spreman!");
                lblPredictionResult.setForeground(new Color(0, 200, 0));
                txtPredictInput.append(">>> Model je uspešno istreniran.\n");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Greška u treningu: " + ex.getMessage());
            }
        });

        btnPredictFile.addActionListener(e -> {
            JFileChooser jfc = new JFileChooser();
            if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                izvrsiPredikcijuFajla(jfc.getSelectedFile());
            }
        });

        return panel;
    }

    private void izvrsiPredikcijuFajla(File f) {
        try {
            if (poslednjiModel == null) {
                JOptionPane.showMessageDialog(this, "Model nije istreniran! Kliknite na 'Train' prvo.");
                return;
            }

            Instance inst;

            if (currentTrainingMode.contains("Extension") || FileMaker.finalDataset.numAttributes() == 11) {
                inst = FileMaker.extractExtensionFeatures(f, null);
                txtPredictInput.append("Mod: Extension Detection\n");
            } else {
                byte[] bytes = Files.readAllBytes(f.toPath());
                inst = FileMaker.extractCipherFeatures(bytes, null, 10);
                txtPredictInput.append("Mod: Cipher Detection\n");
            }

            double resultIdx = poslednjiModel.classifyInstance(inst);
            double[] distribution = poslednjiModel.distributionForInstance(inst);

            String label = FileMaker.finalDataset.classAttribute().value((int) resultIdx);
            double confidence = distribution[(int) resultIdx] * 100;

            String finalOutput = String.format("MODEL PREDVIDJA: %s (Pouzdanost: %.2f%%)", label.toUpperCase(), confidence);
            lblPredictionResult.setText(finalOutput);

            if (confidence > 80) {
                lblPredictionResult.setForeground(new Color(0, 200, 0));
            } else if (confidence > 50) {
                lblPredictionResult.setForeground(new Color(255, 165, 0));
            } else {
                lblPredictionResult.setForeground(Color.RED);
            }

            txtPredictInput.append("Fajl: " + f.getName() + " -> " + finalOutput + "\n");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Analiza nije uspela: " + ex.getMessage());
        }
    }

    private void collectFiles(File dir, List<File> list) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    collectFiles(f, list);
                } else if (!f.isHidden() && f.length() > 0) {
                    list.add(f);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GUI().setVisible(true));
    }
}