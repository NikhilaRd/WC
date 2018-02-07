package edu.smu.stemmer;

import net.didion.jwnl.*;
import net.didion.jwnl.data.*;
import net.didion.jwnl.dictionary.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;

public class Stemmer {
	private int MaxWordLength = 50;
	private Dictionary dic;
	private MorphologicalProcessor morph;
	private boolean IsInitialized = false;  
	public HashMap AllWords = null;
	
	/**
	 * establishes connection to the WordNet database
	 */
	public Stemmer ()
	{
		AllWords = new HashMap ();
		
		try
		{
			JWNL.initialize(new FileInputStream
				("/home/nikhila/workspace/WC/src/edu/smu/stemmer/JWNLproperties.xml"));
			dic = Dictionary.getInstance();
			morph = dic.getMorphologicalProcessor();
			// ((AbstractCachingDictionary)dic).
			//	setCacheCapacity (10000);
			IsInitialized = true;
		}
		catch ( FileNotFoundException e )
		{
			System.out.println ( "Error initializing Stemmer: JWNLproperties.xml not found" );
		}
		catch ( JWNLException e )
		{
			System.out.println ( "Error initializing Stemmer: " 
				+ e.toString() );
			e.printStackTrace();
		} 
		
	}
	public void Unload ()
	{ 
		dic.close();
		Dictionary.uninstall();
		JWNL.shutdown();
	}
	
	/* stems a word with wordnet
	 * @param word word to stem
	 * @return the stemmed word or null if it was not found in WordNet
	 */
	public String StemWordWithWordNet ( String word )
	{
		if ( !IsInitialized )
			return word;
		if ( word == null ) return null;
		if ( morph == null ) morph = dic.getMorphologicalProcessor();
		
		IndexWord w;
		try
		{
			w = morph.lookupBaseForm( POS.VERB, word );
			if ( w != null )
				return w.getLemma().toString ();
			w = morph.lookupBaseForm( POS.NOUN, word );
			if ( w != null )
				return w.getLemma().toString();
			w = morph.lookupBaseForm( POS.ADJECTIVE, word );
			if ( w != null )
				return w.getLemma().toString();
			w = morph.lookupBaseForm( POS.ADVERB, word );
			if ( w != null )
				return w.getLemma().toString();
		} 
		catch ( JWNLException e )
		{
		}
		return null;
	}
	
	/**
	 * Stem a single word
	 * tries to look up the word in the AllWords HashMap
	 * If the word is not found it is stemmed with WordNet
	 * and put into AllWords
	 * 
	 * @param word word to be stemmed
	 * @return stemmed word
	 */
	public String Stem( String word )
	{
		// check if we already know the word
		String stemmedword = (String)AllWords.get( word );
		if ( stemmedword != null )
			return stemmedword; // return it if we already know it
		
		// don't check words with digits in them
		if ( word.matches(".*\\d+.*") )
			stemmedword = null;
		else	// unknown word: try to stem it
			stemmedword = StemWordWithWordNet (word);
		
		if ( stemmedword != null )
		{
			// word was recognized and stemmed with wordnet:
			// add it to hashmap and return the stemmed word
			AllWords.put( word, stemmedword );
			return stemmedword;
		}
		// word could not be stemmed by wordnet, 
		// thus it is no correct english word
		// just add it to the list of known words so 
		// we won't have to look it up again
		AllWords.put( word, word );
		return word;
	}
	
	/**
	 * performs Stem on each element in the given Vector
	 * 
	 */
	public Vector<String> Stem ( Vector<String> words )
	{
		if ( !IsInitialized )
			return words;
		Vector<String> newWords	= new Vector<String>();
		List<String> stopWordsList =  Arrays.asList(StopWords.stopWords);
		for ( int i = 0; i < words.size(); i++ )
		{
			String s = words.get(i).trim().toLowerCase();
			while(s.startsWith("(") || s.startsWith("{")|| s.startsWith("<")){
				s=s.substring(1);
			}
			while(s.endsWith(")") || s.endsWith(".") || s.endsWith(",") || s.endsWith(";")|| s.endsWith("}")|| s.endsWith(">")){
				s=s.substring(0,s.length()-1);
			}
			if(s.trim().length() == 0 || stopWordsList.contains(s.trim())|| !s.matches("[a-zA-Z]*")){
				continue;
			}
			
			newWords.add(Stem(s));
		}
		return newWords;		
	}	
}
