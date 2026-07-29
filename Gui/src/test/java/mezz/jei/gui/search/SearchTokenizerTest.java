package mezz.jei.gui.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchTokenizerTest {

	private final SearchTokenizer tokenizer = new SearchTokenizer();

	@Test
	void simpleWords() {
		List<Token> tokens = tokenizer.tokenize("iron sword");

		assertEquals(List.of(
			new Token("iron", 0, 4, false, false),
			new Token("sword", 5, 10, false, false)
		), tokens);
	}

	@Test
	void quotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("\"iron sword\"");

		assertEquals(List.of(
			new Token("iron sword", 1, 11, true, false)
		), tokens);
	}

	@Test
	void unpairedOpeningQuote() {
		List<Token> tokens = tokenizer.tokenize("\"iron sword");

		assertEquals(List.of(
			new Token("iron sword", 1, 11, true, false)
		), tokens);
	}

	@Test
	void exclusion() {
		List<Token> tokens = tokenizer.tokenize("-diamond");

		assertEquals(List.of(
			new Token("diamond", 1, 8, false, true)
		), tokens);
	}

	@Test
	void quotedWithExclusion() {
		List<Token> tokens = tokenizer.tokenize("-\"iron sword\"");

		assertEquals(List.of(
			new Token("iron sword", 2, 12, true, true)
		), tokens);
	}

	@Test
	void mixed() {
		List<Token> tokens = tokenizer.tokenize("sword \"iron pick\" -wood");

		assertEquals(List.of(
			new Token("sword", 0, 5, false, false),
			new Token("iron pick", 7, 16, true, false),
			new Token("wood", 19, 23, false, true)
		), tokens);
	}

	@Test
	void emptyAndWhitespace() {
		assertTrue(tokenizer.tokenize(null).isEmpty());
		assertTrue(tokenizer.tokenize("").isEmpty());
		assertTrue(tokenizer.tokenize("   ").isEmpty());
	}

	@Test
	void onlyQuotes() {
		List<Token> tokens = tokenizer.tokenize("\"\"");

		assertTrue(tokens.isEmpty());
	}

	@Test
	void prefixedQuotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("%\"redstone blocks\"");

		assertEquals(List.of(
			new Token("%redstone blocks", 0, 17, true, false)
		), tokens);
	}

	@Test
	void prefixedQuotedPhraseWithExclusion() {
		List<Token> tokens = tokenizer.tokenize("-%\"redstone blocks\"");

		assertEquals(List.of(
			new Token("%redstone blocks", 1, 18, true, true)
		), tokens);
	}

	@Test
	void bareExclusionMarkerDoesNotExcludeNextToken() {
		List<Token> tokens = tokenizer.tokenize("- diamond - \"iron sword\"");

		assertEquals(List.of(
			new Token("diamond", 2, 9, false, false),
			new Token("iron sword", 13, 23, true, false)
		), tokens);
	}

	@Test
	void repeatedHyphenKeepsAdditionalHyphensInToken() {
		List<Token> tokens = tokenizer.tokenize("--diamond");

		assertEquals(List.of(
			new Token("-diamond", 1, 9, false, true)
		), tokens);
	}
}
