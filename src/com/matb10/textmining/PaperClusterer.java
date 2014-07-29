package com.matb10.textmining;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.benchmark.byTask.utils.FileUtils;

import com.aliasi.cluster.Clusterer;
import com.aliasi.cluster.KMeansClusterer;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.PorterStemmerTokenizerFactory;
import com.aliasi.tokenizer.RegExTokenizerFactory;
import com.aliasi.tokenizer.TokenFeatureExtractor;
import com.aliasi.util.Files;

public class PaperClusterer {
	
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException {
		PaperClusterer clusterer = new PaperClusterer("/Users/Gustavo/Google Drive/UFBA/Tópicos em BD/Text Mining/Artigos 2/sbsi/", 
				7, 1000, true, 0.0001d);
		Set<Set<CharSequence>> clusters = clusterer.clusterAndSave();
		System.out.println("Clusters = " + clusters.size() + " Total documents: " + clusterer.getDataSetSize());
	}

	private Set<CharSequence> mDocuments = new HashSet<CharSequence>();
	private Clusterer<CharSequence> mClusterer;
	private Map<CharSequence, File> mFiles = new HashMap<CharSequence, File>();
	private File mFolder;

	public PaperClusterer(String folder, int k, int maxEpochs, boolean kMeansPlusPlus, double minImprovement) throws FileNotFoundException {
		mFolder = new File(folder);
		if(!mFolder.isDirectory())
			throw new FileNotFoundException("The path " + folder + " is not a directory.");
		loadDocuments(mFolder);
		
		mClusterer = new KMeansClusterer<>(
				new TokenFeatureExtractor(new PorterStemmerTokenizerFactory(new LowerCaseTokenizerFactory(new RegExTokenizerFactory("[a-zA-Z]+|[0-9]+"), Locale.US))), 
				k, maxEpochs, kMeansPlusPlus, minImprovement);
	}
	
	public Set<Set<CharSequence>> cluster(){
		return mClusterer.cluster(mDocuments);
	}
	
	public Set<Set<CharSequence>> clusterAndSave(){
		Set<Set<CharSequence>> cluster = mClusterer.cluster(mDocuments);
		File folder = new File(mFolder.getAbsolutePath() + "_clustered");
		try {
			FileUtils.fullyDelete(folder);
		} catch (IOException e) {
			System.err.println("Could not delete folder " + folder.getAbsolutePath());
			e.printStackTrace();
		}
		folder.mkdir();
		int n = 0;
		for(Set<CharSequence> set : cluster){
			File clusterFolder = new File(folder.getAbsolutePath() + "/cluster" + n++);
			clusterFolder.mkdir();
			for(CharSequence doc : set){
				File sourceFile = mFiles.get(doc);
				File destFile = new File(clusterFolder.getAbsoluteFile() + sourceFile.getAbsolutePath().substring(mFolder.getAbsolutePath().length()));
				destFile.getParentFile().mkdirs();
				try {
					Files.copyFile(sourceFile, destFile);
				} catch (IOException e) {
					System.err.println("Could not copy file " + sourceFile.getAbsolutePath() + " to " + destFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
		}
		return cluster;
	} 
	
	public int getDataSetSize(){
		return mDocuments.size();
	}

	private void loadDocuments(File folder) throws FileNotFoundException {
		for(File file : folder.listFiles())
			try{
				if(file.isDirectory())
					loadDocuments(file);
				else if(file.getName().endsWith(".txt"))
					loadText(file);
			} catch (Exception e) {
				System.err.println("Exception on file " + file.getAbsolutePath());
				e.printStackTrace();
			}
	}

	private void loadText(File file) throws FileNotFoundException {
		String doc = readFile(file);
		mDocuments.add(doc);
		mFiles.put(doc, file);
	}


	public static String readFile(File file) throws FileNotFoundException 	{
		Scanner scanner = new Scanner(file, "UTF-8");
		try{
			return scanner.useDelimiter("\\A").next();
		} finally {
			scanner.close();
		}
	}
}
