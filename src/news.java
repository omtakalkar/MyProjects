
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	static String linkHref=null;
	
 void DisplayNews() throws Exception 
	{
		 String[] siteURL = {"http://arstechnica.com/tech-policy/2016/12/airbnb-and-nyc-bury-the-hatchet/", "https://www.cnet.com/news/","http://www.digitaltrends.com/computing/"} ;
		 int size = siteURL.length;
		 for (int i=0; i<=size  ; i++)			  
			  {
			 	doc = Jsoup.connect(siteURL[i]).get();
			  	Elements elements = null;
				elements=doc.select("p");
			//	Elements links = doc.select("a[href]");
			/*	
				for (org.jsoup.nodes.Element link : links)
				{
					
					
					System.out.printf(" * a: <%s>  (%s)", link.attr("abs:href"), trim(link.text(), 35));;
				}
			*/	
				text=elements.text();
				
				DisplaySentimentWithNews();
					
			 }	    
		 
		
	}
 

	
	
 
 private static String trim(String s, int width) 
 {
     if (s.length() > width)
         return s.substring(0, width-1) + ".";
     else
         return s;
 }
 
 
  
 
 
 

 
static	 void DisplaySentimentWithNews() throws IOException
	{
		
		 System.out.println(text);
		// System.out.println("news::"+text+", URL::"+linkHref);
		 
		 Properties props = new Properties();
		 props.setProperty("annotators", "tokenize, ssplit, pos, lemma, parse, sentiment");
		 StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		 Annotation document = new Annotation(text);
		 pipeline.annotate(document);
        
		 List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);
		 for (CoreMap sentence : sentences) 
	        {
	           sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
	            
	            //String[] result = ((Hashtable<Object, Object>) sentence).values().toArray(new String[0]);
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
 		Calendar calendar = Calendar.getInstance();
 		Timestamp currentTimestamp = new java.sql.Timestamp(calendar.getTime().getTime());
 		java.sql.Date sqlDate = new java.sql.Date(date.getTime()); 
 		 try {
 			 	  Class.forName(driver);	 
	 		      con = DriverManager.getConnection(url, username, password);
	 		     //Class.forName(driver);	
	 		      
	 		      stmt = con.   createStatement();
	 		      PreparedStatement pstmt = con.prepareStatement("INSERT IGNORE newsFeed(id,news) VALUES (?,?)");
	 		      pstmt.setInt(1, 1);
			      pstmt.setString(2, text);
			      pstmt.executeUpdate();
			     
			      
			      pstmt =con.prepareStatement("INSERT IGNORE sentiment(sentiments,register_date,Title,date,time) VALUES (?,?,?,?,?)");
			      int count = 0;
			      int[] executeResult = null;
				for (int i = 0; i < count; i++)
			      {
			      
					      pstmt.setTimestamp(5, currentTimestamp);
					      pstmt.setTimestamp(2, date);
						  pstmt.setString(1, sentiment);
						  pstmt.setDate(4, sqlDate);
						  pstmt.setString(3, sentiment);
						  pstmt.addBatch();
						  
						  executeResult = pstmt.executeBatch();
			      }      
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


