package com.matb10.textmining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.ufba.matb10.stemmer.Stemmer;

import com.aliasi.util.Files;

public class PaperClassifier {
	
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException {
		try {
			PaperClassifier classifier = new PaperClassifier("/Users/Gustavo/UFBA/TextMining/Artigos/sbsi_manually_classified");
			classifier.classifyFolder("/Users/Gustavo/UFBA/TextMining/Artigos/sbsi");
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Classifier<String, String> mClassifier;
	private List<String> mClasses;
	
	private Stemmer mStemmer;

	public PaperClassifier(String folderTraining) throws IOException {
		mStemmer = Stemmer.StemmerFactory();
		mStemmer.enableCaching(1000);
		
		mClassifier = new BayesClassifier<String, String>();
		mClassifier.setMemoryCapacity(100000);
		File folder = new File(folderTraining);
		if(!folder.isDirectory())
			throw new FileNotFoundException("The path " + folderTraining + " is not a directory.");
		mClasses = new ArrayList<String>();
		for(File file : folder.listFiles())
			if(file.isDirectory()){
				String cls = file.getName();
				mClasses.add(cls);
				for(File doc : file.listFiles())
					if(!doc.getName().startsWith(".")) {
						List<String> preprocess = preprocess(doc);
						mClassifier.learn(cls, preprocess);
					}
			}
	}
	
	public void classifyFolder(String folderName) throws IOException{
		classifyFolder(folderName, null);
	}
	
	private void classifyFolder(String folderName, String saveFolder) throws IOException{
		File folder = new File(folderName);
		if(!folder.isDirectory())
			throw new FileNotFoundException("The path " + folderName + " is not a directory.");
		File folderClassified = new File(saveFolder == null ? folder.getAbsolutePath() + "_classified" : saveFolder);
		folderClassified.mkdirs();

		for(File file : folder.listFiles()){
			if(file.isDirectory()){
				classifyFolder(file.getAbsolutePath(), folderClassified.getAbsolutePath());
				continue;
			}
			if(file.getName().startsWith("."))
				continue;
			String cls = classify(file);
			System.out.println("File " + file.getAbsolutePath() + " classified on " + cls);
			File savedFile = new File(folderClassified.getAbsolutePath() + '/' + cls + file.getAbsolutePath().substring(folderClassified.getAbsolutePath().length() - "_classified".length()));
			savedFile.getParentFile().mkdirs();
			try {
				Files.copyFile(file, savedFile);
			} catch (IOException e) {
				System.err.println("Could not copy file " + file.getAbsolutePath() + " to " + savedFile.getAbsolutePath());
				e.printStackTrace();
			}
		}
	}
	
	public String classify(String file) throws IOException{
		return mClassifier.classify(preprocess(file)).getCategory();
	}
	
	public String classify(File file) throws IOException{
		return mClassifier.classify(preprocess(file)).getCategory();
	}

	public List<String> preprocess(String file) throws IOException {
		return applyStemmer(TokenizeAndStopWords.tokenizeAndRemoveStopWords(file));
	}

	public List<String> preprocess(File file) throws IOException {
		return applyStemmer(TokenizeAndStopWords.tokenizeAndRemoveStopWords(file));
	}

	private List<String> applyStemmer(List<String> tokens) {
		for(int i = 0, size = tokens.size(); i < size; i++)
			tokens.set(i, mStemmer.getWordStem(tokens.get(i)));
		return tokens;
	}
}
