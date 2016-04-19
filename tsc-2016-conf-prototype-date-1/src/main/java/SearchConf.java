import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.ParseLocation;
import com.joestelmach.natty.Parser;

import ch.qos.logback.classic.LoggerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import tools.bing.Search;
import tools.bing.model.Result;
import tools.downloader.ConcurentDownloader;

public class SearchConf {
	
//	public static HashMap<Long,List<Long>> getSpan(List<Long> longtime) {
//		HashMap<Long,List<Long>> spans = new HashMap<>();
//		Collections.sort(longtime);
//		
//		List<Long> list = new ArrayList<>();
//		for(Long date : longtime) {
//			if(list.size() == 0)
//				list.add(date);
//			else {
//				if(date - list.get(list.size() - 1) == 86400000
//						|| date - list.get(list.size() - 1) == 0) {
//					if(date - list.get(list.size() - 1)!=0)
//						list.add(date);
//				}
//				else {
//					spans.put(list.get(0), list);
//					list = new ArrayList<>();
//					list.add(date);
//				}
//			}
//		}
//		return spans;
//	}
	
//	public static HashMap<Long,List<Long>> getDates(String content) {
//		List<Long> dates = new ArrayList<>();
//		Parser parser = new Parser();
//		List<DateGroup> groups = parser.parse(content);
//		for (DateGroup group : groups) {
//			
//			
//			String syntaxTree = group.getSyntaxTree().toStringTree();
//			if(syntaxTree.contains("EXPLICIT_TIME") || syntaxTree.contains("RELATIVE_DATE"))
//				continue;
//			
//			List<Date> datess = group.getDates();
//			for (Date date : datess) {
//				
//				
//				SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//		        String formattedDate = formatter.format(date);
//				try {
//					Date datee = formatter.parse(formattedDate);
//					if(!dates.contains(date));
//						dates.add(datee.getTime());
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}
//		        
//		        break;
//		        
//			}
//		}
//		
//		return getSpan(dates);
//		for (Long date : dates) {
//			Date d = new Date(date);
//			System.out.println(d.toLocaleString());
//		}
//	}

	public static boolean isConferenceWebsite(URL url, int year, String content) {
		
		try {
			if (content.equals(""))
				return false;

			Document doc = Jsoup.parse(content);

			String body = doc.body().text().toLowerCase();

			String title = doc.title();
			if (title.equals(""))
				return false;

			if (!body.contains("track"))
				return false;

			if (title.contains(String.valueOf(year)))
					return true;
			if(url.toString().contains(String.valueOf(year)))
				return false;


			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static void search(String confName, int year) {
		String query = confName + " " + year;
		System.out.println("Search : " + query);

		List<URL> urlToDownload = new ArrayList<URL>();
		List<Result> results = Search.getAllResults(query, 1, false);
		for (Result result : results) {
			try {
				urlToDownload.add(new URL(result.getUrl()));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		ConcurentDownloader concurentDownloader = new ConcurentDownloader(urlToDownload, 30, false);

		HashMap<URL, String> pages = concurentDownloader.doProcess();

		System.out.println("========");

		for (URL url : pages.keySet()) {
			String content = pages.get(url);

			if (isConferenceWebsite(url, year, content)) {
				System.out.println(url);
				
				searchDates(content);

//				HashMap<Long, List<Long>> dateSpans = getDates(content);
//				for (Long span : dateSpans.keySet()) {
//					if(dateSpans.get(span).size()>2) {
//						SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
//						Date start = new Date(dateSpans.get(span).get(0));
//						System.out.println("From : " + formatter.format(start));
//						Date stop = new Date(dateSpans.get(span).get( dateSpans.get(span).size() - 1));
//						System.out.println("To : " + formatter.format(stop));
//					}
//				}
			}

		}

	}

	private static void searchDates(String content) {
		System.out.println(content);		
	}

	public static void main(String[] args) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.stop();

		search("ECCV", 2014);

	}

}
