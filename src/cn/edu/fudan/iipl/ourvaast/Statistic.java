/**
 * @Copyright cn.edu.fudan.iipl
 */

package cn.edu.fudan.iipl.ourvaast;

import static cn.edu.fudan.iipl.util.FileUtil.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * <pre>Score each gene. Get the gene score matrix and calculate the statistic of rank sum&two parts test.
 * This modual will generate a gene score matrix files and statistic matrix files.</pre>
 *
 * @author Yong Chen
 * @since 2015-01-28
 */
public class Statistic {

    private String caseFolderPath = null;  // E:\BaiduYunDownload\shuffledCase\geneScore        recessive_model folder in this path, then the frequencies folder.
    private String controlFolderPath = null; //E:\BaiduYunDownload\control\geneScore            recessive_model folder in this path, but no frequencies folder.
    private String toBeShuffledFrequencyFilePath = null;
    private String inheritanceModel = null;
    private String caseOutputPath = null;
    private String controlOutputPath = null;

    public static void main(String[] args) {
        if (args.length < 12) {
            usage();
            return;
        }

        Statistic statistic = new Statistic();

        /** process input,  preserve args. */
        statistic.processInput(args);


        /** enter the inheritance model folder. */
        statistic.setCaseFolderPath(statistic.getCaseFolderPath() + File.separator + statistic.getInheritanceModel());
        dirJudge(statistic.getCaseFolderPath());
        statistic.setControlFolderPath(statistic.getControlFolderPath() + File.separator + statistic.getInheritanceModel());
        dirJudge(statistic.getControlFolderPath());


        /** create gene score matrix output folder path. */
        statistic.setCaseOutputPath(statistic.getCaseOutputPath() + File.separator + "geneScoreMatrix" + File.separator
                + statistic.getInheritanceModel());
        dirsCreate(statistic.getCaseOutputPath());
        statistic.setControlOutputPath(statistic.getControlOutputPath() + File.separator + "geneScoreMatrix" + File.separator
                + statistic.getInheritanceModel());
        dirsCreate(statistic.getControlOutputPath());


        /** read frequency list. */
        List<Double> frequencyList = new ArrayList<Double>();
        BufferedReader frequencyFileReader = null;
        try {
            frequencyFileReader = new BufferedReader(new FileReader(statistic.getToBeShuffledFrequencyFilePath()));
            String tempString = null;
            while ((tempString = frequencyFileReader.readLine()) != null) {
                String[] frequencies = tempString.trim().split("\\s+");
                for (String frequency : frequencies) {
                    frequencyList.add(Double.valueOf(frequency));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                frequencyFileReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        /** get gene score matrix for case and control. */
        for (int j = 0; j < frequencyList.size(); j++) {
            //case
            statistic.getGeneScoreMatrixFromGeneScoreFiles(statistic.getCaseFolderPath() + File.separator
                    + getPercentFormat(frequencyList.get(j), 2, 0), "case", statistic.getInheritanceModel(), statistic.getCaseOutputPath());
        }
        //control
        statistic.getGeneScoreMatrixFromGeneScoreFiles(statistic.getControlFolderPath(), "control",
                statistic.getInheritanceModel(), statistic.getControlOutputPath());


        /** get statistic matrix of rank sum&two parts test. */
        for (int j = 0; j < frequencyList.size(); j++) {
            String caseGeneScoreMatrixPath = getCanonicalPath(statistic.getCaseOutputPath() + File.separator
                    + getPercentFormat(frequencyList.get(j), 2, 0) + File.separator + "case_" + statistic.getInheritanceModel() + ".geneScoreMatrix");
            String controlGeneScoreMatrixPath = getCanonicalPath(statistic.getControlOutputPath() + File.separator
                    + "control_" + statistic.getInheritanceModel() + ".geneScoreMatrix");
            statistic.getStatisticMatrix(caseGeneScoreMatrixPath, statistic.getInheritanceModel(), controlGeneScoreMatrixPath);
        }

    }

    /**
     * <pre>convert double to percentage style, maximum bits of interger part is corresponding with parameter 'integerDigits',
     * maximum bits of decimal part is corresponding with parameter 'fractionDigits'.
     * <br/>for example,<br/>
     * getPercentFormat(0.02, 2, 0) will get the result "2%".
     * getPercentFormat(0.20, 2, 0) will get the result "20%".
     * </pre>
     *
     * @param num
     * @param integerDigits
     * @param fractionDigits
     * @return percentage style of double
     */
    public static String getPercentFormat(double num, int integerDigits, int fractionDigits) {
        NumberFormat numberFormat = NumberFormat.getPercentInstance();

        /** number of interger part. */
        numberFormat.setMaximumIntegerDigits(integerDigits);

        /** number of decimal part. */
        numberFormat.setMinimumFractionDigits(fractionDigits);
        return numberFormat.format(num);
    }

    /**
     * print usage message
     */
    public static void usage() {

        String usageString = "\n\t";
        usageString += "This modual combines the case samples or control sample, and generates the gene score matrix.";
        usageString += "\n\n";
        usageString += "usage: java Statistic"
                + "\n\t"
                + "-casein caseFolderPath: [required] The directory for case (Attention: Inheritane model folder must be in this directory!)."
                + "\n\t\t\t An input example: '/var/lib/case/geneScore' but NOT '/var/lib/case/geneScore/recessive_model'."
                + "\n\t"
                + "-controlin controlFolderPath: [required] The directory for control (Attention: Inheritane model folder must be in this directory!)."
                + "\n\t\t\t An input example: '/var/lib/control/geneScore' but NOT '/var/lib/control/geneScore/recessive_model'."
                + "\n\t"
                + "-frequency toBeShuffledFrequencyFilePath: [required] The path to the file whose content is a list of frequency, such as \"0.02, 0.03 ...\"."
                + "\n\t"
                + "-inheritance inheritanceModel: [required] The inheritance model. Two values will be accepted, such as 'recessive_model' or 'dominant_model'."
                + "\n\t"
                + "-caseout caseOutputPath: [required] The case output path."
                + "\n\t"
                + "-controlout controlOutputPath: [required] The control output path.";
        System.out.println(usageString);
    }

    public void processInput(String[] args) {
        for (int i = 0; i < 12; i++) {
            if (i % 2 == 0) {
                switch (InputEnum.valueOf(args[i].substring(1).toUpperCase())) {
                    case CASE_IN:
                        dirJudge(args[++i]);
                        this.caseFolderPath = getCanonicalPath(args[i]);
                        break;
                    case CONTROL_IN:
                        dirJudge(args[++i]);
                        this.controlFolderPath = getCanonicalPath(args[i]);
                        break;
                    case FREQUENCY:
                        fileJudge(args[++i]);
                        this.toBeShuffledFrequencyFilePath = getCanonicalPath(args[i]);
                        break;
                    case INHERITANCE:
                        this.inheritanceModel = args[++i];
                        if (!this.inheritanceModel.matches("(.*recessive.*|.*dominant.*)")) {
                            System.err.println("-inheritance parameter error! Please input 'recessive_model' or 'dominant_model'!");
                            System.exit(1);
                        }
                        break;
                    case CASE_OUT:
                        dirCreate(args[++i]);
                        this.caseOutputPath = getCanonicalPath(args[i]);
                        break;
                    case CONTROL_OUT:
                        dirCreate(args[++i]);
                        this.controlOutputPath = getCanonicalPath(args[i]);
                        break;
                }
            }
        }
    }

    /**
     * get matrix of gene score from ".geneScore" files in the input folder.
     *
     * @param geneScoreFileFolder
     * @param caseOrControl       ("case" or "control")
     * @param inheritanceModel    ("dominant_model" or "recessive_model")
     * @param outputFolder
     */
    public void getGeneScoreMatrixFromGeneScoreFiles(String geneScoreFileFolder, String caseOrControl, String inheritanceModel, String outputFolder) {
        LinkedHashMap<String, List<String>> geneScoreMap = new LinkedHashMap<String, List<String>>();

        if (caseOrControl.equals("case")) {
            outputFolder += File.separator + getFileName(geneScoreFileFolder);
            System.out.println("Getting gene score matrix for case " + getFileName(geneScoreFileFolder) + "!");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            dirCreate(outputFolder);
        } else {
            System.out.println("Getting gene score matrix for control!");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<String> fileNameList = new ArrayList<String>(Arrays.asList(new File(geneScoreFileFolder).list()));

        File outputFile = new File(outputFolder + File.separator + caseOrControl + "_" + inheritanceModel + ".geneScoreMatrix");
        fileCreate(getCanonicalPath(outputFile));

        FileWriter fWriter = null;
        try {
            fWriter = new FileWriter(outputFile);
            for (int i = 0; i < fileNameList.size(); i++) {
                String fileNameItem = fileNameList.get(i);
                System.out.println("Reading " + (i + 1) + " :" + fileNameItem + "!");

                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(geneScoreFileFolder + File.separator + fileNameItem));
                    String tempString = null;
                    while ((tempString = br.readLine()) != null) {
                        tempString = tempString.trim();

                        /** skip the header in the file. */
                        if (tempString.startsWith("#"))
                            continue;

                        String[] columns = tempString.split("\\s+");

                        /** columns[0] is gene name, columns[1] is gene score. */
                        if (geneScoreMap.get(columns[0]) == null) {
                            List<String> individualGeneScoreItemList = new ArrayList<String>();
                            for (int j = 0; j < i; j++) {
                                individualGeneScoreItemList.add("N/A");
                            }
                            individualGeneScoreItemList.add(columns[1]);
                            geneScoreMap.put(columns[0], individualGeneScoreItemList);
                        } else {
                            List<String> individualGeneScoreItemList = geneScoreMap.get(columns[0]);
                            for (int j = individualGeneScoreItemList.size(); j < i; j++) {
                                individualGeneScoreItemList.add("N/A");
                            }
                            individualGeneScoreItemList.add(columns[1]);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    br.close();
                }
            }
            System.out.println("Got the gene score matrix in memory!");
            System.out.println("Now output it into a file!");
            StringBuilder writeStringBuilder = new StringBuilder();

            /** get and write the header of gene score matrix file. */
            writeStringBuilder.append("#geneName");
            for (String fileNameItem : fileNameList) {
                /** get the sampel's name, remove the suffix ".geneScore" from it. */
                String[] columns = fileNameItem.split("\\.");
                writeStringBuilder.append("\t" + columns[0]);
            }
            writeStringBuilder.append("\n");
            fWriter.write(writeStringBuilder.toString(), 0, writeStringBuilder.toString().length());
            fWriter.flush();

            for (Entry<String, List<String>> geneEntry : geneScoreMap.entrySet()) {
                writeStringBuilder = new StringBuilder();
                writeStringBuilder.append(geneEntry.getKey());
                for (String geneScore : geneEntry.getValue()) {
                    writeStringBuilder.append("\t" + geneScore);
                }
                if (geneEntry.getValue().size() < fileNameList.size()) {
                    for (int j = geneEntry.getValue().size(); j < fileNameList.size(); j++) {
                        writeStringBuilder.append("\tN/A");
                    }
                }
                writeStringBuilder.append("\n");
                fWriter.write(writeStringBuilder.toString(), 0, writeStringBuilder.toString().length());
                fWriter.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting gene score matrix done!");
        System.out.println("Output file at " + outputFolder);
    }

    /**
     * get statistic matrix.
     *
     * @param caseGeneScoreMatrixFilePath
     * @param inheritanceModel
     * @param controlGeneScoreMatrixFilePath
     */
    public void getStatisticMatrix(String caseGeneScoreMatrixFilePath, String inheritanceModel, String controlGeneScoreMatrixFilePath) {
        String statisticMatrixPath = new File(caseGeneScoreMatrixFilePath).getParent() + File.separator + inheritanceModel + ".statisticMatrix";
        fileCreate(statisticMatrixPath);

        /** 把control file中的所有sample放入map中，然后遍历case中的sample，一个一个在control的map中找. */
        Map<String, String> controlGeneScoreMap = new TreeMap<String, String>();
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            br = new BufferedReader(new FileReader(controlGeneScoreMatrixFilePath));
            String tempReadString = null;

            while ((tempReadString = br.readLine()) != null) {

                /** skip the header.*/
                if (tempReadString.startsWith("#"))
                    continue;

                String[] columns = tempReadString.replaceFirst("\\s+", "#").split("#");
                controlGeneScoreMap.put(columns[0], columns[1]);
            }
            br.close();
            fw = new FileWriter(statisticMatrixPath);

            /** write the header. */
            String writeString = "#Gene\tRS\tB\tW\tX2\tn1\tn2\tm1\tm2\n";
            fw.write(writeString, 0, writeString.length());
            br = new BufferedReader(new FileReader(caseGeneScoreMatrixFilePath));

            System.out.println("Reading " + caseGeneScoreMatrixFilePath + " now!");
            while ((tempReadString = br.readLine()) != null) {
                /** skip the header. */
                if (tempReadString.startsWith("#"))
                    continue;

                String[] columns = tempReadString.replaceFirst("\\s+", "#").split("#");
                List<Double> geneStatisticList = computeRankSumAndStatistic(columns[1], controlGeneScoreMap.get(columns[0]));
                writeString = columns[0];
                for (Double geneStaticticItem : geneStatisticList) {
                    writeString += "\t" + geneStaticticItem;
                }
                writeString += "\n";
                fw.write(writeString, 0, writeString.length());
                System.out.println("Statistic computing for gene '" + columns[0] + "' done!");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null)
                    br.close();
                if (fw != null)
                    fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Getting statistic matrix finished!");
        System.out.println("Statistic matrix output at " + statisticMatrixPath);
    }

    /**
     * compute Rank Sum And Statistic between caseScoreString and controlScoreString seperated by '\t'
     *
     * @param caseScores
     * @param controlScores
     * @return a list contains RS,B,W,X2,n1,n2,m1,m2;
     */
    public List<Double> computeRankSumAndStatistic(String caseScores, String controlScores) {
        String[] caseScoreArray = caseScores.split("\\s+");
        String[] controlScoreArray = null;
        if (controlScores != null)
            controlScoreArray = controlScores.split("\\s+");
        double RS, B, W, X2, n1, n2, m1, m2;
        n1 = caseScoreArray.length;
        n2 = n1;
        List<Double> caseScoreNonNAList = new ArrayList<Double>();
        List<Double> controlScoreNonNAList = new ArrayList<Double>();
        for (String score : caseScoreArray) {
            if (!score.equals("N/A"))
                caseScoreNonNAList.add(Double.valueOf(score));
        }
        m1 = n1 - caseScoreNonNAList.size();
        if (controlScores != null) {
            for (String score : controlScoreArray) {
                if (!score.equals("N/A"))
                    controlScoreNonNAList.add(Double.valueOf(score));// how can 'm2' be a negative value?
            }
            m2 = n2 - controlScoreNonNAList.size();
        } else
            m2 = n2;

        List<Double> allscores = new ArrayList<Double>();
        allscores.addAll(caseScoreNonNAList);
        allscores.addAll(controlScoreNonNAList);
        if (controlScoreNonNAList.size() == 0)
            RS = 0;
        else
            RS = computeRankSum(caseScoreNonNAList, allscores);

        double p1 = (double) m1 / n1;
        double p2 = (double) m2 / n2;
        double p = (double) (m1 + m2) / (n1 + n2);

        // compute B(Zp)
        if (m1 == m2 && m1 == 0 || m1 == n2 && m2 == n2)
            B = 0;
        else {
            B = (p1 - p2) / Math.sqrt((double) p * (1 - p) * (n1 + n2) / (n1 * n2));
        }
        // compute W(Zu)
        if (m1 == n2 || m2 == n2)
            W = 0;
        else {
            double numerator = RS - (double) (n1 - m1) * (n1 - m1 + n2 - m2 + 1) / 2;
            double denominator = Math.sqrt((double) (n1 - m1) * (n2 - m2) * (n1 - m1 + n2 - m2 + 1) / 12);
            W = numerator / denominator;
        }
        X2 = B * B + W * W;
        return Arrays.asList(RS, B, W, X2, n1, n2, m1, m2);
    }

    /**
     * compute rank sum of cases
     *
     * @param caseScoreList  without "N/A"
     * @param totalScoreList without "N/A"
     * @return rankSum
     */
    public double computeRankSum(List<Double> caseScoreList, List<Double> totalScoreList) {
        if (totalScoreList.size() == 0) {
            System.out.println("totalScoreList size is zero!");
            return 0;
        }
        Collections.sort(totalScoreList);
        int i = 0, rank = 0, count = 0;
        Map<Double, Double> rankMap = new HashMap<Double, Double>();
        for (double currentValue = totalScoreList.get(0); i < totalScoreList.size(); i++) {
            currentValue = totalScoreList.get(i);
            rank += i + 1;
            count++;
            if (i + 1 == totalScoreList.size()) {
                rankMap.put(currentValue, (double) rank / count);
                i++;
            } else if (currentValue != totalScoreList.get(i + 1)) {
                rankMap.put(currentValue, (double) rank / count);
                rank = 0;
                count = 0;
            }
        }
        double rankSum = 0;
        for (Double caseScore : caseScoreList) {
            rankSum += rankMap.get(caseScore);
        }
        return rankSum;
    }


    public String getCaseFolderPath() {
        return caseFolderPath;
    }

    public void setCaseFolderPath(String caseFolderPath) {
        this.caseFolderPath = caseFolderPath;
    }

    public String getControlFolderPath() {
        return controlFolderPath;
    }

    public void setControlFolderPath(String controlFolderPath) {
        this.controlFolderPath = controlFolderPath;
    }

    public String getToBeShuffledFrequencyFilePath() {
        return toBeShuffledFrequencyFilePath;
    }

    public void setToBeShuffledFrequencyFilePath(
            String toBeShuffledFrequencyFilePath) {
        this.toBeShuffledFrequencyFilePath = toBeShuffledFrequencyFilePath;
    }

    public String getInheritanceModel() {
        return inheritanceModel;
    }

    public void setInheritanceModel(String inheritanceModel) {
        this.inheritanceModel = inheritanceModel;
    }

    public String getCaseOutputPath() {
        return caseOutputPath;
    }

    public void setCaseOutputPath(String caseOutputPath) {
        this.caseOutputPath = caseOutputPath;
    }

    public String getControlOutputPath() {
        return controlOutputPath;
    }

    public void setControlOutputPath(String controlOutputPath) {
        this.controlOutputPath = controlOutputPath;
    }

    enum InputEnum {
        CASE_IN,      //input option "-casein"
        CONTROL_IN,   //input option "-controlin"
        FREQUENCY,   //input option "-frequency"
        INHERITANCE, //input option "-inheritance"
        CASE_OUT,     //input option "-caseout"
        CONTROL_OUT;  //input option "-controlout"
    }

}
