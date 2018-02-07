package edu.smu;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class Main {

	public static void main(String[] args) {
		String url = args[0];//http://lyle.smu.edu/~fmoore
		SpiderLeg.rootUrl = url;
		SpiderLeg.maxPagesToCrawl = Integer.parseInt(args[1]);
		int max = 0;
		SpiderLeg spiderLeg = new SpiderLeg();
		spiderLeg.parseRobots(url);
		spiderLeg.crawl(url);

		System.out.print("All Visited Links in "+url+": ");
		for(Entry<String, Integer> entry : spiderLeg.getVisited().entrySet()){
			System.out.print(entry.getKey()+" ");
		}
		System.out.println();
		System.out.println("Outgoing Links with the title: "+spiderLeg.getOutGoingLinksMap());
		
		System.out.print("Duplicate Links: ");
		for(Entry<String, Integer> entry : spiderLeg.getVisited().entrySet()){
			if(entry.getValue()>1){
				System.out.print(entry.getKey()+" ");
			}
		}
		System.out.println();
		System.out.println("Broken links: "+spiderLeg.getBrokenUrl());
		System.out.println("Graphic Files: "+spiderLeg.getGraphicFiles());
		System.out.println("Number of Graphic files  are "+spiderLeg.getGraphicFiles().size());
		
		//System.out.println("wordMap: "+spiderLeg.getWordMap());
		System.out.println("words: "+spiderLeg.getWordDocuments());
		for(Entry<String, Integer> entry : spiderLeg.getWordMap().entrySet()){
			if(max<entry.getValue()){
				max=entry.getValue(); 
			}
		}
		int count = 0;
		Set<String> words = new HashSet<String>();
		while(max>0 && count<20){
			for(Entry<String, Integer> entry : spiderLeg.getWordMap().entrySet()){
				if(entry.getValue()==max){
					words.add(entry.getKey());
					if(++count>20){
						break;
					}
				}
			}
			max--;
		}
		System.out.println("Frequent words: "+words);
		Map<String,Map<Integer,Integer>> wordFreq = new HashMap<String, Map<Integer,Integer>>();
		for(String word: words){
			Map <Integer,Integer> tempMap = new HashMap<Integer, Integer>();
			for(Entry<Integer, Vector<String>> entry : spiderLeg.getWordDocuments().entrySet()){
				int freq = 0;
				for(String str:entry.getValue()){
					if(str.equals(word)){
						freq++;
					}
				}
				tempMap.put(entry.getKey(), freq);
			}
			wordFreq.put(word, tempMap);
		}
		System.out.println("Term :" +"DocId: "+ "Document Frequency");
		System.out.println(wordFreq);
    }
}
