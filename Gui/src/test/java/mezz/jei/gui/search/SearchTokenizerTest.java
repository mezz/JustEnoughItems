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
			new Token("iron", false),
			new Token("sword", false)
		), tokens);
	}

	@Test
	void quotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("\"iron sword\"");

		assertEquals(List.of(
			new Token("iron sword", false)
		), tokens);
	}

	@Test
	void unpairedOpeningQuote() {
		List<Token> tokens = tokenizer.tokenize("\"iron sword");

		assertEquals(List.of(
			new Token("iron sword", false)
		), tokens);
	}

	@Test
	void exclusion() {
		List<Token> tokens = tokenizer.tokenize("-diamond");

		assertEquals(List.of(
			new Token("diamond", true)
		), tokens);
	}

	@Test
	void quotedWithExclusion() {
		List<Token> tokens = tokenizer.tokenize("-\"iron sword\"");

		assertEquals(List.of(
			new Token("iron sword", true)
		), tokens);
	}

	@Test
	void unpairedOpeningQuoteWithDash() {
		List<Token> tokens = tokenizer.tokenize("\"-iron sword");

		assertEquals(List.of(
			new Token("-iron sword", false)
		), tokens);
	}

	@Test
	void dashInQuoted() {
		List<Token> tokens = tokenizer.tokenize("\"-iron sword\"");

		assertEquals(List.of(
			new Token("-iron sword", false)
		), tokens);
	}

	@Test
	void quotedWithExclusionWithDashInQuoted() {
		List<Token> tokens = tokenizer.tokenize("-\"-iron sword\"");

		assertEquals(List.of(
			new Token("-iron sword", true)
		), tokens);
	}

	@Test
	void mixed() {
		List<Token> tokens = tokenizer.tokenize("sword \"iron pick\" -wood");

		assertEquals(List.of(
			new Token("sword", false),
			new Token("iron pick", false),
			new Token("wood", true)
		), tokens);
	}

	@Test
	void emptyAndWhitespace() {
		assertTrue(tokenizer.tokenize("").isEmpty());
		assertTrue(tokenizer.tokenize("   ").isEmpty());
		assertTrue(tokenizer.tokenize("-   ").isEmpty());
		assertTrue(tokenizer.tokenize(" \"  ").isEmpty());
	}

	@Test
	void onlyQuotes() {
		List<Token> tokens = tokenizer.tokenize("\"\"");

		assertTrue(tokens.isEmpty());
	}

	@Test
	void escapedQuoteCanBeSearchedInUnquotedToken() {
		List<Token> tokens = tokenizer.tokenize("iron\\\"sword \\\"quoted\\\"");

		assertEquals(List.of(
			new Token("iron\"sword", false),
			new Token("\"quoted\"", false)
		), tokens);
	}

	@Test
	void escapedQuoteCanBeSearchedInQuotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("\"the \\\"best\\\" sword\"");

		assertEquals(List.of(
			new Token("the \"best\" sword", false)
		), tokens);
	}

	@Test
	void escapedQuoteCanBeSearchedInExcludedQuotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("-\"the \\\"best\\\" sword\"");

		assertEquals(List.of(
			new Token("the \"best\" sword", true)
		), tokens);
	}

	@Test
	void escapedQuoteCanBeSearchedInPrefixedQuotedPhrase() {
		String filterText = "$\"tooltip says \\\"place\\\"\" " +
			"-@\"mod \\\"name\\\"\"";
		List<Token> tokens = tokenizer.tokenize(filterText);

		assertEquals(List.of(
			new Token("$tooltip says \"place\"", false),
			new Token("@mod \"name\"", true)
		), tokens);
	}

	@Test
	void quotedPhraseTrimsWhitespaceInsideQuotes() {
		List<Token> tokens = tokenizer.tokenize("\"  iron sword  \" -\"  gold ingot  \"");

		assertEquals(List.of(
			new Token("iron sword", false),
			new Token("gold ingot", true)
		), tokens);
	}

	@Test
	void whitespaceCharactersSeparateTokensOutsideQuotes() {
		List<Token> tokens = tokenizer.tokenize("iron\t\"gold ingot\"\n-wood\r\n\"red\tstone\"");

		assertEquals(List.of(
			new Token("iron", false),
			new Token("gold ingot", false),
			new Token("wood", true),
			new Token("red\tstone", false)
		), tokens);
	}

	@Test
	void prefixedQuotedPhrase() {
		List<Token> tokens = tokenizer.tokenize("%\"redstone blocks\"");

		assertEquals(List.of(
			new Token("%redstone blocks", false)
		), tokens);
	}

	@Test
	void prefixedQuotedPhraseWithExclusion() {
		List<Token> tokens = tokenizer.tokenize("-%\"redstone blocks\"");

		assertEquals(List.of(
			new Token("%redstone blocks", true)
		), tokens);
	}

	@Test
	void eachSearchPrefixSupportsQuotedPhrases() {
		String filterText = """
			@"just enough items" \
			#"minecraft logs" \
			$"tooltip text" \
			%"redstone blocks" \
			^"light blue" \
			&"minecraft:iron_ingot\"""";
		List<Token> tokens = tokenizer.tokenize(filterText);

		assertEquals(List.of(
			new Token("@just enough items", false),
			new Token("#minecraft logs", false),
			new Token("$tooltip text", false),
			new Token("%redstone blocks", false),
			new Token("^light blue", false),
			new Token("&minecraft:iron_ingot", false)
		), tokens);
	}

	@Test
	void eachSearchPrefixSupportsExcludedQuotedPhrases() {
		String filterText = """
			-@"just enough items" \
			-#"minecraft logs" \
			-$"tooltip text" \
			-%"redstone blocks" \
			-^"light blue" \
			-&"minecraft:iron_ingot\"""";
		List<Token> tokens = tokenizer.tokenize(filterText);

		assertEquals(List.of(
			new Token("@just enough items", true),
			new Token("#minecraft logs", true),
			new Token("$tooltip text", true),
			new Token("%redstone blocks", true),
			new Token("^light blue", true),
			new Token("&minecraft:iron_ingot", true)
		), tokens);
	}

	@Test
	void unpairedOpeningQuoteAfterPrefix() {
		List<Token> tokens = tokenizer.tokenize("%\"redstone blocks");

		assertEquals(List.of(
			new Token("%redstone blocks", false)
		), tokens);
	}

	@Test
	void bareExclusionMarkerDoesNotExcludeNextToken() {
		List<Token> tokens = tokenizer.tokenize("- diamond - \"iron sword\"");

		assertEquals(List.of(
			new Token("diamond", false),
			new Token("iron sword", false)
		), tokens);
	}

	@Test
	void emptyQuotedExclusionsDoNotAffectNextToken() {
		List<Token> tokens = tokenizer.tokenize("-\"\" diamond -\"  \" gold");

		assertEquals(List.of(
			new Token("diamond", false),
			new Token("gold", false)
		), tokens);
	}

	@Test
	void dashInsideUnquotedTokenIsNotExclusion() {
		List<Token> tokens = tokenizer.tokenize("iron-sword @mod-name %-redstone");

		assertEquals(List.of(
			new Token("iron-sword", false),
			new Token("@mod-name", false),
			new Token("%-redstone", false)
		), tokens);
	}

	@Test
	void repeatedTokens() {
		List<Token> tokens = tokenizer.tokenize("diamond diamond diamond diamond");

		assertEquals(List.of(
			new Token("diamond", false),
			new Token("diamond", false),
			new Token("diamond", false),
			new Token("diamond", false)
		), tokens);
	}

	@Test
	void repeatedHyphenIgnoresAdditionalHyphens() {
		List<Token> tokens = tokenizer.tokenize("-diamond --diamond ---diamond ----diamond");

		assertEquals(List.of(
			new Token("diamond", true),
			new Token("diamond", true),
			new Token("diamond", true),
			new Token("diamond", true)
		), tokens);
	}
}
