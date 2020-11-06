import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.Alphanumeric;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the {@link HtmlFetcher} class.
 *
 * DO NOT OVER-CALL THESE TESTS OR YOU CAN BE BLOCKED FROM THE WEB SERVER!
 */
@TestMethodOrder(Alphanumeric.class)
public class HtmlFetcherTest {

	/** How long to wait for individual tests to complete. */
	public static final Duration TIMEOUT = Duration.ofSeconds(30);
	
	/**
	 * Tests the {@link HtmlFetcher#isHtml(Map)} method.
	 *
	 * @see HtmlFetcher#isHtml(Map)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class A_HtmlTypeTests {
		/**
		 * Tests the {@link HtmlFetcher#isHtml(Map)} method for URLs that do not point to valid HTML webpages.
		 *
		 * @param link the link to test
		 * @throws IOException from {@link URL#openConnection()}
		 *
		 * @see HtmlFetcher#isHtml(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt"})
		@Order(1)
		public void testNotHtml(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertFalse(HtmlFetcher.isHtml(headers));
			});
		}

		/**
		 * Tests the {@link HtmlFetcher#isHtml(Map)} method for URLs that do point to valid HTML webpages.
		 *
		 * @param link the link to test
		 * @throws IOException from {@link URL#openConnection()}
		 *
		 * @see HtmlFetcher#isHtml(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/",
				"https://www.cs.usfca.edu/~cs212/simple/empty.html",
				"https://www.cs.usfca.edu/~cs212/birds/falcon.html",
				"https://www.cs.usfca.edu/~cs212/redirect/nowhere"})
		@Order(2)
		public void testIsHtml(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertTrue(HtmlFetcher.isHtml(headers));
			});
		}
	}

	/**
	 * Tests the status code methods.
	 *
	 * @see HtmlFetcher#getStatusCode(Map)
	 * @see HtmlFetcher#isRedirect(Map)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class B_StatusCodeTests {
		/**
		 * Tests that the status code is 200.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #test(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt",
				"https://www.cs.usfca.edu/~cs212/birds/yellowthroat.html"})
		@Order(1)
		public void test200(String link) throws IOException {
			test(link, 200);
		}

		/**
		 * Tests that the status code is 404.
		 *
		 * @throws IOException from {@link #test(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@Test
		@Order(2)
		public void test404() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs212/redirect/nowhere";
			test(link, 404);
		}

		/**
		 * Tests that the status code is 410.
		 *
		 * @throws IOException from {@link #test(String, int)}
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		@Test
		@Order(3)
		public void test410() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs212/redirect/gone";
			test(link, 410);
		}

		/**
		 * Tests that the status code is a redirect.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #test(String, int)}
		 *
		 * @see HtmlFetcher#isRedirect(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/redirect/loop1",
				"https://www.cs.usfca.edu/~cs212/redirect/loop2",
				"https://www.cs.usfca.edu/~cs212/redirect/one",
				"https://www.cs.usfca.edu/~cs212/redirect/two"})
		@Order(4)
		public void testRedirect(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertTrue(HtmlFetcher.isRedirect(headers));
			});
		}

		/**
		 * Tests that the status code is not a redirect.
		 *
		 * @param link the link to fetch
		 * @throws IOException from {@link #test(String, int)}
		 *
		 * @see HtmlFetcher#isRedirect(Map)
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/redirect/nowhere",
				"https://www.cs.usfca.edu/~cs212/redirect/gone"})
		@Order(5)
		public void testNotRedirect(String link) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				Assertions.assertFalse(HtmlFetcher.isRedirect(headers));
			});
		}

		/**
		 * Tests if the status code returned is as expected.
		 *
		 * @param link the URL to fetch
		 * @param code the expected status code
		 * @throws IOException from {@link URL#openConnection()}
		 *
		 * @see HtmlFetcher#getStatusCode(Map)
		 */
		public void test(String link, int code) throws IOException {
			URL url = new URL(link);
			HttpURLConnection.setFollowRedirects(false);

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				Map<String, List<String>> headers = url.openConnection().getHeaderFields();
				int actual = HtmlFetcher.getStatusCode(headers);
				Assertions.assertEquals(code, actual);
			});
		}
	}

	/**
	 * Tests fetching HTML for troublesome links.
	 *
	 * @see HtmlFetcher#fetch(String)
	 * @see HtmlFetcher#fetch(URL)
	 * @see HtmlFetcher#fetch(String, int)
	 * @see HtmlFetcher#fetch(URL, int)
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class C_FetchHtmlTests {
		/**
		 * Test that attempting to fetch pages that do not have valid HTML results in a null value.
		 *
		 * @param link the link to fetch
		 */
		@ParameterizedTest
		@ValueSource(strings = {
				"https://www.cs.usfca.edu/~cs212/simple/no_extension",
				"https://www.cs.usfca.edu/~cs212/simple/double_extension.html.txt",
				"https://www.cs.usfca.edu/~cs212/redirect/nowhere"})
		@Order(1)
		public void testNotValidHtml(String link) {
			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(link);
				Assertions.assertNull(html);
			});
		}

		/**
		 * Tests the HTML returned for a valid page.
		 *
		 * @throws IOException if unable to read html file
		 */
		@Test
		@Order(2)
		public void testValidHtml() throws IOException {
			String link = "https://www.cs.usfca.edu/~cs212/birds/yellowthroat.html";

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(link);
				Assertions.assertNotNull(html);

				Path hello = Paths.get("src", "test", "resources", "yellowthroat.html");
				List<String> lines = Files.readAllLines(hello, StandardCharsets.UTF_8);
				String expected = String.join("\n", lines);
				Assertions.assertEquals(expected, html);
			});
		}
	}
	
	/**
	 * Tests that redirects work.
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class D_RedirectTests {
		/**
		 * Tests that null is returned when a link does not resolve within a specific number of
		 * redirects.
		 *
		 * @param redirects the number of redirects to try
		 */
		@ParameterizedTest
		@ValueSource(ints = {-1, 0, 1, 2})
		@Order(1)
		public void testUnsuccessfulRedirect(int redirects) {
			String one = "https://www.cs.usfca.edu/~cs212/redirect/one";

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(one, redirects);
				Assertions.assertNull(html);
			});
		}

		/**
		 * Tests that proper HTML is returned when a link DOES resolve within a specific number of
		 * redirects.
		 *
		 * @param redirects the number of redirects to try
		 * @throws IOException if unable to read html file
		 */
		@ParameterizedTest
		@ValueSource(ints = {3, 4})
		@Order(2)
		public void testSuccessfulRedirect(int redirects) throws IOException {
			String one = "https://www.cs.usfca.edu/~cs212/redirect/one";

			Assertions.assertTimeoutPreemptively(TIMEOUT, () -> {
				String html = HtmlFetcher.fetch(one, redirects);
				Assertions.assertNotNull(html);

				Path hello = Paths.get("src", "test", "resources", "hello.html");
				List<String> lines = Files.readAllLines(hello, StandardCharsets.UTF_8);
				String expected = String.join("\n", lines);
				Assertions.assertEquals(expected, html);
			});
		}		
	}
	
	/**
	 * Tests that certain classes or packages do not appear in the implementation
	 * code. Attempts to fool this test will be considered cheating.
	 */
	@Nested
	public class E_ApproachTests {
		/**
		 * Tests that certain classes or packages do not appear in the implementation code. Attempts to
		 * fool this test will be considered cheating.
		 *
		 * @throws IOException if unable to read source code
		 */
		@Test
		public void testClasses() throws IOException {
			String source = Files.readString(Path.of("src", "main", "java", "HtmlFetcher.java"), StandardCharsets.UTF_8);
			Assertions.assertFalse(source.contains("import java.net.*;"), "Modify your code to use more specific import statements.");
			Assertions.assertFalse(source.contains("import java.net.URLConnection;"), "You may not use this class.");
			Assertions.assertFalse(source.contains("import java.net.HttpURLConnection;"), "You may not use this class.");
		}
	}
}
