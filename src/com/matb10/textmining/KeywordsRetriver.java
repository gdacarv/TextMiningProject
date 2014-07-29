package com.matb10.textmining;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class KeywordsRetriver {
	
	public static void main(String[] args) throws NumberFormatException, FileNotFoundException {
		KeywordsRetriver keywordsRetriver = new KeywordsRetriver("/Users/Gustavo/UFBA/TextMining/Artigos/sbie_clustered/cluster3");
		System.out.println("Keywords = " + keywordsRetriver.getKeywords(40));
	}

	private TfIdf mTfIdf;
	
	public KeywordsRetriver(String folder){
		mTfIdf = new TfIdf(folder);
		mTfIdf.buildAllDocuments();
	}
	
	public List<String> getKeywords(int num){
		Map<String, Number> mMap = new HashMap<String, Number>();
		for(String doc : mTfIdf.documentNames()){
			String[] kws = mTfIdf.bestWords(doc);
			for(String keyword : kws){
				if(mMap.containsKey(keyword))
					mMap.put(keyword, mMap.get(keyword).intValue() + 1);
				else
					mMap.put(keyword, 0);
			}
		}
		
		ArrayList<Entry<String, Number>> list = new ArrayList<Entry<String, Number>>(mMap.entrySet());
		Collections.sort(list, comparator);
		//for(int i = num, size = list.size(); i < size;size--) list.remove(i);
		ArrayList<String> keywords = new ArrayList<String>(num);
		for(int i = 0; keywords.size() <= num && i < list.size(); i++)
			if(!list.get(i).getKey().trim().matches("-?\\d+")) // if it is not a number
				keywords.add(list.get(i).getKey());
				
		
		TokenizeAndStopWords tsw = new TokenizeAndStopWords();
		String concatStringsWSep = concatStringsWSep(keywords, " ");
		try {
			return tsw.tokenizeAndRemoveStopWords(concatStringsWSep);
		} catch (IOException e) {
			e.printStackTrace();
			return keywords;
		}
	}
	

	private static final Comparator<Entry<String, Number>> comparator = new Comparator<Map.Entry<String,Number>>() {
		
		@Override
		public int compare(Entry<String, Number> o1, Entry<String, Number> o2) {
			return Integer.compare(o2.getValue().intValue(), o1.getValue().intValue());
		}
	};
	
	public static String concatStringsWSep(List<String> strings, String separator) {
	    StringBuilder sb = new StringBuilder();
	    String sep = "";
	    for(String s: strings) {
	        sb.append(sep).append(s);
	        sep = separator;
	    }
	    return sb.toString();                           
	}
}
