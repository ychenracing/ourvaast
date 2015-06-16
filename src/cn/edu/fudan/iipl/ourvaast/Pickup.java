/**
 * @Copyright cn.edu.fudan.iipl
 */

package cn.edu.fudan.iipl.ourvaast;

import static cn.edu.fudan.iipl.util.FileUtil.dirJudge;
import static cn.edu.fudan.iipl.util.FileUtil.dirCreate;
import static cn.edu.fudan.iipl.util.FileUtil.getCanonicalPath;
import static cn.edu.fudan.iipl.util.FileUtil.copyFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <pre>Randomly pick up half number of total samples in input folder as case.
 * Then repeat it for control.
 * </pre>
 *
 * @author Yong Chen
 * @since 2015-01-28
 */
public class Pickup {

  private String inputFolder = null;
  private String outputFolder = null;

  public static void main(String[] args) {
    if (args.length < 4) {
      usage();
      return;
    }

    Pickup pickup = new Pickup();

    /** process input,  preserve args. */
    pickup.processInput(args);

    pickup.pickup(pickup.getInputFolder(), pickup.getOutputFolder());
  }

  /**
   * print usage message
   */
  public static void usage() {

    String usageString = "\n\t";
    usageString +=
      "This modual randomly picks up half number of total samples in input folder as case. Then repeat it for control.";
    usageString += "\n\n";
    usageString += "usage: java Pickup" + "\n\t"
      + "-in inputPath: [required] The input folder contains all samples." + "\n\t"
      + "-out outputPath: [required] The output path.";
    System.out.println(usageString);
  }

  public void processInput(String[] args) {
    for (int i = 0; i < 4; i++) {
      if (i % 2 == 0) {
        switch (InputEnum.valueOf(args[i].substring(1).toUpperCase())) {
          case IN:
            dirJudge(args[++i]);
            setInputFolder(getCanonicalPath(args[i]));
            break;
          case OUT:
            dirCreate(args[++i]);
            setOutputFolder(getCanonicalPath(args[i]));
            break;
        }
      }
    }
  }

  public void pickup(String inputFolder, String outputFolder) {
    List<String> fileNameList = new ArrayList<String>();
    fileNameList.addAll(Arrays.asList(new File(inputFolder).list()));

    File caseFolderFile = new File(outputFolder + File.separator + "case_with_score");
    File controlFolderFile = new File(outputFolder + File.separator + "control_with_score");

    dirCreate(getCanonicalPath(caseFolderFile));
    dirCreate(getCanonicalPath(controlFolderFile));

    Collections.shuffle(fileNameList);

    int halfNumber = (int) Math.floor((double) fileNameList.size() / 2);
    for (int i = 0; i < halfNumber; i++) {
      String fileName = fileNameList.get(i);

      String src = getCanonicalPath(inputFolder) + File.separator + fileName;
      String obj = getCanonicalPath(caseFolderFile) + File.separator + fileName;
      if (new File(obj).exists()) {
        System.out.println(fileName + " already exists at " + getCanonicalPath(caseFolderFile));
        System.exit(1);
      }
      copyFile(src, obj);
    }

    System.out.println("case done!");

    Collections.shuffle(fileNameList);
    for (int i = 0; i < halfNumber; i++) {
      String fileName = fileNameList.get(i);

      String src = getCanonicalPath(inputFolder) + File.separator + fileName;
      String obj = getCanonicalPath(controlFolderFile) + File.separator + fileName;
      if (new File(obj).exists()) {
        System.out.println(fileName + " already exists at " + getCanonicalPath(controlFolderFile));
        System.exit(1);
      }
      copyFile(src, obj);
    }
    System.out.println("control done!");
    System.out.println("pickup finished!");
  }

  public String getInputFolder() {
    return inputFolder;
  }

  public void setInputFolder(String inputFolder) {
    this.inputFolder = inputFolder;
  }

  public String getOutputFolder() {
    return outputFolder;
  }

  public void setOutputFolder(String outputFolder) {
    this.outputFolder = outputFolder;
  }

  enum InputEnum {
    IN,          //input option "-in"
    OUT,         //input option "-out"
  }
}
