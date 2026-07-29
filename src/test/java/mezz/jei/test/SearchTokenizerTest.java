package mezz.jei.test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mezz.jei.search.SearchToken;
import mezz.jei.search.SearchTokenizer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchTokenizerTest {
	private final SearchTokenizer tokenizer = new SearchTokenizer();

	@Test
	public void tokenizesWordsQuotedPhrasesAndExclusions() {
		List<SearchToken> tokens = tokenizer.tokenize("sword \"iron pick\" -wood");

		assertEquals(Arrays.asList(
			new SearchToken("sword", false),
			new SearchToken("iron pick", false),
			new SearchToken("wood", true)
		), tokens);
	}

	@Test
	public void acceptsUnpairedOpeningQuote() {
		assertEquals(
			Collections.singletonList(new SearchToken("iron sword", false)),
			tokenizer.tokenize("\"iron sword")
		);
	}

	@Test
	public void supportsEscapedQuotesInQuotedPhrases() {
		assertEquals(
			Collections.singletonList(new SearchToken("the \"best\" sword", true)),
			tokenizer.tokenize("-\"the \\\"best\\\" sword\"")
		);
	}

	@Test
	public void keepsPrefixesWithQuotedPhrases() {
		assertEquals(Arrays.asList(
			new SearchToken("$tooltip text", false),
			new SearchToken("@mod name", true)
		), tokenizer.tokenize("$\"tooltip text\" -@\"mod name\""));
	}

	@Test
	public void bareExclusionDoesNotAffectNextToken() {
		assertEquals(
			Collections.singletonList(new SearchToken("diamond", false)),
			tokenizer.tokenize("- diamond")
		);
	}

	@Test
	public void ignoresEmptyQuotedTokens() {
		assertTrue(tokenizer.tokenize("-\"\" \"  \"").isEmpty());
	}
}
