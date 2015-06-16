/**
 * 
 */
package cn.edu.fudan.iipl.onekey;

import static cn.edu.fudan.iipl.util.FileUtil.copyFile;
import static cn.edu.fudan.iipl.util.FileUtil.dirCreate;
import static cn.edu.fudan.iipl.util.FileUtil.dirJudge;
import static cn.edu.fudan.iipl.util.FileUtil.dirsCreate;
import static cn.edu.fudan.iipl.util.FileUtil.fileJudge;
import static cn.edu.fudan.iipl.util.FileUtil.getCanonicalPath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import cn.edu.fudan.iipl.ourvaast.Identify;
import cn.edu.fudan.iipl.ourvaast.Pickup;
import cn.edu.fudan.iipl.ourvaast.Score;
import cn.edu.fudan.iipl.ourvaast.Shuffle;
import cn.edu.fudan.iipl.ourvaast.Statistic;


/**
 * @author Administrator
 *
 */
public class Onekey {

	private String inputFolderPath = null;                    // -i
	private String diseaseVariantsFilePath = null;			  // -v
	private String toBeShuffledGeneNameFilePath = null;		  // -g
	private String toBeShuffledFrequencyFilePath = null;	  // -f
	private String inheritanceModel = null;					  // -m
	private int toBeShuffledVariantsNumber = 0;				  // -n
	private String outputPath = null;   					  // -o

	public static void main(String[] args) throws InterruptedException {
		if(args.length < 14){
			usage();
			return;
		}
		
		Onekey onekey = new Onekey();
		
		/** process input, preserve input args. */
		onekey.processInput(args);
		
		String caseFolderPath = onekey.getOutputPath() + File.separator + "case";
		String controlWithScoreFolderPath = onekey.getOutputPath() + File.separator + "control_with_score";
		String controlFolderPath = onekey.getOutputPath() + File.separator + "control";
		String shuffledCaseFolderPath = onekey.getOutputPath() + File.separator + "shuffledCase";
		
		dirCreate(caseFolderPath);
		dirCreate(controlWithScoreFolderPath);
		dirCreate(controlFolderPath);
		dirCreate(shuffledCaseFolderPath);
		
		
		/** read disease names. */
		List<String> diseaseGeneNameList = new ArrayList<String>();
		BufferedReader geneNameFileReader = null;
		try{
			geneNameFileReader = new BufferedReader(new FileReader(onekey.getToBeShuffledGeneNameFilePath()));
			String tempString = null;
			while((tempString = geneNameFileReader.readLine()) != null){
				String[] geneNames = tempString.trim().split("\\s+");
				for(String geneName:geneNames){
					diseaseGeneNameList.add(geneName);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				geneNameFileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		/** read frequency list. */
		List<Double> frequencyList = new ArrayList<Double>();
		BufferedReader frequencyFileReader = null;
		try{
			frequencyFileReader = new BufferedReader(new FileReader(onekey.getToBeShuffledFrequencyFilePath()));
			String tempString = null;
			while((tempString = frequencyFileReader.readLine()) != null){
				String[] frequencies = tempString.trim().split("\\s+");
				for(String frequency:frequencies){
					frequencyList.add(Double.parseDouble(frequency));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally{
			try {
				frequencyFileReader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		Pickup pickup = new Pickup();
		pickup.pickup(onekey.getInputFolderPath(), onekey.getOutputPath());
		System.out.println("Pickup finished! Case and control are generated at " + onekey.getOutputPath() + "!");
		TimeUnit.SECONDS.sleep(1);
		
		
		
		Shuffle shuffle = new Shuffle();
		shuffle.setCaseFolderPath(caseFolderPath);
		shuffle.setDiseaseVariantsFilePath(onekey.getDiseaseVariantsFilePath());
		shuffle.setShuffledCaseOutputPath(shuffledCaseFolderPath);
		shuffle.setToBeShuffledFrequencyFilePath(onekey.getToBeShuffledFrequencyFilePath());
		shuffle.setToBeShuffledGeneNameFilePath(onekey.getToBeShuffledGeneNameFilePath());
		shuffle.setToBeShuffledVariantsNumber(onekey.getToBeShuffledVariantsNumber());
		/** shuffle. */
		shuffle.shuffleAccordingToGeneName(shuffle.getDiseaseVariantsFilePath(), diseaseGeneNameList, frequencyList, shuffle.getToBeShuffledVariantsNumber());
		/** copy. */
		shuffle.copyCaseFilesToShuffledCaseFolder(shuffle.getCaseFolderPath(), frequencyList);
		/** finished shuffling. */
		System.out.println("Shuffle finished! ShuffledCase are generated at " + shuffle.getShuffledCaseOutputPath() + "!");
		TimeUnit.SECONDS.sleep(1);
		
		
		Score score = new Score();
		score.setCaseFolderPath(shuffledCaseFolderPath);
		score.setControlFolderPath(controlWithScoreFolderPath);
		score.setInheritanceModel(onekey.getInheritanceModel());
		score.setToBeShuffledFrequencyFilePath(onekey.getToBeShuffledFrequencyFilePath());
		score.setCaseOutputPath(shuffledCaseFolderPath);
		score.setControlOutputPath(controlFolderPath);
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
		/** get average score of each variant. */
		/** case. */
		for(Double frequency : frequencyList)
			score.getAverageScoreForVariants(score.getCaseFolderPath() + File.separator + getPercentFormat(frequency, 2, 0), "case", caseVariantAvgScoreOutputPath);
		/** control. */
		score.getAverageScoreForVariants(score.getControlFolderPath(), "control", controlVariantAvgScoreOutputPath);
		System.out.println("Getting average score for variants finished!");
		System.out.println("Now we will begin to score each gene in 5 seconds!");
		for(int i = 5 ; i >= 1 ;){
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
			score.scoreEachGene(caseVariantAvgScoreOutputPath + File.separator + getPercentFormat(frequencyList.get(j), 2, 0), "case", score.getInheritanceModel(), caseGeneScoreOutputPath, 5);
		}
		// control
		score.scoreEachGene(controlVariantAvgScoreOutputPath, "control", score.getInheritanceModel(), controlGeneScoreOutputPath, 5);
		System.out.println("Score finished!");
		TimeUnit.SECONDS.sleep(1);
		
		
		Statistic statistic = new Statistic();
		statistic.setCaseFolderPath(caseGeneScoreOutputPath);
		statistic.setControlFolderPath(controlGeneScoreOutputPath);
		statistic.setInheritanceModel(onekey.getInheritanceModel());
		statistic.setToBeShuffledFrequencyFilePath(onekey.getToBeShuffledFrequencyFilePath());
		statistic.setCaseOutputPath(shuffledCaseFolderPath);
		statistic.setControlOutputPath(controlFolderPath);
		/** enter the inheritance model folder. */
		statistic.setCaseFolderPath(statistic.getCaseFolderPath() + File.separator + statistic.getInheritanceModel());
		dirJudge(statistic.getCaseFolderPath());
		statistic.setControlFolderPath(statistic.getControlFolderPath() + File.separator + statistic.getInheritanceModel());
		dirJudge(statistic.getControlFolderPath());
		/** create gene score matrix output folder path. */
		statistic.setCaseOutputPath(statistic.getCaseOutputPath() + File.separator + "geneScoreMatrix" + File.separator + statistic.getInheritanceModel());
		dirsCreate(statistic.getCaseOutputPath());
		statistic.setControlOutputPath(statistic.getControlOutputPath() + File.separator + "geneScoreMatrix" + File.separator + statistic.getInheritanceModel());
		dirsCreate(statistic.getControlOutputPath());
		/** get gene score matrix for case and control. */
		for (int j = 0; j < frequencyList.size(); j++) {
			//case
			statistic.getGeneScoreMatrixFromGeneScoreFiles(statistic.getCaseFolderPath() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0), "case", statistic.getInheritanceModel(), statistic.getCaseOutputPath());
		}
		//control
		statistic.getGeneScoreMatrixFromGeneScoreFiles(statistic.getControlFolderPath(), "control", statistic.getInheritanceModel(), statistic.getControlOutputPath());
		/** get statistic matrix of rank sum&two parts test. */
		for(int j=0;j<frequencyList.size();j++){
			String caseGeneScoreMatrixPath = getCanonicalPath(statistic.getCaseOutputPath() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0) + File.separator + "case_" + statistic.getInheritanceModel() + ".geneScoreMatrix");
			String controlGeneScoreMatrixPath = getCanonicalPath(statistic.getControlOutputPath() + File.separator + "control_" + statistic.getInheritanceModel() + ".geneScoreMatrix");
			statistic.getStatisticMatrix(caseGeneScoreMatrixPath, statistic.getInheritanceModel(), controlGeneScoreMatrixPath);
		}
		System.out.println("Statistic finished!");
		TimeUnit.SECONDS.sleep(1);
		
		
		Identify identify = new Identify();
		identify.setCaseFolderPath(shuffledCaseFolderPath + File.separator + "geneScoreMatrix");
		identify.setInheritanceModel(onekey.getInheritanceModel());
		identify.setOutputPath(onekey.getOutputPath());
		identify.setToBeShuffledFrequencyFilePath(onekey.getToBeShuffledFrequencyFilePath());
		/** enter the inheritance model folder. */
		identify.setCaseFolderPath(identify.getCaseFolderPath() + File.separator + identify.getInheritanceModel());
		dirJudge(identify.getCaseFolderPath());
		for (int j = 0; j < frequencyList.size(); j++) {
			String statisticMatrixPath = identify.getCaseFolderPath() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0) + File.separator + identify.getInheritanceModel() + ".statisticMatrix";
			fileJudge(statisticMatrixPath);
			identify.generateRScriptWithStatisticMatrix(statisticMatrixPath);
			identify.runRscrpt(statisticMatrixPath + ".r", getPercentFormat(frequencyList.get(j), 2, 0));

			/** copy result file to output folder. */
			String src = new File(statisticMatrixPath).getParent() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0) + "_" + identify.getInheritanceModel() + ".txt";
			String obj = identify.getOutputPath() + File.separator + getPercentFormat(frequencyList.get(j), 2, 0) + "_" + identify.getInheritanceModel() + ".txt";
			copyFile(src, obj);
			System.out.println("Result file output at " + obj + "!");
		}
	}
	
	public void processInput(String[] args){
		for(int i = 0; i < 14; i++){
			if(i % 2 == 0){
				switch (args[i].charAt(1)) {
					case 'i':
						dirJudge(args[++i]);
						setInputFolderPath(getCanonicalPath(args[i]));
						break;
					case 'v':
						fileJudge(args[++i]);
						setDiseaseVariantsFilePath(getCanonicalPath(args[i]));
						break;
					case 'f':
						fileJudge(args[++i]);
						setToBeShuffledFrequencyFilePath(getCanonicalPath(args[i]));
						break;
					case 'm':
						setInheritanceModel(args[++i]);
						if(!getInheritanceModel().matches("(.*recessive.*|.*dominant.*)")){
							System.err.println("-inheritance parameter error! Please input 'recessive_model' or 'dominant_model'!");
							System.exit(1);
						}
						break;
					case 'g':
						fileJudge(args[++i]);
						setToBeShuffledGeneNameFilePath(getCanonicalPath(args[i]));
						break;
					case 'n':
						setToBeShuffledVariantsNumber(Integer.parseInt(args[++i]));
						break;
					case 'o':
						dirCreate(args[++i]);
						setOutputPath(getCanonicalPath(args[i]));
						break;
				}
			}
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
	
	public String getInputFolderPath() {
		return inputFolderPath;
	}

	public void setInputFolderPath(String inputFolderPath) {
		this.inputFolderPath = inputFolderPath;
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

	public void setToBeShuffledGeneNameFilePath(String toBeShuffledGeneNameFilePath) {
		this.toBeShuffledGeneNameFilePath = toBeShuffledGeneNameFilePath;
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

	public String getOutputPath() {
		return outputPath;
	}

	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public int getToBeShuffledVariantsNumber() {
		return toBeShuffledVariantsNumber;
	}

	public void setToBeShuffledVariantsNumber(int toBeShuffledVariantsNumber) {
		this.toBeShuffledVariantsNumber = toBeShuffledVariantsNumber;
	}

	
	public static void usage(){
		String usage = "\n\t";
		usage += "This is a Onekey modual. You can run ourvaast work in a single command. "
				 	+ "You should put all your samples into a folder, then use this folder "
				 	+ "as input path. This program will randomly select half number of all "
				 	+ "samples as case, then repeat it randomly for control. You should give"
				 	+ "a \"frequency\" file whose content is frequencies to be shuffled, such"
				 	+ "as \"0.02 0.03 0.04 ...\", as well as a pathogenic variants file scored"
				 	+ "by dbNSFP. Then you want to shuffle several variants of certain genes "
				 	+ "into case, so a file contains a disease gene list is needed. You must "
				 	+ "specify the inheritance model, such as \"recessive_model\" or \"dominant_model\".";
		System.out.println(usage);
	}

}
