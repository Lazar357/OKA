package rs.ac.singidunum.Dan7;

public class FolderEntropyStats {
    private double totalEntropy = 0.0;
    public int fileCount = 0;

    public synchronized void addEntropy(double entropy) {
        this.totalEntropy += entropy;
        this.fileCount++;
    }

    public double getAverageEntropy() {
        if (fileCount == 0) return 0.0;
        return totalEntropy / fileCount;
    }
}