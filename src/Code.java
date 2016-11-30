
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.mysql.jdbc.ResultSet;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;

 class news
{

	
	static  String driver = "com.mysql.jdbc.Driver";
	static  String url = "jdbc:mysql://localhost:3306/STUDENTS";
	static  String username = "om";
	static  String password = "omtakalkar";
	static	Document doc = null;
	static  String text=null;
	static  StanfordCoreNLP pipeline;
	static  String sentiment = null ;
	
 void DisplayNews() throws IOException 
	{
		 String[] siteURL = {"http://arstechnica.com/", "https://www.cnet.com/news/","http://www.digitaltrends.com/computing/"} ;
		 int size = siteURL.length;
		 for (int i=0; i<=size  ; i++)			  
			  {
				    doc = Jsoup.connect(siteURL[i]).get();
				  	Elements elements = null;
					elements=doc.select("p");
					text=elements.text();
				   // System.out.println("\t" +text +"\n");
					DisplaySentimentWithNews();
			 }	    
	}
	
	
static	 void DisplaySentimentWithNews() throws IOException
	{
		
		 System.out.println(text);
		 Properties props = new Properties();
		 props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
		 StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		 Annotation document = new Annotation(text);
		 pipeline.annotate(document);
        
		 List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		 for (CoreMap sentence : sentences) 
	        {
	           sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
	            System.out.println(sentiment + "\t" + sentence);
	        }
		 CountingWordsInNews();
	}
	
	static  void CountingWordsInNews()
	{
		//String a1 = text;
		String[] splitted = text.split(" ");
        Map<String, Integer> hm = new HashMap<String, Integer>();
        for (int i1=0; i1<splitted.length ; i1++)
        {
            if (hm.containsKey(splitted[i1])) 
	            {
	               int cont = hm.get(splitted[i1]);
	               hm.put(splitted[i1], cont + 1);
	            } 
            else 
	            {
	               hm.put(splitted[i1], 1);
	            }
        }
        
         System.out.println(hm+"\n");
         DisplayNounVerbs();
	}
	
	
	
	static void DisplayNounVerbs()
	{
		MaxentTagger tagger = new  MaxentTagger ("taggers/english-left3words-distsim.tagger");
	    // String abc = text;
	    String tagged = tagger.tagString(text);
	    String taggedString = tagger.tagTokenizedString(tagged);
	    System.out.println(taggedString+"\n");
	    InsertIntoDatabase();
	}
	
	
	static  void InsertIntoDatabase()
	{
		Connection con = null;
 		Statement stmt = null ;
 		java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
 		 try {
			 			 
	 		      con = DriverManager.getConnection(url, username, password);
	 		      Class.forName(driver);	
	 		      
	 		      stmt = con.   createStatement();
	 		      PreparedStatement pstmt = con.prepareStatement("INSERT IGNORE newsFeed(id,news) VALUES (?,?)");
	 		      pstmt.setInt(1, 1);
			      pstmt.setString(2, text);
			      pstmt.executeUpdate();
			     
			      
			      pstmt =con.prepareStatement("INSERT IGNORE sentiment(sentiments,register_date) VALUES (?,?)");
			      pstmt.setTimestamp(2, date);
			     
						      pstmt.setString(1, sentiment);
						     
						      
					    	  pstmt.executeUpdate();
					      
			      ResultSet rs;
			      rs = (ResultSet) stmt.executeQuery("SELECT * from sentiment");
		            while ( rs.next() )
			            {
			                String arstechnicanews = rs.getString("sentiments");
			                System.out.println(arstechnicanews);
			            }
		            CountingSentiments();
	  
 		    } 
	 		 catch (ClassNotFoundException e) 
		 		 {
		 		      System.out.println(" failed to load MySQL driver.");
		 		      e.printStackTrace();
		 		 } 
	 		 catch (SQLException e) 
		 		 {
		 		      System.out.println("error: failed to create a connection");
		 		      e.printStackTrace();
		 	     } 
	 		 catch (Exception e)
		 		 {
		 		      System.out.println("other error:");
		 		      e.printStackTrace();
		 		 } 
	 		finally
	 		{
	 		      try
		 		      {
		 		        stmt.close();
		 		        con.close();        
		 		      }
	 		     catch (SQLException e)
		 		      {
		 		        e.printStackTrace();
		 		      }
	 		}
	   }
	
	
	static  void CountingSentiments()
	{
		
		List<String> list = new ArrayList<String>();
 		list.add(sentiment);
 		

 		Set<String> unique = new HashSet<String>(list);
 		for (String key : unique)
 		{
 			 System.out.println("counting positive negative words");
 		    System.out.println(key + ": " + Collections.frequency(list, key));
 		    
 		}
	}
	

}

public class Code
{
	

	public static void main(String[] args) throws IOException 
	{
		// TODO Auto-generated method stub
		news cde = new news();
		cde.DisplayNews();
		news.DisplayNounVerbs();
		news.DisplaySentimentWithNews();
		news.CountingWordsInNews();
		news.DisplayNounVerbs();
		news.InsertIntoDatabase();
		news.CountingSentiments();
				
			
	}
}
	
