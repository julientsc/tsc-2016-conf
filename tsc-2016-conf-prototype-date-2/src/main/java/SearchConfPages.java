import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.joestelmach.natty.DateGroup;
import com.joestelmach.natty.Parser;

import ch.qos.logback.classic.LoggerContext;
import tools.bing.Search;
import tools.bing.model.Result;
import tools.downloader.ConcurentDownloader;

public class SearchConfPages {
	
	private void searchDate(String sentence, int year) {
		if(sentence.length() < 6)
			return;
		
		Parser parser = new Parser();
		List<DateGroup> groups = parser.parse(sentence);
		for (DateGroup group : groups) {
			String matchingValue = group.getText();
			System.out.println(" => " + matchingValue);
			List<Date> dates = group.getDates();
			for (Date date : dates) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				if (cal.get(Calendar.YEAR) == year) {
					System.out.println(" * " + date.toLocaleString());
				}
			}
		}
	}

	public SearchConfPages(String conferenceAbbr, int year) throws MalformedURLException {
		String query = conferenceAbbr + " " + year;
		List<Result> results = Search.getAllResults(query, 5, false);

		ArrayList<URL> urls = new ArrayList<>();
		for (Result result : results) {
			urls.add(new URL(result.getUrl()));
		}
		ConcurentDownloader concurentDownloader = new ConcurentDownloader(urls, 20, false);
		HashMap<URL, String> result = concurentDownloader.doProcess();

		for (URL url : urls) {
			String content = result.get(url);
			ArrayList<String> lines = createLines(content);
			for (String line : lines) {
				if(containsNumber(line)) {
					System.out.println(line);
					searchDate(line, year);
				}
			}
			return;
		}

	}

	private boolean containsNumber(String line) {
		for(int i = 0 ; i < line.length() ; i++)
			if(line.charAt(i) >= '0' && line.charAt(i) <= '9')
				return true;
		return false;
	}

	private ArrayList<String> createLines(String content) {
		ArrayList<String> lines = new ArrayList<>();
		String startTag = "";
		boolean startTagTest = false;
		String stopTag = "";
		boolean stopTagTest = false;
		
		String line = "";
		for(int c = 0 ; c < content.length() ; c++) {
			char charactere = content.charAt(c);
			
			line += charactere;
			
			if(!stopTagTest) {

				if(startTagTest && (charactere == '>')) {
					startTagTest = false;
				}
				
				if(startTagTest) {
					startTag += charactere;
				}
				
				if(charactere == '<' && content.charAt(c + 1 ) != '/') {
					startTagTest = true;
					startTag = "";
					line = "" + charactere;
				}
			}
			
			if(!startTagTest) {
				
				if(stopTagTest && (charactere == '>')) {
					stopTagTest = false;
					String l = line.trim();
					if(l.charAt(1) != '/') {
						int close = l.indexOf('>');
						int open = l.indexOf('<', close);
						
						if(close != -1 && open != -1 && close + 1 != open) {
							l = l.substring(close + 1, open).trim();
							if(!l.equals("&nbsp;") && !l.equals("")) {
								l = l.replaceAll("\n", " ");
								String ll = "";
								for(int i = 0 ; i < l.length() ; i++) {
									if(i == 0) {
										ll += l.charAt(i);
									} else if(l.charAt(i) != ' ') {
										ll += l.charAt(i);
									} else {
										if(l.charAt(i-1) != ' ')
											ll += l.charAt(i);
									}
										
								}
								lines.add(ll);
								line = "";
							}
						}
					}
				}
				
				if(stopTagTest) {
					stopTag += charactere;
				}
				
				if(charactere == '<' && content.charAt(c + 1 ) == '/') {
					stopTagTest = true;
					stopTag = "";
				}
			}
			
			
			
			
		}
		return lines;
	}


	public static void main(final String[] args) throws Exception {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.stop();
		
		new SearchConfPages("ICDS", 2016);

		// System.out.println(new
		// Date(span.getBeginCalendar().getTimeInMillis()).toString());

		// return new DateTime(span.getBeginCalendar().getTimeInMillis(),
		// tz).getMillis() / 1000L;
	}
}
