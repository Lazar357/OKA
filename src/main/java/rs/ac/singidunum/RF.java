//package rs.ac.singidunum;
//
//import rs.ac.singidunum.Weka.FileMaker;
//import weka.classifiers.Evaluation;
//import weka.classifiers.trees.RandomForest;
//import weka.core.Instance;
//import weka.core.Instances;
//import weka.core.converters.ConverterUtils.DataSource;
//
//import javax.swing.*;
//import java.io.File;
//import java.util.Random;
//
//public class RF {
//
//    public static void main(String[] args) {
//        try {
//            // 1. Učitavanje dataseta
//            DataSource source = new DataSource("Kriptoanaliza/Dataset.arff");
//            Instances trainData = source.getDataSet();
//            if (trainData.classIndex() == -1)
//                trainData.setClassIndex(trainData.numAttributes() - 1);
//
//            // 2. Treniranje modela
//            RandomForest rf = new RandomForest();
//            rf.setNumIterations(300);
//            rf.setMaxDepth(0);
//            rf.setNumFeatures(0);
//            rf.buildClassifier(trainData);
//
//            Evaluation eval = new Evaluation(trainData);
//            eval.crossValidateModel(rf, trainData, 10, new Random(1));
//            System.out.println("Preciznost modela: " + eval.pctCorrect() + "%");
//
//            JFileChooser jfc = new JFileChooser();
//            if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
//                File selectedFile = jfc.getSelectedFile();
//
////                Instance unknown = FileMaker.ex(selectedFile, null, trainData);
//
//                if (unknown != null) {
//                    double result = rf.classifyInstance(unknown);
//                    String label = trainData.classAttribute().value((int) result);
//
//                    // Ispis verovatnoće (da vidiš koliko je RandomForest siguran u odluku)
//                    double[] confidence = rf.distributionForInstance(unknown);
//                    System.out.println("Sistem smatra da je ovo: " + label);
//                    System.out.println("Sigurnost: " + (confidence[(int)result] * 100) + "%");
//                    System.out.println(eval.toMatrixString("--- Matrica zabune ---"));
//                    System.out.println(eval.toSummaryString("\nRezultati:\n", false));
//                }
//            }
//        } catch (Exception e) {
//            System.err.println("Error: " + e.getMessage());
//        }
//    }
//}
