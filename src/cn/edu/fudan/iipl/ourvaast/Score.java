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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Score each gene
 *
 * @author Yong Chen
 * @since 2015-01-28
 */
public class Score {

    private String caseFolderPath = null;
    private String controlFolderPath = null;
    private String toBeShuffledFrequencyFilePath = null;
    private String inheritanceModel = null;
    private String caseOutputPath = null;
    private String controlOutputPath = null;

    public static void main(String[] args) {
        if (args.length < 12) {
            usage();
            return;
        }

        Score score = new Score();

        /** process input,  preserve args. */
        score.processInput(args);


        /** create case variant average score output path and case gene score output path. */
        String caseVariantAvgScoreOutputPath = score.getCaseOutputPath() + File.separator + "variantAvgScore";
        String caseGeneScoreOutputPath = score.getCaseOutputPath() + File.separator + "geneScore";
        dirCreate(caseVariantAvgScoreOutputPath);
        dirCreate(caseGeneScoreOutputPath);


        /** create case variant average score output path and case gene score output path. */
        String controlVariantAvgScoreOutputPath = score.getControlOutputPath() + File.separator + "variantAvgScore";
        String controlGeneScoreOutputPath = score.getControlOutputPath() + File.separator + "geneScore";
        dirCreate(controlVariantAvgScoreOutputPath);
        dirCreate(controlGeneScoreOutputPath);


        /** read frequency list. */
        List<Double> frequencyList = new ArrayList<Double>();
        BufferedReader frequencyFileReader = null;
        try {
            frequencyFileReader = new BufferedReader(new FileReader(score.getToBeShuffledFrequencyFilePath()));
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


        /** get average score of each variant. */
        /** case. */
        for (Double frequency : frequencyList)
            score.getAverageScoreForVariants(score.getCaseFolderPath() + File.separator + getPercentFormat(frequency, 2, 0),
                    "case", caseVariantAvgScoreOutputPath);
        /** control. */
        score.getAverageScoreForVariants(score.getControlFolderPath(), "control", controlVariantAvgScoreOutputPath);


        System.out.println("Getting average score for variants finished!");
        System.out.println("Now we will begin to score each gene in 5 seconds!");
        for (int i = 5; i >= 1; ) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Now we will begin to score each gene in " + (--i) + " seconds!");
        }


		/* score each gene. */
        for (int j = 0; j < frequencyList.size(); j++) {
            System.out.println("\n------- Score Each Gene start! Now processing frequency " + getPercentFormat(frequencyList.get(j), 2, 0) + "!\n");
            // case
            score.scoreEachGene(caseVariantAvgScoreOutputPath + File.separator + getPercentFormat(frequencyList.get(j), 2, 0),
                    "case", score.getInheritanceModel(), caseGeneScoreOutputPath, 5);
        }
        // control
        score.scoreEachGene(controlVariantAvgScoreOutputPath, "control", score.getInheritanceModel(), controlGeneScoreOutputPath, 5);
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
        usageString += "This modual computes an average of four algorithm scores for every variant in a sample. Then stores the average at column '5'"
                + "(column starts from '0', see the output file with suffix '.avgScore'). At last, this modual score all the genes in a sample,"
                + " see the output file with suffix '.geneScore'.";
        usageString += "\n\n";
        usageString += "usage: java Score"
                + "\n\t"
                + "-casein caseFolderPath: [required] The directory for case (Attention: Frequencies folder must be in this directory!)."
                + "\n\t\t\t An input example: '/var/lib/case' but NOT '/var/lib/case/2%'."
                + "\n\t"
                + "-controlin controlFolderPath: [required] The directory for control. An input example: '/var/lib/control'."
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
     * <pre> get average score for every variant in certain folder, if case,
     *  will generate concentration folder in output folder.
     * <br/> we use rank score instead of scores generated by
     * the algorithms because rank scores are from 0 to 1.
     * <br/> rank scores of four algorithms are located at column 6, 9,
     *  13, 15 respectively (column starts from 0).
     * </pre>
     *
     * @param folderPath
     * @param caseOrControl
     * @param outputFolderPath
     */
    public void getAverageScoreForVariants(String folderPath, String caseOrControl, String outputFolderPath) {
        try {
            File folder = new File(folderPath);
            System.out.println(folderPath);
            List<String> fileList = Arrays.asList(folder.list());

            dirCreate(outputFolderPath);
            if (caseOrControl.equals("case")) {
                String frequency = getFileName(folderPath);
                outputFolderPath += File.separator + frequency;
                dirCreate(outputFolderPath);
            }

            for (String fileItem : fileList) {
                File file = new File(folderPath + File.separator + fileItem);
                BufferedReader bReader = new BufferedReader(new FileReader(file));

                File averageScoreFile = new File(outputFolderPath + File.separator + fileItem + ".avgScore");
                FileWriter fWriter = new FileWriter(averageScoreFile);
                try {
                    String tempString = bReader.readLine();
                    String[] feature = tempString.trim().split("\\s+");
                    String writeString = feature[0] + "\t" + feature[1] + "\t" + feature[2] + "\t" + feature[3] + "\t" + feature[4] + "\t" + "average_score";
                    for (int i = 5; i < feature.length; i++)
                        writeString += "\t" + feature[i];
                    writeString += "\n";
                    fWriter.write(writeString, 0, writeString.length());
                    fWriter.flush();

                    while ((tempString = bReader.readLine()) != null) {
                        feature = tempString.trim().split("\\s+");
                        writeString = "";
                        for (int i = 0; i < 5; i++) {
                            writeString += feature[i] + "\t";
                        }

                        /** calculate average. */
                        double averageScore = countAverage(Arrays.asList(feature[6], feature[9], feature[13], feature[15]));
                        String averageScoreString = null;
                        DecimalFormat dFormat = new DecimalFormat("#####0.000000");
                        if (averageScore == -1) {
                            averageScoreString = ".";
                        } else {
                            averageScoreString = dFormat.format(averageScore);
                        }

                        writeString += averageScoreString;
                        for (int i = 5; i < feature.length; i++) {
                            writeString += "\t" + feature[i];
                        }
                        writeString += "\n";
                        fWriter.write(writeString, 0, writeString.length());
                        fWriter.flush();
                    }
                } finally {
                    bReader.close();
                    fWriter.close();
                }
                System.out.println(fileItem + " got variants' average score at " + averageScoreFile.getCanonicalPath() + " !");
            }
            System.out.println("counting average score finished!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * count average of 4 double number, one or more of which may not be a
     * number, that is, '.' perhaps.
     *
     * @param list
     * @return average of double numbers in the list.
     */
    public double countAverage(List<String> list) {
        double average = 0;
        int count = 0;
        for (String scoreItem : list) {
            if (!scoreItem.equals(".")) {
                average += Double.parseDouble(scoreItem);
                count++;
            }
        }
        if (average != 0)
            average /= count;
        return average;
    }

    /**
     * score each gene in certain inheritance model.
     * #for recessive model, only genes those who get two or more variants were scored. use the sum of the two highest variant-scores as the score of each gene
     * #for dominant model, genes contain one or more variants were scored. use the highest variant-score as the score of each gene.
     *
     * @param samplesFolderPath folder path of samples
     * @param inheritanceModel  "recessive_model" or "dominant_model"
     * @param outputPath
     * @param column            index of algorithm score in variant samples' files, without any averageScore, SVM=5, LR=8, VEST=12, CADD=14.
     * @note we haven't do "remove one of two variants in cis (the two variants closely reside on the same read) and keep the one with the higher score".
     */
    public void scoreEachGene(String samplesFolderPath, String caseOrControl, String inheritanceModel, String outputPath, int column) {
        try {


            /** get frequency(percentage format). */
            String frequency = getFileName(samplesFolderPath);

            outputPath = outputPath + File.separator + inheritanceModel;
            dirCreate(outputPath);

            if (caseOrControl.equals("case")) {
                outputPath = outputPath + File.separator + frequency;
                dirCreate(outputPath);
            }

            System.out.println("You choose \"" + inheritanceModel + "\" model!");

            File folderFile = new File(samplesFolderPath);
            List<String> fileList = Arrays.asList(folderFile.list());

            for (String fileItemName : fileList) {
                System.out.println("scoreEachGene: processing " + samplesFolderPath + File.separator + fileItemName);


                /** remove the suffix ".score.out.avgScore" from the file name. */
                String[] sampleName = fileItemName.split("\\.");
                File geneScoreFile = new File(outputPath + File.separator + sampleName[0] + ".genescore");
                fileCreate(geneScoreFile.getCanonicalPath());

                /** create a hashmap to store gene score matrix. */
                HashMap<String, List<String>> geneScoreMap = new HashMap<String, List<String>>();


                BufferedReader bReader = null;
                FileWriter fWriter = null;
                String tempString = null;


                try {
                    bReader = new BufferedReader(new FileReader(samplesFolderPath + File.separator + fileItemName));
                    fWriter = new FileWriter(geneScoreFile);

                    /** get the header. */
                    tempString = bReader.readLine();
                    String[] columns = tempString.trim().split("\\s");
                    String writeString = "#" + columns[4] + "\t" + "geneScore" + "\n";
                    fWriter.write(writeString, 0, writeString.length());
                    fWriter.flush();


                    while ((tempString = bReader.readLine()) != null) {
                        columns = tempString.trim().split("\\s+");

                        /**
                         * columns[4] is gene name, columns[5] is the average
                         * score of the variant.
                         */
                        if (geneScoreMap.containsKey(columns[4])) {
                            List<String> keyList = new ArrayList<String>();
                            for (String string : geneScoreMap.get(columns[4]))
                                keyList.add(string);
                            keyList.add(columns[column]);
                            geneScoreMap.put(columns[4], keyList);
                        } else
                            geneScoreMap.put(columns[4], Arrays.asList(columns[column]));
                    }


                    for (Entry<String, List<String>> entry : geneScoreMap.entrySet()) {
                        writeString = entry.getKey() + "\t";
                        List<String> scoreList = entry.getValue();
                        if (inheritanceModel.matches("(.)*recessive(.)*")) {
                            if (scoreList.size() < 2)
                                writeString += "N/A\n";
                            else {
                                writeString += getGeneScoreRecessiveModel(scoreList) + "\n";
                            }
                        } else if (inheritanceModel.matches("(.)*dominant(.)*")) {
                            Collections.sort(scoreList);

                            /** The highest score located at the last position after sorting operation. */
                            writeString += scoreList.get(scoreList.size() - 1) + "\n";
                        } else {
                            System.out.println("inheritance model error!");
                            return;
                        }
                        fWriter.write(writeString, 0, writeString.length());
                    }
                } finally {
                    fWriter.close();
                    bReader.close();
                }
            }
            System.out.println("scoreEachGene finished!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get sum of two highest score in a list.
     *
     * @param list list of scores
     * @return the sum of two highest score in a list.
     */
    public String getGeneScoreRecessiveModel(List<String> list) {
        if (list.size() < 2) {
            System.out.println("getGeneScoreRecessiveModel error! length of list is less than 2");
            return null;
        }
        Collections.sort(list, new MyComparator());
        DecimalFormat dFormat = new DecimalFormat("#####0.000000");
        if (list.get(list.size() - 1).trim().equals(".") || list.get(list.size() - 2).trim().equals("."))
            return "N/A";
        double sum = Double.parseDouble(list.get(list.size() - 1)) + Double.parseDouble(list.get(list.size() - 2));
        return dFormat.format(sum);
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

    class MyComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            o1 = o1.trim();
            o2 = o2.trim();
            if (o1.equals(".")) {
                if (o2.equals("."))  // o1 is "." and o2 is "."
                    return 0;
                if (!o2.equals(".")) // o1 is "." and o2 is not "."
                    return -1;
            } else {
                if (o2.equals(".")) // o1 is not "." and o2 is "."
                    return 1;
            }

            // both o1 and o2 are numbers.
            return Double.valueOf(o1).compareTo(Double.valueOf(o2));
        }
    }
}
