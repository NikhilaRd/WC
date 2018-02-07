package edu.smu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.smu.stemmer.Stemmer;

public class SpiderLeg {
	// We'll use a fake USER_AGENT so the web server thinks the robot is a normal web browser.
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1";
    private List<String> links = new LinkedList<String>();
    public static int maxPagesToCrawl = 10;
    public static String rootUrl = "";
    private int countOfLinks = 0;
    private Map<String,Integer> visited = new HashMap<String,Integer>();
    private Set<String> brokenUrl = new HashSet<String>();
    private Set<String> nonHtml = new HashSet<String>();
    private Set<String> pdfFiles = new HashSet<String>();
    private Set<String> graphicFiles = new HashSet<String>();
    private Map<String,String> outGoingLinksMap = new HashMap<String,String>();
    private Set<String> disAllowed = new HashSet<String>();
    //private List<String> pagesToVisit = new LinkedList<String>();
    //private Set<String> words = new HashSet<String>();
    Map <Integer,Vector<String>> wordDocuments = new HashMap<Integer,Vector<String>>();
    Map <String, Integer> wordMap = new HashMap<String, Integer>();

    /**
     * This performs all the work. It makes an HTTP request, checks the response, and then gathers
     * up all the links on the page. Perform a searchForWord after the successful crawl
     * 
     * @param url
     *            - The URL to visit
     * @return whether or not the crawl was successful
     */
    public boolean crawl(String url)
    {
    	if(!isValidLink(url)){
    		System.out.println("Access denied for crawling this URL");
    		return false;
    	}
    	
        try
        {
            Connection connection = Jsoup.connect(url).userAgent(USER_AGENT);
            //connection.ignoreContentType(true);
            if(url.trim().endsWith("pdf")){
            	pdfFiles.add(url);
            	return false;
        	}else if(url.trim().endsWith("jpg")|| url.trim().endsWith("jpg")){
            	graphicFiles.add(url);
            	return false;
        	}
            if(url.trim().toLowerCase().endsWith("xls")|| url.trim().toLowerCase().endsWith("xlsx")){
        		return false;
        	}
            
            Document htmlDocument = connection.get();
            
            if(!connection.response().contentType().contains("text/html") && !connection.response().contentType().contains("text/plain"))
            {
            	nonHtml.add(url);
                System.out.println("**Failure** Retrieved something other than HTML "+url);
                return false;
            }

            if(!url.trim().toLowerCase().startsWith(rootUrl)){
            	String title = htmlDocument.title();
        		outGoingLinksMap.put(url, title);
        		return false;
        	}
            
            String htmlText = htmlDocument.body().text();
            if(visited.containsKey(url)){
            	int count = visited.get(url);
            	visited.put(url, ++count);
            	return false;
            } else {
            	visited.put(url, 1);
            }
            Vector<String> v = new Vector<String>();
            Vector<String> resultVector = new Vector<String>();
            v.addAll(Arrays.asList(htmlText.split(" ")));
            resultVector.addAll(new Stemmer().Stem(v));
            for(String stemWord : resultVector){
	            if(wordMap.containsKey(stemWord)){
	            	int count = wordMap.get(stemWord);
	            	wordMap.put(stemWord, ++count);
	            } else {
	            	wordMap.put(stemWord, 1);
	            }
            }
            resultVector.add(0, url);
            wordDocuments.put(countOfLinks,resultVector);
            
            
            if(countOfLinks>=maxPagesToCrawl){
            	return true;
            }
            countOfLinks++;
            Elements linksOnPage = htmlDocument.select("a[href]");
            //System.out.println("Found (" + linksOnPage.size() + ") links");
            for(Element link : linksOnPage)
            {
                //this.links.add(link.absUrl("href"));
                crawl(link.absUrl("href"));
            }
            return true;
        }catch(HttpStatusException e){
        	brokenUrl.add(url);
        	System.out.println("Invalid URL "+url);
        	return false;
        }
        catch(IOException ioe){
            // We were not successful in our HTTP request
            return false;
        } catch(IllegalArgumentException iae){
        	System.out.println(url);
        	iae.printStackTrace();
        	return false;
        }
    }
    
    public void parseRobots(String url){
			try{
				BufferedReader in = new BufferedReader(	
		            new InputStreamReader(new URL(url+"/robots.txt").openStream()));
		        String line = null;
		        while((line = in.readLine()) != null) {
		            if(!line.trim().startsWith("#")){
		            	if(line.trim().startsWith("Disallow")){
		            		disAllowed.add(url+line.trim().substring(line.trim().indexOf(':')+1).trim());
		            	}
		            }
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
    }
    public boolean isValidLink(String url){
    	for(String durl:disAllowed){
    		if(durl.equalsIgnoreCase(url)){
    			return false;
    		}
    	}
    	return true;
    }

    public List<String> getLinks()
    {
        return this.links;
    }

	public int getCountOfLinks() {
		return countOfLinks;
	}

	public Map<String, Integer> getVisited() {
		return visited;
	}

	public Set<String> getBrokenUrl() {
		return brokenUrl;
	}

	public Set<String> getNonHtml() {
		return nonHtml;
	}
	
	public Map<Integer, Vector<String>> getWordDocuments(){
		return wordDocuments;
	}

	public Set<String> getGraphicFiles() {
		return graphicFiles;
	}

	public Map<String,String> getOutGoingLinksMap() {
		return outGoingLinksMap;
	}
	
	public Map<String,Integer> getWordMap() {
		return wordMap;
	}    

}
