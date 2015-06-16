/**
 * @Copyright cn.edu.fudan.iipl
 */

package cn.edu.fudan.iipl.ourvaast;

import static cn.edu.fudan.iipl.util.FileUtil.copyFile;
import static cn.edu.fudan.iipl.util.FileUtil.dirCreate;
import static cn.edu.fudan.iipl.util.FileUtil.dirJudge;
import static cn.edu.fudan.iipl.util.FileUtil.fileCreate;
import static cn.edu.fudan.iipl.util.FileUtil.fileJudge;
import static cn.edu.fudan.iipl.util.FileUtil.getCanonicalPath;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Shuffle positive variants into case.
 *
 * @author Yong Chen
 * @since 2015-01-28
 */
public class Shuffle {

  private String caseFolderPath = null;
  private String diseaseVariantsFilePath = null;
  private String toBeShuffledGeneNameFilePath = null;
  private String toBeShuffledFrequencyFilePath = null;
  private int toBeShuffledVariantsNumber = 0;
  private String shuffledCaseOutputPath = null;

  public static void main(String[] args) {
    if (args.length < 12) {
      usage();
      return;
    }

    Shuffle shuffle = new Shuffle();


    /** process input,  preserve args. */
    shuffle.processInput(args);

    /** create shuffled case output path. */
    shuffle.setShuffledCaseOutputPath(
      shuffle.getShuffledCaseOutputPath() + File.separator + "shuffledCase");
    dirCreate(shuffle.getShuffledCaseOutputPath());


    /** read disease names. */
    List<String> diseaseGeneNameList = new ArrayList<String>();
    BufferedReader geneNameFileReader = null;
    try {
      geneNameFileReader =
        new BufferedReader(new FileReader(shuffle.getToBeShuffledGeneNameFilePath()));
      String tempString = null;
      while ((tempString = geneNameFileReader.readLine()) != null) {
        String[] geneNames = tempString.trim().split("\\s+");
        for (String geneName : geneNames) {
          diseaseGeneNameList.add(geneName);
        }
      }
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        geneNameFileReader.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }


    /** read frequency list. */
    List<Double> frequencyList = new ArrayList<Double>();
    BufferedReader frequencyFileReader = null;
    try {
      frequencyFileReader =
        new BufferedReader(new FileReader(shuffle.getToBeShuffledFrequencyFilePath()));
      String tempString = null;
      while ((tempString = frequencyFileReader.readLine()) != null) {
        String[] frequencies = tempString.trim().split("\\s+");
        for (String frequency : frequencies) {
          frequencyList.add(Double.parseDouble(frequency));
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


    /** shuffle. */
    shuffle.shuffleAccordingToGeneName(shuffle.getDiseaseVariantsFilePath(), diseaseGeneNameList,
      frequencyList, shuffle.getToBeShuffledVariantsNumber());


    /** copy. */
    shuffle.copyCaseFilesToShuffledCaseFolder(shuffle.getCaseFolderPath(), frequencyList);
    /** finished shuffling. */
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
    usageString +=
      "Shuffle positive variants into case files follow frequencies you defined. You should give the path of a positive variants file, path of a frequency file, path of a positive gene names file. Please see the [options] below.";
    usageString += "\n\n";
    usageString += "usage: java Shuffle" + "\n\t"
      + "-t caseFolderPath: [required] The directory for case which you want to shuffle disease variants into."
      + "\n\t"
      + "-v diseaseVariantsFilePath: [required] The path to pathogenic variants file(all variants are scored by dbNSFP tool), such as Clinvar or HGMD"
      + "\n\t"
      + "-g toBeShuffledGeneNameFilePath: [required] The path to the file whose content is a list of disease gene names."
      + "\n\t"
      + "-f toBeShuffledFrequencyFilePath: [required] The path to the file whose content is a list of frequency, such as \"0.02, 0.03 ...\"."
      + "\n\t"
      + "-n toBeShuffledVariantsNumber: [required] Number of variants to be picked up and shuffled into each case sample. \n\t\t"
      + "First we extract all pathogenic variants from the file [-v diseaseVariantsFilePath] you input. So we get all pathogenic variants you are interested in. \n\t\t"
      + "For each sample, we randomly picked up this number(you input from [-n shuffledVariantsNumber]) of variants from all these pathogenic variants, \n\t\t"
      + "and shuffled them into samples according to frequency." + "\n\t"
      + "-o shuffledCaseOutputPath: [required] The output path.";
    System.out.println(usageString);
  }

  public void processInput(String[] args) {
    for (int i = 0; i < 12; i++) {
      if (i % 2 == 0) {
        switch (args[i].charAt(1)) {
          case 't':
            dirJudge(args[++i]);
            this.caseFolderPath = getCanonicalPath(args[i]);
            break;
          case 'v':
            fileJudge(args[++i]);
            this.diseaseVariantsFilePath = getCanonicalPath(args[i]);
            break;
          case 'g':
            fileJudge(args[++i]);
            this.toBeShuffledGeneNameFilePath = getCanonicalPath(args[i]);
            break;
          case 'f':
            fileJudge(args[++i]);
            this.toBeShuffledFrequencyFilePath = getCanonicalPath(args[i]);
            break;
          case 'n':
            this.toBeShuffledVariantsNumber = Integer.parseInt(args[++i]);
            break;
          case 'o':
            dirCreate(args[++i]);
            this.shuffledCaseOutputPath = getCanonicalPath(args[i]);
            break;
        }
      }
    }
  }

  /**
   * step 1#
   * shuffle positive variants to case files according to gene name list.
   *
   * @param diseaseFilePath     path of disease related variants, clinvar or hgmd ,
   *                            predisposed by scoring system dbsnfp2.7 or higher version.
   * @param diseaseGeneNameList list of disease gene names which vatiants belong to,
   *                            you wanted to shuffle these variants(For each gene, 2 variants for recessive model,
   *                            1 variant for dominant model) to case files.
   * @param concentrationList   represent by a list of Double variables, such as "0.10, 0.20, 0.30".
   * @param variantSampleNumber in general, you will choose 2 for recessive model in this parameter, 1 for dominant model.
   * @return true if shuffling succeed, false for failed
   */
  public boolean shuffleAccordingToGeneName(String diseaseFilePath,
    List<String> diseaseGeneNameList, List<Double> concentrationList, int variantSampleNumber) {
    try {
      //getCanonicalPath and check parameters
      caseFolderPath = getCanonicalPath(caseFolderPath);
      dirJudge(caseFolderPath.toString());
      fileJudge(diseaseFilePath);
      if (diseaseGeneNameList.isEmpty()) {
        System.out.println("diseaseGeneNameList is empty!");
        System.exit(1);
      }
      if (concentrationList.isEmpty()) {
        System.out.println("concentrationList is empty!");
        System.exit(1);
      }

      //now shuffle starts
      for (Double concentration : concentrationList) {
        File diseaseFile = new File(diseaseFilePath);
        File caseFolderFile = new File(caseFolderPath.toString());

        //get case file list
        List<File> caseFileList = Arrays.asList(caseFolderFile.listFiles());
        System.out.println("get case file list success!");

        int counts = (int) Math.ceil(caseFileList.size() * concentration);

        /**
         * put variants of genes exist in diseaseGeneNameList into a map in memory,
         * key of the map is gene name, value of the map is a list of variants of corresponding gene
         */
        Map<String, List<String>> geneVariantsMap = new HashMap<String, List<String>>();
        BufferedReader diseaseBufferedReader = new BufferedReader(new FileReader(diseaseFile));
        String tempString = null;
        List<String> variantList = new ArrayList<String>();
        while ((tempString = diseaseBufferedReader.readLine()) != null) {
          String[] diseaseFeature = tempString.trim().split("\\s+");
          if (diseaseGeneNameList.contains(diseaseFeature[4])) {
            if (!geneVariantsMap.containsKey(diseaseFeature[4])) {
              List<String> variantsList = new ArrayList<String>();
              StringBuilder writeString = new StringBuilder();
              for (int i = 0; i < diseaseFeature.length - 3; i++) {
                writeString.append(diseaseFeature[i] + "\t");
              }
              writeString.append(diseaseFeature[diseaseFeature.length - 3]);
              variantsList.add(writeString.toString());
              geneVariantsMap.put(diseaseFeature[4], variantsList);
            } else {
              StringBuilder writeString = new StringBuilder();
              for (int i = 0; i < diseaseFeature.length - 3; i++) {
                writeString.append(diseaseFeature[i] + "\t");
              }
              writeString.append(diseaseFeature[diseaseFeature.length - 3]);
              List<String> variantsList = geneVariantsMap.get(diseaseFeature[4]);
              variantsList.add(writeString.toString());
            }
          }
        }
        diseaseBufferedReader.close();
        System.out.println("Putting variants in diseaseGeneNameList into a list success!");
        System.out
          .println("length of variant list related to a certain disease:" + variantList.size());

        //check and create "concentration folder", such as "5%"
        StringBuilder concentrationPath =
          new StringBuilder(getShuffledCaseOutputPath()).append(File.separatorChar)
            .append(getPercentFormat(concentration, 2, 0));
        concentrationPath = new StringBuilder(dirCreate(concentrationPath.toString()));

        //shuffle list of case File
        List<File> shuffledCaseFileList = new ArrayList<File>(caseFileList);
        Collections.shuffle(shuffledCaseFileList);
        System.out.println("shuffle case file list success!");

        for (int i = 0; i < shuffledCaseFileList.size(); i++) {
          if (i < counts) {
            StringBuilder caseFilePathItemShuffled =
              new StringBuilder(concentrationPath).append(File.separatorChar)
                .append(shuffledCaseFileList.get(i).getName());
            caseFilePathItemShuffled =
              new StringBuilder(fileCreate(caseFilePathItemShuffled.toString()));
            BufferedReader caseFileBufferedReader = new BufferedReader(new FileReader(
              caseFolderPath.toString() + File.separatorChar + shuffledCaseFileList.get(i)
                .getName()));
            try {
              FileWriter fw = new FileWriter(caseFilePathItemShuffled.toString());
              try {
                tempString = caseFileBufferedReader.readLine();
                fw.write(tempString + "\n", 0, (tempString + "\n").length());
                for (String geneName : geneVariantsMap.keySet()) {
                  List<String> shuffledVariantList = geneVariantsMap.get(geneName);
                  Collections.shuffle(shuffledVariantList);
                  for (int j = 0; j < variantSampleNumber; j++)
                    fw.write(shuffledVariantList.get(j) + "\n", 0,
                      (shuffledVariantList.get(j) + "\n").length());
                }
                for (tempString = caseFileBufferedReader.readLine(); tempString != null; ) {
                  fw.write(tempString + "\n", 0, (tempString + "\n").length());
                  tempString = caseFileBufferedReader.readLine();
                }
              } finally {
                fw.close();
              }
            } finally {
              caseFileBufferedReader.close();
            }
          }
        }
        System.out.println("shuffleAccordingToGeneName finished!");
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * step 2#
   * copy case files to shuffled case folder if and only if case files those not exists in objective folder
   *
   * @param caseFolderPath
   */
  public void copyCaseFilesToShuffledCaseFolder(String caseFolderPath,
    List<Double> concentrationList) {
    dirJudge(caseFolderPath);
    dirJudge(getShuffledCaseOutputPath());

    File originalCaseFolderFile = new File(caseFolderPath);
    List<String> caseFileList = Arrays.asList(originalCaseFolderFile.list());

    for (Double concentration : concentrationList) {
      StringBuilder concentrationPath =
        new StringBuilder().append(getShuffledCaseOutputPath()).append(File.separatorChar)
          .append(getPercentFormat(concentration, 2, 0));
      concentrationPath = new StringBuilder(dirCreate(concentrationPath.toString()));

      File objectiveFolderFile = new File(concentrationPath.toString());
      Set<String> shuffledDiseaseSet = new HashSet<String>();
      Collections.addAll(shuffledDiseaseSet, objectiveFolderFile.list());

      for (String caseFile : caseFileList) {
        if (!shuffledDiseaseSet.contains(caseFile)) {
          System.out.println("copying " + caseFile);

          StringBuilder shuffledCaseFilePath =
            new StringBuilder(getCanonicalPath(objectiveFolderFile)).append(File.separatorChar)
              .append(caseFile);
          copyFile(caseFolderPath + File.separatorChar + caseFile, shuffledCaseFilePath.toString());
        }
      }
      System.out.println("percent " + getPercentFormat(concentration, 2, 0) + " done!");
    }
    System.out.println("copyCaseFilesToShuffledCaseFolder done!");
  }

  public String getCaseFolderPath() {
    return this.caseFolderPath;
  }

  public void setCaseFolderPath(String caseFolderPath) {
    this.caseFolderPath = caseFolderPath;
  }

  public String getDiseaseVariantsFilePath() {
    return diseaseVariantsFilePath;
  }

  public void setDiseaseVariantsFilePath(String diseaseVariantsFilePath) {
    this.diseaseVariantsFilePath = diseaseVariantsFilePath;
  }

  public String getToBeShuffledGeneNameFilePath() {
    return toBeShuffledGeneNameFilePath;
  }

  public void setToBeShuffledGeneNameFilePath(String shuffledGeneNameFilePath) {
    this.toBeShuffledGeneNameFilePath = shuffledGeneNameFilePath;
  }

  public String getToBeShuffledFrequencyFilePath() {
    return toBeShuffledFrequencyFilePath;
  }

  public void setToBeShuffledFrequencyFilePath(String shuffledFrequencyFilePath) {
    this.toBeShuffledFrequencyFilePath = shuffledFrequencyFilePath;
  }

  public int getToBeShuffledVariantsNumber() {
    return toBeShuffledVariantsNumber;
  }

  public void setToBeShuffledVariantsNumber(int shuffledVariantsNumber) {
    this.toBeShuffledVariantsNumber = shuffledVariantsNumber;
  }

  public String getShuffledCaseOutputPath() {
    return shuffledCaseOutputPath;
  }

  public void setShuffledCaseOutputPath(String shuffledCaseOutputPath) {
    this.shuffledCaseOutputPath = shuffledCaseOutputPath;
  }
}
