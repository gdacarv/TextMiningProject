package com.matb10.textmining;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.util.Version;

import com.snowtide.pdf.PDFTextStream;

public class TokenizeAndStopWords {

	/*public static void main(String[] args) {
		try {
			//String[] stopWords = getStopWords();
			//System.out.println(Arrays.toString(stopWords));
			//System.out.println(tokenizeAndRemoveStopWords("Olá você que quer saber sobre mineração de texto. Veio ao lugar certo!"));
			System.out.println(tokenizeAndRemoveStopWords(new File("/Users/Gustavo/Downloads/LPOO_Avaliacao2_20121.doc")));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public static List<String> tokenizeAndRemoveStopWordsPDF(File file) throws IOException {
		PDFTextStream is = new PDFTextStream(file);

		try{
			return tokenizeAndRemoveStopWords(is);
		} finally {
			is.close();
		}
	}

	public static List<String> tokenizeAndRemoveStopWords(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		try{
			return tokenizeAndRemoveStopWords(is);
		} finally {
			is.close();
		}
	}

	public static List<String> tokenizeAndRemoveStopWords(InputStream inputStream) throws IOException {
		return tokenizeAndRemoveStopWords(new InputStreamReader(inputStream));
	}

	public static List<String> tokenizeAndRemoveStopWords(String text) throws IOException {
		StringReader reader = new StringReader(text.trim());
		try {
			return tokenizeAndRemoveStopWords(reader);
		} finally {
			reader.close();
		}
	}

	public static List<String> tokenizeAndRemoveStopWords(Reader reader) throws IOException {
		CharArraySet stopWords = BrazilianAnalyzer.getDefaultStopSet();
		TokenStream tokenStream = new StandardTokenizer(Version.LUCENE_48, reader);
		List<String> words = new LinkedList<>();

		try{
			tokenStream = new StopFilter(Version.LUCENE_48, tokenStream, stopWords);
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = charTermAttribute.toString();
				words.add(term);
			}
		}finally {
			tokenStream.close();
		}
		return words;
	}

	public static String[] getStopWords(){
		String string = BrazilianAnalyzer.getDefaultStopSet().toString();
		return string.substring(1, string.length()-1).split(", ");
	}

}
