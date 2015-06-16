/**
 * @Copyright cn.edu.fudan.iipl
 */

package cn.edu.fudan.iipl.ourvaast;

import static cn.edu.fudan.iipl.util.FileUtil.fileCreate;
import static cn.edu.fudan.iipl.util.FileUtil.dirCreate;
import static cn.edu.fudan.iipl.util.FileUtil.fileJudge;
import static cn.edu.fudan.iipl.util.FileUtil.dirJudge;
import static cn.edu.fudan.iipl.util.FileUtil.getCanonicalPath;
import static cn.edu.fudan.iipl.util.FileUtil.copyFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <pre>Generate R script with statistic matrix file and run it.
 * Identity disease genes.
 * </pre>
 *
 * @author Yong Chen
 * @since 2015-01-28
 */
public class Identify {

  private String caseFolderPath = null;
    // recessive_model folder in this path, then the frequencies folder.
  private String toBeShuffledFrequencyFilePath = null;
  private String inheritanceModel = null;
  private String outputPath = null;

  public static void main(String[] args) {
    if (args.length < 8) {
      usage();
      return;
    }

    Identify identify = new Identify();

    /** process input,  preserve args. */
    identify.processInput(args);

    /** enter the inheritance model folder. */
    identify.setCaseFolderPath(
      identify.getCaseFolderPath() + File.separator + identify.getInheritanceModel());
    dirJudge(identify.getCaseFolderPath());

    /** read frequency list. */
    List<Double> frequencyList = new ArrayList<Double>();
    BufferedReader frequencyFileReader = null;
    try {
      frequencyFileReader =
        new BufferedReader(new FileReader(identify.getToBeShuffledFrequencyFilePath()));
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

    for (int j = 0; j < frequencyList.size(); j++) {
      String statisticMatrixPath =
        identify.getCaseFolderPath() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0)
          + File.separator + identify.getInheritanceModel() + ".statisticMatrix";
      fileJudge(statisticMatrixPath);
      identify.generateRScriptWithStatisticMatrix(statisticMatrixPath);
      identify.runRscrpt(statisticMatrixPath + ".r", getPercentFormat(frequencyList.get(j), 2, 0));

      /** copy result file to output folder. */
      String src = new File(statisticMatrixPath).getParent() + File.separator + getPercentFormat(
        frequencyList.get(j), 2, 0) + "_" + identify.getInheritanceModel() + ".txt";
      String obj =
        identify.getOutputPath() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0)
          + "_" + identify.getInheritanceModel() + ".txt";
      copyFile(src, obj);
      System.out.println("Result file output at " + obj + "!");
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
    usageString +=
      "This modual generates Rscript with statistic maxtrix file. Then run the script to get the result.";
    usageString += "\n\n";
    usageString += "usage: java Identify" + "\n\t"
      + "-casein caseFolderPath: [required] The directory for case (Attention: Inheritane model folder must be in this directory!)."
      + "\n\t\t\t An input example: '/var/lib/case/geneScoreMatrix' but NOT '/var/lib/case/geneScoreMatrix/recessive_model'."
      + "\n\t"
      + "-frequency toBeShuffledFrequencyFilePath: [required] The path to the file whose content is a list of frequency, such as \"0.02, 0.03 ...\"."
      + "\n\t"
      + "-inheritance inheritanceModel: [required] The inheritance model. Two values will be accepted, such as 'recessive_model' or 'dominant_model'."
      + "\n\t" + "-out outputPath: [required] The output path.";
    System.out.println(usageString);
  }

  public void processInput(String[] args) {
    for (int i = 0; i < 8; i++) {
      if (i % 2 == 0) {
        switch (InputEnum.valueOf(args[i].substring(1).toUpperCase())) {
          case CASE_IN:
            dirJudge(args[++i]);
            this.caseFolderPath = getCanonicalPath(args[i]);
            break;
          case FREQUENCY:
            fileJudge(args[++i]);
            this.toBeShuffledFrequencyFilePath = getCanonicalPath(args[i]);
            break;
          case INHERITANCE:
            this.inheritanceModel = args[++i];
            if (!this.inheritanceModel.matches("(.*recessive.*|.*dominant.*)")) {
              System.err.println(
                "-inheritance parameter error! Please input 'recessive_model' or 'dominant_model'!");
              System.exit(1);
            }
            break;
          case OUT:
            dirCreate(args[++i]);
            this.outputPath = getCanonicalPath(args[i]);
            break;
        }
      }
    }
  }

  /**
   * Generate RScript with statistic matrix file.
   *
   * @param statisticMatrixPath the path to statistic matrix file.
   */
  public void generateRScriptWithStatisticMatrix(String statisticMatrixPath) {
    String rscriptFilePath = statisticMatrixPath + ".r";
    fileCreate(rscriptFilePath);

    BufferedReader br = null;
    FileWriter fw = null;
    try {
      String readTempString = null;
      String writeString = null;
      br = new BufferedReader(new FileReader(statisticMatrixPath));
      fw = new FileWriter(rscriptFilePath);
      while ((readTempString = br.readLine()) != null) {

        /** skip the header. */
        if (readTempString.startsWith("#"))
          continue;

        String[] columns = readTempString.split("\\s+");

        if (columns[0].contains("-")) {
          System.out.print("Converted " + columns[0]);
          columns[0] = columns[0].replace("-", "___");
          System.out.println(" to " + columns[0] + "!");
        }
        if (columns[0].contains(";")) {
          String[] subFeature = columns[0].split(";");
          for (String subFeatureItem : subFeature) {
            writeString = subFeatureItem + "=pchisq(" + columns[4] + ",2, lower.tail=FALSE)\n\""
              + subFeatureItem + "\"\n" + subFeatureItem + "\n";
            fw.write(writeString, 0, writeString.length());
            fw.flush();
          }
          continue;
        }
        writeString =
          columns[0] + "=pchisq(" + columns[4] + ",2, lower.tail=FALSE)\n\"" + columns[0] + "\"\n"
            + columns[0] + "\n";
        fw.write(writeString, 0, writeString.length());
        fw.flush();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        fw.close();
        br.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    System.out.println("Generating Rscript at " + rscriptFilePath + "!");
  }

  /**
   * run Rscript under linux(There will be some problems if under windows),
   * output will be generated in the same folder
   *
   * @param rscriptFilePath path of rscript
   */
  public void runRscrpt(String rscriptFilePath, String frequency) {
    try {
      rscriptFilePath = getCanonicalPath(rscriptFilePath);
      fileJudge(rscriptFilePath);

      String resultPath =
        new File(rscriptFilePath).getParent() + File.separator + this.getInheritanceModel() + "_"
          + frequency + ".txt";
      String[] cmds = {"/bin/bash", "-c", "Rscript " + rscriptFilePath + " > " + resultPath};
      Process pb = Runtime.getRuntime().exec(cmds);
      System.out.println(rscriptFilePath + " running success!");
      BufferedReader outputbr = null;
      String tempString = null;
      try {
        outputbr = new BufferedReader(new InputStreamReader(pb.getInputStream()));
        while ((tempString = outputbr.readLine()) != null) {
          System.out.println(tempString);
        }
      } finally {
        outputbr.close();
      }
      BufferedReader errbr = null;
      try {
        errbr = new BufferedReader(new InputStreamReader(pb.getErrorStream()));
        while ((tempString = errbr.readLine()) != null) {
          System.err.println(tempString);
        }
      } finally {
        errbr.close();
      }
      int exitValue = pb.waitFor();
      if (exitValue != 0)
        pb.destroy();

      convertRawoutToReadable(resultPath, frequency);

    } catch (IOException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * convert raw output format to readable.
   *
   * @param rawoutFilePath
   * @param frequency
   */
  public void convertRawoutToReadable(String rawoutFilePath, String frequency) {
    File rawoutFile = new File(rawoutFilePath);
    fileJudge(rawoutFilePath);
    String readableFilePath =
      new File(rawoutFilePath).getParent() + File.separator + frequency + "_" + this
        .getInheritanceModel() + ".txt";
    fileCreate(readableFilePath);

    String readTempString = null;
    String writeString = null;
    BufferedReader bReader = null;
    FileWriter fWriter = null;

    Map<String, String> genePvalueMap = new HashMap<String, String>();

    List<Map.Entry<String, String>> genePvalueList = new LinkedList<Map.Entry<String, String>>();
    try {
      bReader = new BufferedReader(new FileReader(rawoutFilePath));
      fWriter = new FileWriter(readableFilePath);
      writeString = "#Gene\tTwopart_P_value\n";
      fWriter.write(writeString, 0, writeString.length());
      while ((readTempString = bReader.readLine()) != null) {
        String[] geneNameFeature = readTempString.split("\\s+");
        readTempString = bReader.readLine();
        System.out.println("converting " + geneNameFeature[1].replace("\"", ""));
        String[] pvalueFeature = readTempString.split("\\s+");
        genePvalueMap.put(geneNameFeature[1].replaceAll("\"", ""), pvalueFeature[1]);
      }

      genePvalueList.addAll(genePvalueMap.entrySet());
      Collections.sort(genePvalueList, new PvalueComparator());
      for (Map.Entry<String, String> genePvalue : genePvalueList) {
        writeString = genePvalue.getKey() + "\t" + genePvalue.getValue() + "\n";
        fWriter.write(writeString, 0, writeString.length());
      }
      genePvalueList = null;
      genePvalueMap = null;

    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        bReader.close();
        fWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    if (rawoutFile.exists())
      rawoutFile.delete();
  }

  public String getCaseFolderPath() {
    return caseFolderPath;
  }

  public void setCaseFolderPath(String caseFolderPath) {
    this.caseFolderPath = caseFolderPath;
  }

  public String getToBeShuffledFrequencyFilePath() {
    return toBeShuffledFrequencyFilePath;
  }

  public void setToBeShuffledFrequencyFilePath(String toBeShuffledFrequencyFilePath) {
    this.toBeShuffledFrequencyFilePath = toBeShuffledFrequencyFilePath;
  }

  public String getInheritanceModel() {
    return inheritanceModel;
  }

  public void setInheritanceModel(String inheritanceModel) {
    this.inheritanceModel = inheritanceModel;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

  enum InputEnum {
    CASE_IN,      //input option "-casein"
    FREQUENCY,   //input option "-frequency"
    INHERITANCE, //input option "-inheritance"
    OUT,         //input option "-out"
  }


  class PvalueComparator implements Comparator<Map.Entry<String, String>> {

    @Override
    public int compare(Map.Entry<String, String> entry1, Map.Entry<String, String> entry2) {
      Double pvalue1 = Double.valueOf(entry1.getValue());
      Double pvalue2 = Double.valueOf(entry2.getValue());
      return pvalue1.compareTo(pvalue2);
    }
  }
}
