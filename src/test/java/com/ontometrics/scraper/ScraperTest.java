package com.ontometrics.scraper;

import static com.ontometrics.scraper.HtmlSample.DetailPage;
import static com.ontometrics.scraper.HtmlSample.PagedListingTable;
import static com.ontometrics.scraper.HtmlSample.ProgramDetailPage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import net.htmlparser.jericho.HTMLElementName;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScraperTest {

	private static final Logger log = LoggerFactory.getLogger(ScraperTest.class);

	private Scraper scraper;

	private String eligibilityCodeId = "dnf_class_values_cfda__applicant_eligibility__widget";

	private String eligibilityClassName = "fld_applicant_eligibility";

	@Before
	public void setup() {
		scraper = new Scraper();
	}

	@Test
	public void scrapeUrlReturnsHtml() throws IOException {
		String pageText = new Scraper().url(PagedListingTable.getUrl()).getResult();
		assertThat(pageText.length(), is(greaterThan(0)));
		assertThat(pageText.contains("<html>"), is(true));
		log.debug("pageText = {}", pageText);
	}

	@Test
	public void extractPageText() throws IOException {
		String pageContent = new Scraper().url(PagedListingTable.getUrl()).asText().getResult();
		assertThat(pageContent.contains("<html>"), is(false));
		log.info("Content: {}", pageContent);
	}

	@Test
	public void extractTableFromPage() throws Exception {
		log.info("HtmlElementName.TABLE: {}", HTMLElementName.TABLE);
		String pageText = scraper
				.url(PagedListingTable.getUrl())
				.extract(scraper.extractor().table(3).execute())
				.getResult();
		log.debug("table extracted: {}", pageText);
		assertThat(pageText.startsWith("<table"), is(true));
		log.info(pageText);

	}

	@Test
	public void extractLinksFromTableOnPage() throws Exception {
		List<String> urls = scraper
				.url(PagedListingTable.getUrl())
				.extract(scraper.extractor().table(3).links().getResults())
				.getResults();

		assertThat(urls.size(), is(greaterThan(0)));

	}

	@Test
	public void extractContentsOfElementWithId() throws Exception {
		String tagText = scraper
				.url(ProgramDetailPage.getUrl())
				.extract(scraper.extractor().id(eligibilityCodeId).execute())
				.getResult();
		log.info("tag text: {}", tagText);
		assertThat(tagText.contains("nonprofit institutions of higher education"), is(true));
	}

	@Test
	public void extractContentsByClassAndOccurrence() throws Exception {
		String tagText = scraper
				.url(ProgramDetailPage.getUrl())
				.extract(scraper.extractor().ofClass(eligibilityClassName, 1).execute())
				.getResult();
		log.info("tag text: {}", tagText);
		assertThat(tagText.contains("39"), is(true));
		assertThat(tagText.contains("52"), is(true));
	}

	@Test
	public void extractParameterFromLinksInTable() throws Exception {
		Scraper scraper = new Scraper();
		List<String> ids = scraper
				.url(PagedListingTable.getUrl())
				.extract(scraper.extractor().table(3).links().parameter("oppId").getResults())
				.getResults();

		assertThat(ids.size(), is(greaterThan(0)));
		log.info("ids found: {}", ids);
		assertThat(ids, hasItems("40034", "40158", "40095", "40790", "40821", "40849", "40315"));
		assertThat(ids, hasItems("40967", "41255", "41282", "40458", "41599", "41734", "40667", "41771"));
		assertThat(ids, hasItems("41898", "41032", "41896", "42394", "42445"));
	}

	@Test
	public void extractLinksFromTableContainingString() throws Exception {
		Scraper scraper = new Scraper();
		String table = scraper
				.url(DetailPage.getUrl())
				.extract(scraper.extractor().table("Document Type").execute())
				.getResult();

		log.info("table matching {} : {}", "Document Type", table);
		assertThat(table.toString().contains("Document Type"), is(true));

	}

	@Test
	public void extractParameterFromLinksInIteratedTables() throws Exception {
		Scraper scraper = new Scraper();
		Iterator pageIterator = new Iterator() {
			@Override
			public URL build(int i) {
				String nextPageUrl = MessageFormat.format("/testpages/ids-page-{0}.html", i + 2);
				log.debug("next page to iterate to: {}", nextPageUrl);
				return TestUtil.getFileAsURL(nextPageUrl);
			}
		};
		List<String> ids = scraper
				.url(PagedListingTable.getUrl())
				.pages(1)
				.iterator(pageIterator)
				.extract(scraper.extractor().table(3).links().parameter("oppId").getResults())
				.getResults();

		assertThat(ids.size(), is(40));
		log.info("ids {} found: {}", ids.size(), ids);
	}

	@Test
	public void extractFieldsFromTable() throws IOException {
		Scraper scraper = new Scraper();
		Map<String, String> opportunities = scraper
				.url(DetailPage.getUrl())
				.extract(scraper.extractor().field("title", HTMLElementName.H1).getFields())
				.getFields();

		assertThat(opportunities.size(), is(greaterThan(0)));
		log.debug("fields = {}", opportunities);

	}

	@Test
	public void extractFieldsFromTableAndTitleFromH1() throws IOException {
		Scraper scraper = new Scraper();
		Map<String, String> opportunities = scraper
				.url(DetailPage.getUrl())
				.extract(scraper.extractor().field("title", HTMLElementName.H1).getFields())
				.extract(scraper.extractor().table(4).getFields())
				.getFields();

		assertThat(opportunities.size(), is(greaterThan(0)));
		assertThat(opportunities.get("title"), is(notNullValue()));
		log.debug("fields = {}", opportunities);

	}

	@Test
	public void extractFieldsBasedOnPairedTags() throws MalformedURLException, IOException {
		Scraper scraper = new Scraper();
		Map<String, String> fields = scraper
				.url(DetailPage.getUrl())
				.extract(scraper.extractor().pair(HTMLElementName.H4, HTMLElementName.DD).getFields())
				.getFields();

		assertThat(fields.size(), is(greaterThan(0)));
		// log.debug("fields = {}", fields);

	}

	@Test
	public void extractFieldWithMultipleValues() throws MalformedURLException, IOException {
		Scraper scraper = new Scraper();
		Map<String, String> fields = scraper
				.url(DetailPage.getUrl())
				.extract(scraper.extractor().pair(HTMLElementName.H4, HTMLElementName.DD).getFields())
				.getFields();

		assertThat(fields.size(), is(greaterThan(0)));
		
		String[] eligibilityCodes = fields.get("Eligible Applicants").split(";");
		assertThat(eligibilityCodes.length, is(greaterThan(1)));
		for (int i = 0; i < eligibilityCodes.length; i++){
			log.debug("eligibility code: {}", eligibilityCodes[i]);
		}
//		log.debug("eligibility = {}", fields.get("Eligible Applicants"));

	}

	@Test
	public void extractFieldsBasedOnPairedTagsAfterAnotherTag() throws MalformedURLException, IOException {
		Scraper scraper = new Scraper();
		Map<String, String> fields = scraper
				.url(DetailPage.getUrl())
				.extract(
						scraper.extractor()
								.after(HTMLElementName.TABLE, 5)
								.pair(HTMLElementName.H4, HTMLElementName.DD)
								.getFields())
				.getFields();

		assertThat(fields.size(), is(greaterThan(0)));
		assertThat(fields.keySet().contains("Eligible Applicants"), is(true));

	}

	@Ignore
	@Test
	public void useIteratedListingAndDetailInterface() throws IOException {
		Scraper scraper = new Scraper();
		Iterator pageIterator = new Iterator() {
			@Override
			public URL build(int i) {
				String nextPageUrl = MessageFormat.format("/testpages/ids-page-{0}.html", i + 2);
				log.debug("next page to iterate to: {}", nextPageUrl);
				return TestUtil.getFileAsURL(nextPageUrl);
			}
		};
		Scraper detailScraper = new Scraper();
		List<Map<String, String>> records = scraper
				.url(PagedListingTable.getUrl())
				.pages(3)
				.iterator(pageIterator)
				.listing(scraper.extractor().table(3).links().getResults())
				.detail(detailScraper)
				.getRecords();

		assertThat(records.size(), is(greaterThan(0)));
		log.debug("fields = {}", records);

	}

}
