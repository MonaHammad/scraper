package com.ontometrics.scraper.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.net.URL;

import net.htmlparser.jericho.HTMLElementName;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ontometrics.scraper.TestUtil;

public class ScraperUtilTest {

	private static final Logger log = LoggerFactory.getLogger(ScraperUtilTest.class);
	
	private String testPageSource = "/testpages/ids-page-2.html";
	
	private URL testPageSourceURL;
	
	@Before
	public void setup(){
		testPageSourceURL = TestUtil.getFileAsURL(testPageSource);
	}

	@Test
	public void extractOccurrenceOfTag() {

		String test = "<table>1</table><table>2</table><table>3</table><table>4</table><table>5</table>";

		String table1 = ScraperUtil.extract(test, HTMLElementName.TABLE, 0);
		String table3 = ScraperUtil.extract(test, HTMLElementName.TABLE, 2);

		assertThat(table1, is(equalTo("<table>1</table>")));
		assertThat(table3, is(equalTo("<table>3</table>")));

	}
	
	@Test
	public void extractParameterFromGet(){
		String testUrl = "http://www.grants.gov/search/category.do;jsessionid=yy2pTSxJ2x10GQg11sJhCqBKM6dhnGdry0gXP9YMz73Kqm8nT11x!-25395513";
		
		String sessionID = ScraperUtil.extractParameter(testUrl, "jsessionid");
		
		log.info("session ID: {}", sessionID);
		
		assertThat(sessionID, is("yy2pTSxJ2x10GQg11sJhCqBKM6dhnGdry0gXP9YMz73Kqm8nT11x!-25395513"));
	}
	
	@Test
	public void extractSessionIDFromLink() throws IOException{
		
		String sessionID = ScraperUtil.extractSessionId(testPageSourceURL, "jsessionid");
		log.info("sessionID: {}", sessionID);
		
		assertThat(sessionID, is("np3QTP2BZtspJv5Y38dyMnWZRgC6NV1Zn6lHSKJq1rQQ3tkLTyyZ!336881473"));
		
	}

}