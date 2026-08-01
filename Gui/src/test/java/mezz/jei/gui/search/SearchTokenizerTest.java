package mezz.jei.gui.search;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchTokenizerTest {

	private static Token token(String text, boolean exclusion, int start, int end) {
		return new Token(text, exclusion, false, start, end);
	}

	@Test
	void simpleWords() {
		List<Token> tokens = SearchTokenizer.tokenize("iron sword");

		assertEquals(List.of(
			token("iron", false, 0, 4),
			token("sword", false, 5, 10)
		), tokens);
	}

	@Test
	void quotedPhrase() {
		List<Token> tokens = SearchTokenizer.tokenize("\"iron sword\"");

		assertEquals(List.of(
			token("iron sword", false, 0, 12)
		), tokens);
	}

	@Test
	void unpairedOpeningQuote() {
		List<Token> tokens = SearchTokenizer.tokenize("\"iron sword");

		assertEquals(List.of(
			token("iron sword", false, 0, 11)
		), tokens);
	}

	@Test
	void exclusion() {
		List<Token> tokens = SearchTokenizer.tokenize("-diamond");

		assertEquals(List.of(
			token("diamond", true, 0, 8)
		), tokens);
	}

	@Test
	void quotedWithExclusion() {
		List<Token> tokens = SearchTokenizer.tokenize("-\"iron sword\"");

		assertEquals(List.of(
			token("iron sword", true, 0, 13)
		), tokens);
	}

	@Test
	void unpairedOpeningQuoteWithDash() {
		List<Token> tokens = SearchTokenizer.tokenize("\"-iron sword");

		assertEquals(List.of(
			token("-iron sword", false, 0, 12)
		), tokens);
	}

	@Test
	void dashInQuoted() {
		List<Token> tokens = SearchTokenizer.tokenize("\"-iron sword\"");

		assertEquals(List.of(
			token("-iron sword", false, 0, 13)
		), tokens);
	}

	@Test
	void quotedWithExclusionWithDashInQuoted() {
		List<Token> tokens = SearchTokenizer.tokenize("-\"-iron sword\"");

		assertEquals(List.of(
			token("-iron sword", true, 0, 14)
		), tokens);
	}

	@Test
	void mixed() {
		List<Token> tokens = SearchTokenizer.tokenize("sword \"iron pick\" -wood");

		assertEquals(List.of(
			token("sword", false, 0, 5),
			token("iron pick", false, 6, 17),
			token("wood", true, 18, 23)
		), tokens);
	}

	@Test
	void emptyAndWhitespace() {
		assertTrue(SearchTokenizer.tokenize("").isEmpty());
		assertTrue(SearchTokenizer.tokenize("   ").isEmpty());
		assertTrue(SearchTokenizer.tokenize("-   ").isEmpty());
		assertTrue(SearchTokenizer.tokenize(" \"  ").isEmpty());
	}

	@Test
	void onlyQuotes() {
		List<Token> tokens = SearchTokenizer.tokenize("\"\"");

		assertTrue(tokens.isEmpty());
	}

	@Test
	void escapedQuoteCanBeSearchedInUnquotedToken() {
		List<Token> tokens = SearchTokenizer.tokenize("iron\\\"sword \\\"quoted\\\"");

		assertEquals(List.of(
			token("iron\"sword", false, 0, 11),
			token("\"quoted\"", false, 12, 22)
		), tokens);
	}

	@Test
	void escapedQuoteCanBeSearchedInQuotedPhrase() {
		List<Token> tokens = SearchTokenizer.tokenize("\"the \\\"best\\\" sword\"");

		assertEquals(List.of(
			token("the \"best\" sword", false, 0, 20)
		), tokens);
	}

	@Test
	void escapedQuoteCanBeSearchedInExcludedQuotedPhrase() {
		List<Token> tokens = SearchTokenizer.tokenize("-\"the \\\"best\\\" sword\"");

		assertEquals(List.of(
			token("the \"best\" sword", true, 0, 21)
		), tokens);
	}

	@Test
	void escapedQuoteCanBeSearchedInPrefixedQuotedPhrase() {
		String filterText = "$\"tooltip says \\\"place\\\"\" " +
			"-@\"mod \\\"name\\\"\"";
		List<Token> tokens = SearchTokenizer.tokenize(filterText);

		assertEquals(List.of(
			token("$tooltip says \"place\"", false, 0, 25),
			token("@mod \"name\"", true, 26, 42)
		), tokens);
	}

	@Test
	void quotedPhraseTrimsWhitespaceInsideQuotes() {
		List<Token> tokens = SearchTokenizer.tokenize("\"  iron sword  \" -\"  gold ingot  \"");

		assertEquals(List.of(
			token("iron sword", false, 0, 16),
			token("gold ingot", true, 17, 34)
		), tokens);
	}

	@Test
	void whitespaceCharactersSeparateTokensOutsideQuotes() {
		List<Token> tokens = SearchTokenizer.tokenize("iron\t\"gold ingot\"\n-wood\r\n\"red\tstone\"");

		assertEquals(List.of(
			token("iron", false, 0, 4),
			token("gold ingot", false, 5, 17),
			token("wood", true, 18, 23),
			token("red\tstone", false, 25, 36)
		), tokens);
	}

	@Test
	void prefixedQuotedPhrase() {
		List<Token> tokens = SearchTokenizer.tokenize("%\"redstone blocks\"");

		assertEquals(List.of(
			token("%redstone blocks", false, 0, 18)
		), tokens);
	}

	@Test
	void prefixedQuotedPhraseWithExclusion() {
		List<Token> tokens = SearchTokenizer.tokenize("-%\"redstone blocks\"");

		assertEquals(List.of(
			token("%redstone blocks", true, 0, 19)
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
		List<Token> tokens = SearchTokenizer.tokenize(filterText);

		assertEquals(List.of(
			token("@just enough items", false, 0, 20),
			token("#minecraft logs", false, 21, 38),
			token("$tooltip text", false, 39, 54),
			token("%redstone blocks", false, 55, 73),
			token("^light blue", false, 74, 87),
			token("&minecraft:iron_ingot", false, 88, 111)
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
		List<Token> tokens = SearchTokenizer.tokenize(filterText);

		assertEquals(List.of(
			token("@just enough items", true, 0, 21),
			token("#minecraft logs", true, 22, 40),
			token("$tooltip text", true, 41, 57),
			token("%redstone blocks", true, 58, 77),
			token("^light blue", true, 78, 92),
			token("&minecraft:iron_ingot", true, 93, 117)
		), tokens);
	}

	@Test
	void unpairedOpeningQuoteAfterPrefix() {
		List<Token> tokens = SearchTokenizer.tokenize("%\"redstone blocks");

		assertEquals(List.of(
			token("%redstone blocks", false, 0, 17)
		), tokens);
	}

	@Test
	void bareExclusionMarkerDoesNotExcludeNextToken() {
		List<Token> tokens = SearchTokenizer.tokenize("- diamond - \"iron sword\"");

		assertEquals(List.of(
			token("diamond", false, 2, 9),
			token("iron sword", false, 12, 24)
		), tokens);
	}

	@Test
	void emptyQuotedExclusionsDoNotAffectNextToken() {
		List<Token> tokens = SearchTokenizer.tokenize("-\"\" diamond -\"  \" gold");

		assertEquals(List.of(
			token("diamond", false, 4, 11),
			token("gold", false, 18, 22)
		), tokens);
	}

	@Test
	void dashInsideUnquotedTokenIsNotExclusion() {
		List<Token> tokens = SearchTokenizer.tokenize("iron-sword @mod-name %-redstone");

		assertEquals(List.of(
			token("iron-sword", false, 0, 10),
			token("@mod-name", false, 11, 20),
			token("%-redstone", false, 21, 31)
		), tokens);
	}

	@Test
	void repeatedTokens() {
		List<Token> tokens = SearchTokenizer.tokenize("diamond diamond diamond diamond");

		assertEquals(List.of(
			token("diamond", false, 0, 7),
			token("diamond", false, 8, 15),
			token("diamond", false, 16, 23),
			token("diamond", false, 24, 31)
		), tokens);
	}

	@Test
	void orOperator() {
		List<Token> tokens = SearchTokenizer.tokenize("iron|sword");

		assertEquals(List.of(
			token("iron", false, 0, 4),
			Token.operator(4),
			token("sword", false, 5, 10)
		), tokens);
	}

	@Test
	void orOperatorWithSpaces() {
		List<Token> tokens = SearchTokenizer.tokenize("iron | sword");

		assertEquals(List.of(
			token("iron", false, 0, 4),
			Token.operator(5),
			token("sword", false, 7, 12)
		), tokens);
	}

	@Test
	void orOperatorInsideQuotes() {
		List<Token> tokens = SearchTokenizer.tokenize("\"iron|sword\"");

		assertEquals(List.of(
			token("iron|sword", false, 0, 12)
		), tokens);
	}

	@Test
	void escapedOrOperatorIsNotOperator() {
		List<Token> tokens = SearchTokenizer.tokenize("iron\\|sword");

		assertEquals(List.of(
			token("iron|sword", false, 0, 11)
		), tokens);
	}

	@Test
	void literalPipeIsNotOperator() {
		List<Token> quoted = SearchTokenizer.tokenize("\"|\"");
		assertEquals(List.of(
			token("|", false, 0, 3)
		), quoted);

		List<Token> escaped = SearchTokenizer.tokenize("\\|");
		assertEquals(List.of(
			token("|", false, 0, 2)
		), escaped);
	}

	@Test
	void orOperatorWithExclusion() {
		List<Token> tokens = SearchTokenizer.tokenize("-iron|sword");

		assertEquals(List.of(
			token("iron", true, 0, 5),
			Token.operator(5),
			token("sword", false, 6, 11)
		), tokens);
	}

	@Test
	void multipleOrOperators() {
		List<Token> tokens = SearchTokenizer.tokenize("a|b|c");

		assertEquals(List.of(
			token("a", false, 0, 1),
			Token.operator(1),
			token("b", false, 2, 3),
			Token.operator(3),
			token("c", false, 4, 5)
		), tokens);
	}

	@Test
	void leadingAndTrailingOr() {
		List<Token> leading = SearchTokenizer.tokenize("|iron");
		assertEquals(List.of(
			Token.operator(0),
			token("iron", false, 1, 5)
		), leading);

		List<Token> trailing = SearchTokenizer.tokenize("iron|");
		assertEquals(List.of(
			token("iron", false, 0, 4),
			Token.operator(4)
		), trailing);
	}

	@Test
	void splitByOperators() {
		List<Token> tokens = List.of(
			token("iron", false, 0, 4),
			Token.operator(4),
			token("sword", false, 5, 10),
			Token.operator(10),
			token("gold", false, 11, 15)
		);

		List<List<Token>> groups = SearchTokenizer.splitByOperators(tokens);
		assertEquals(3, groups.size());
		assertEquals(List.of(token("iron", false, 0, 4)), groups.get(0));
		assertEquals(List.of(token("sword", false, 5, 10)), groups.get(1));
		assertEquals(List.of(token("gold", false, 11, 15)), groups.get(2));
	}

	@Test
	void splitByOperatorsWithoutOperators() {
		List<Token> tokens = List.of(
			token("iron", false, 0, 4),
			token("sword", false, 5, 10)
		);

		List<List<Token>> groups = SearchTokenizer.splitByOperators(tokens);
		assertEquals(1, groups.size());
		assertEquals(tokens, groups.getFirst());
	}

	@Test
	void splitByOperatorsEmpty() {
		List<List<Token>> groups = SearchTokenizer.splitByOperators(List.of());
		assertEquals(1, groups.size());
		assertTrue(groups.getFirst().isEmpty());
	}

	@Test
	void unpairedQuoteSeparatesWords() {
		List<Token> tokens = SearchTokenizer.tokenize("iron\"sword");

		assertEquals(List.of(
			token("iron", false, 0, 4),
			token("sword", false, 5, 10)
		), tokens);
	}

	@Test
	void unpairedQuoteAtEndDoesNotProduceEmptyToken() {
		List<Token> tokens = SearchTokenizer.tokenize("iron\"");

		assertEquals(List.of(
			token("iron", false, 0, 4)
		), tokens);
	}

	@Test
	void prefixBeforeUnpairedQuoteStartsQuotedPhrase() {
		List<Token> tokens = SearchTokenizer.tokenize("%\"redstone blocks");

		assertEquals(List.of(
			token("%redstone blocks", false, 0, 17)
		), tokens);
	}

	@Test
	void adjacentQuotedPhrases() {
		List<Token> tokens = SearchTokenizer.tokenize("\"a\"\"b\"");

		assertEquals(List.of(
			token("a", false, 0, 3),
			token("b", false, 3, 6)
		), tokens);
	}
}
