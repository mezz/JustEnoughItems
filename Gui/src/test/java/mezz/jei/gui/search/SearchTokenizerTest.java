package mezz.jei.gui.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchTokenizerTest {

	private final SearchTokenizer tokenizer = new SearchTokenizer();

	@Test
	void simpleWords() {
		List<Token> tokens = tokenizer.tokenize("iron sword");

		assertEquals(2, tokens.size());
		assertEquals("iron", tokens.get(0).text());
		assertEquals("sword", tokens.get(1).text());
		assertFalse(tokens.get(0).quoted());
		assertFalse(tokens.get(1).quoted());
	}

	@Test
	void quotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("\"iron sword\"");

		assertEquals(1, tokens.size());
		assertEquals("iron sword", tokens.get(0).text());
		assertTrue(tokens.get(0).quoted());
	}

	@Test
	void unpairedOpeningQuote() {
		List<Token> tokens = tokenizer.tokenize("\"iron sword");

		assertEquals(1, tokens.size());
		assertEquals("iron sword", tokens.get(0).text());
		assertTrue(tokens.get(0).quoted());
	}

	@Test
	void exclusion() {
		List<Token> tokens = tokenizer.tokenize("-diamond");

		assertEquals(1, tokens.size());
		assertEquals("diamond", tokens.get(0).text());
		assertTrue(tokens.get(0).exclusion());
	}

	@Test
	void quotedWithExclusion() {
		List<Token> tokens = tokenizer.tokenize("-\"iron sword\"");

		assertEquals(1, tokens.size());
		assertEquals("iron sword", tokens.get(0).text());
		assertTrue(tokens.get(0).quoted());
		assertTrue(tokens.get(0).exclusion());
	}

	@Test
	void mixed() {
		List<Token> tokens = tokenizer.tokenize("sword \"iron pick\" -wood");

		assertEquals(3, tokens.size());

		assertEquals("sword", tokens.get(0).text());
		assertFalse(tokens.get(0).quoted());

		assertEquals("iron pick", tokens.get(1).text());
		assertTrue(tokens.get(1).quoted());

		assertEquals("wood", tokens.get(2).text());
		assertTrue(tokens.get(2).exclusion());
	}

	@Test
	void emptyAndWhitespace() {
		assertTrue(tokenizer.tokenize("").isEmpty());
		assertTrue(tokenizer.tokenize("   ").isEmpty());
	}

	@Test
	void onlyQuotes() {
		List<Token> tokens = tokenizer.tokenize("\"\"");
		// Decide what your tokenizer should do here and assert it
	}
}
