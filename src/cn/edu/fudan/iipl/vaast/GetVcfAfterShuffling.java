/**
 * @Copyright cn.edu.fudan.iipl
 */

package cn.edu.fudan.iipl.vaast;

import static cn.edu.fudan.iipl.util.FileUtil.dirCreate;
import static cn.edu.fudan.iipl.util.FileUtil.dirJudge;
import static cn.edu.fudan.iipl.util.FileUtil.fileCreate;
import static cn.edu.fudan.iipl.util.FileUtil.fileJudge;
import static cn.edu.fudan.iipl.util.FileUtil.getCanonicalPath;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Get vcf format of samples after shuffling.
 *
 * @author Yong Chen
 * @since 2015-06-17
 */
public class GetVcfAfterShuffling {

    private String vcfSamplesFolderPath = null;
    private String caseFrequencyFolderPath = null;
    private String toBeShuffledGeneNameFilePath = null;
    private String outputPath = null;
    
    enum InputEnum {
        FREQUENCY, GENE, OUT, VCF
    }
    
    public static void main(String[] args) {
        if (args.length < 8) {
            usage();
            return;
        }

        GetVcfAfterShuffling getVcfAfterShuffling = new GetVcfAfterShuffling();

        /** process input, preserve input args. */
        getVcfAfterShuffling.processInput(args);

        /** simplify vcf files. */
        getVcfAfterShuffling.simplifyVcf(getVcfAfterShuffling.getVcfSamplesFolderPath());


        /** read disease names. */
        List<String> diseaseGeneNameList = new ArrayList<String>();
        BufferedReader geneNameFileReader = null;
        try {
            geneNameFileReader =
                    new BufferedReader(new FileReader(
                            getVcfAfterShuffling.getToBeShuffledGeneNameFilePath()));
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

        String simplifiedVcfFolderPath =
                getVcfAfterShuffling.getVcfSamplesFolderPath() + "_simplified";

        getVcfAfterShuffling.getVcfAfterShuffled(getVcfAfterShuffling.getCaseFrequencyFolderPath(),
                simplifiedVcfFolderPath, getVcfAfterShuffling.getOutputPath() + File.separator
                        + "case", new HashSet<String>(diseaseGeneNameList));


        getVcfAfterShuffling.copyVcfToControlFolder(simplifiedVcfFolderPath,
                getVcfAfterShuffling.getOutputPath() + File.separator + "case",
                getVcfAfterShuffling.getOutputPath() + File.separator + "control");

    }
    
    public static void usage() {
        String usage = "\n\t";
        usage += "This modual was designed to get vcf format of sample files after shuffling. ";
        usage += "\n\t";
        usage +=
                "Usage: java GetVcfAfterShuffling"
                        + "\n\t"
                        + "-vcf vcfSamplesFolderPath: [required] The directory for all vcf samples. We will simplify these vcf samples first(retain columns 0-9, 14-15, discard others)."
                        + "\n\t"
                        + "-frequency caseFrequencyFolderPath: [required] The directory for case variant score folder. There must be frequency folders in this path, such as \"2%, 3%, 4%...\"."
                        + "\n\t"
                        + "-gene toBeShuffledGeneNameFilePath: [required] The path to the file whose content is a list of disease gene names."
                        + "\n\t" + "-out outputPath: [required] The output path.";
        System.out.println(usage);
    }
    
    /**
     * copy vcf files(control) to target folder, excluding those in case folder.
     * 
     * @param args
     */
    public void copyVcfToControlFolder(String vcfFilesPath, String caseVcfFilesPath,
            String targetPath) {
        try {
            dirJudge(vcfFilesPath);
            dirJudge(caseVcfFilesPath);
            dirCreate(targetPath);

            caseVcfFilesPath =
                    caseVcfFilesPath + File.separator + new File(caseVcfFilesPath).list()[0];
            dirJudge(caseVcfFilesPath);

            Set<String> vcfSet =
                    new HashSet<String>(Arrays.asList(new File(caseVcfFilesPath).list()));
            String[] grossVcfFileNames = new File(vcfFilesPath).list();
            for (String vcfFile : grossVcfFileNames) {
                if (!vcfSet.contains(vcfFile)) {
                    BufferedReader br =
                            new BufferedReader(new FileReader(vcfFilesPath + File.separatorChar
                                    + vcfFile));
                    BufferedWriter bw =
                            new BufferedWriter(new FileWriter(targetPath + File.separatorChar
                                    + vcfFile));
                    String tempString = null, writeString = "";
                    while ((tempString = br.readLine()) != null) {
                        writeString += tempString + "\n";
                    }
                    bw.write(writeString, 0, writeString.length());
                    bw.close();
                    br.close();
                    writeString = null;
                    tempString = null;
                }
            }
            System.out.println("copyVcfToControlFolder done!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * get concentration folder list in the path from parameter
     * 
     * @param shuffledFrequencyFolderPath
     * @return list of different concentrations
     */
    public List<String> getConcentrationFolderList(String shuffledFrequencyFolderPath) {
        List<String> concentrationFolderList = new ArrayList<String>();
        File geneNameFolder = new File(shuffledFrequencyFolderPath);
        Pattern pattern = Pattern.compile("[0-9]+%$", Pattern.CASE_INSENSITIVE);
        String[] folderNameArray = geneNameFolder.list();
        Matcher matcher;
        for (String concentration : folderNameArray) {
            matcher = pattern.matcher(concentration);
            while (matcher.find()) {
                concentrationFolderList.add(matcher.group());
            }
        }
        return concentrationFolderList;
    }

    /**
     * This modual will convert variants to vcf format after shuffled for case only(see
     * {@link GetVcfAfterShuffling#copyVcfToControlFolder} for control). It will fabricate the vcf
     * format of those shuffled variants extracted from positive controls.
     * 
     * @param shuffledFrequencyFolderPath the folder in which contains different frequency folders
     *        of shuffled case. e.g.
     *        "E:\experiment\retinaDiseaseGeneSimulation\retina_1gene_2variants\USH2A"
     * @param simplifiedVcfFolderPath the folder in which contains simplified vcf files. e.g.
     *        "E:\experiment\CHARGE_cohort_filterd_vcf_simplified"
     * @param outputPath in case outputPath, should contain concentration categories(if not exist,
     *        create the concentration folder). e.g.
     *        "E:\experiment\retinaDiseaseGeneSimulation\retina_1gene_2variants\USH2A\vcf\case "
     * @param geneNameSet a set of disease gene names.
     */
    public void getVcfAfterShuffled(String shuffledFrequencyFolderPath,
            String simplifiedVcfFolderPath, String outputPath, Set<String> geneNameSet) {
        Random random = new Random();
        dirJudge(shuffledFrequencyFolderPath);
        dirJudge(simplifiedVcfFolderPath);
        dirCreate(outputPath);

        List<String> frequencyList = getConcentrationFolderList(shuffledFrequencyFolderPath);
        for (String frequency : frequencyList) {
            File outputFolderFile = new File(outputPath + File.separatorChar + frequency);
            if (!outputFolderFile.exists())
                outputFolderFile.mkdir();
            File inputFolderFile =
                    new File(shuffledFrequencyFolderPath + File.separatorChar + frequency);
            String[] inputFileNames = inputFolderFile.list();
            for (String inputFileName : inputFileNames) {
                String tempString = null;
                String writeString = "";
                BufferedReader vcfFileBufferedReader = null;
                BufferedReader variantFileBufferedReader = null;
                String[] feature = null;
                BufferedWriter bw = null;
                try {
                    vcfFileBufferedReader =
                            new BufferedReader(new FileReader(simplifiedVcfFolderPath
                                    + File.separatorChar + inputFileName.split("\\.")[0]
                                    + ".filt.vcf"));
                    variantFileBufferedReader =
                            new BufferedReader(new FileReader(inputFolderFile.getAbsolutePath()
                                    + File.separatorChar + inputFileName));
                    tempString = variantFileBufferedReader.readLine();
                    writeString += vcfFileBufferedReader.readLine() + "\n";
                    tempString = variantFileBufferedReader.readLine();
                    feature = tempString.split("\\s+");
                    if (geneNameSet.contains(feature[4])) {
                        writeString +=
                                feature[0] + "\t" + feature[1] + "\t.\t" + feature[2] + "\t"
                                        + feature[3] + "\t60\tPASS\t.\tGT:VR:RR:DP:GQ\t0/1:"
                                        + random.nextInt(100) + ":" + random.nextInt(200) + ":"
                                        + random.nextInt(300) + ":.\t"
                                        + inputFileName.split("\\.")[0] + "\t" + feature[4] + "\n";
                        for (int i = 0; i < geneNameSet.size() * 2 - 1; i++) {
                            tempString = variantFileBufferedReader.readLine();
                            feature = tempString.split("\\s+");
                            writeString +=
                                    feature[0] + "\t" + feature[1] + "\t.\t" + feature[2] + "\t"
                                            + feature[3] + "\t60\tPASS\t.\tGT:VR:RR:DP:GQ\t0/1:"
                                            + random.nextInt(100) + ":" + random.nextInt(200) + ":"
                                            + random.nextInt(300) + ":.\t"
                                            + inputFileName.split("\\.")[0] + "\t" + feature[4]
                                            + "\n";
                        }
                    }
                    bw =
                            new BufferedWriter(new FileWriter(outputFolderFile.getAbsolutePath()
                                    + File.separatorChar + inputFileName.split("\\.")[0]
                                    + ".filt.vcf"));
                    while ((tempString = vcfFileBufferedReader.readLine()) != null) {
                        writeString += tempString + "\n";
                    }
                    bw.write(writeString, 0, writeString.length());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        bw.close();
                        vcfFileBufferedReader.close();
                        variantFileBufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(inputFileName.split("\\.")[0] + " done!");
            }
            System.out.println(frequency + " done!");
        }
    }

    public void processInput(String[] args) {
        for (int i = 0; i < 8; i++) {
            if (i % 2 == 0) {
                switch (InputEnum.valueOf(args[i].substring(1).toUpperCase())) {
                    case VCF:
                        dirJudge(args[++i]);
                        setVcfSamplesFolderPath(getCanonicalPath(args[i]));
                        break;
                    case FREQUENCY:
                        dirJudge(args[++i]);
                        setCaseFrequencyFolderPath(getCanonicalPath(args[i]));
                        break;
                    case GENE:
                        fileJudge(args[++i]);
                        setToBeShuffledGeneNameFilePath(getCanonicalPath(args[i]));
                        break;
                    case OUT:
                        dirCreate(args[++i]);
                        setOutputPath(getCanonicalPath(args[i]));
                        break;
                }
            }
        }
    }

    /**
     * simplify all vcf Files, retain columns range from 0~9, 14~15.
     * 
     * @param vcfFolderPath The path to folder contains all samples in vcf format.
     */
    public void simplifyVcf(String vcfFolderPath) {
        vcfFolderPath = getCanonicalPath(vcfFolderPath);
        dirJudge(vcfFolderPath);
        File vcfFile = new File(vcfFolderPath);
        String folderName = vcfFile.getName();
        String parentFolderPath = vcfFile.getParent();
        String outputFolderPath = parentFolderPath + File.separator + folderName + "_simplified";
        dirCreate(outputFolderPath);

        String[] vcfFileNames = vcfFile.list();
        BufferedReader br = null;
        StringBuilder writeString = new StringBuilder();
        String readString = null;
        for (String fileName : vcfFileNames) {
            fileCreate(outputFolderPath + File.separator + fileName);
            System.out.println("Simplifying " + fileName + " now!");
            BufferedWriter bWriter = null;
            try {
                bWriter =
                        new BufferedWriter(new FileWriter(outputFolderPath + File.separator
                                + fileName));
                br = new BufferedReader(new FileReader(vcfFolderPath + File.separator + fileName));
                while ((readString = br.readLine()) != null) {
                    String[] feature = readString.trim().split("\\s+");
                    for (int i = 0; i < feature.length; i++) {
                        if (i <= 9 || i > 13 && i < 16) {
                            writeString.append(feature[i]);
                            if (i < 15) {
                                writeString.append("\t");
                            } else {
                                writeString.append("\n");
                            }
                        }
                    }
                }
                bWriter.write(writeString.toString(), 0, writeString.length());
                writeString = new StringBuilder();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    bWriter.close();
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("simplifyVcf done!");
        System.out.println("Output at " + outputFolderPath);
    }
    

    public String getCaseFrequencyFolderPath() {
        return caseFrequencyFolderPath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getToBeShuffledGeneNameFilePath() {
        return toBeShuffledGeneNameFilePath;
    }

    public String getVcfSamplesFolderPath() {
        return vcfSamplesFolderPath;
    }

    public void setCaseFrequencyFolderPath(String caseFrequencyFolderPath) {
        this.caseFrequencyFolderPath = caseFrequencyFolderPath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setToBeShuffledGeneNameFilePath(String toBeShuffledGeneNameFilePath) {
        this.toBeShuffledGeneNameFilePath = toBeShuffledGeneNameFilePath;
    }

    public void setVcfSamplesFolderPath(String vcfSamplesFolderPath) {
        this.vcfSamplesFolderPath = vcfSamplesFolderPath;
    }
}
