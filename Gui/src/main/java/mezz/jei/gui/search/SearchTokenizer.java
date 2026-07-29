package mezz.jei.gui.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SearchTokenizer {

	private static final Set<Character> PREFIXES = Set.of('@', '#', '$', '%', '^', '&');

	public List<Token> tokenize(String filterText) {
		List<Token> tokens = new ArrayList<>();

		if (filterText.isEmpty()) {
			return tokens;
		}

		StringBuilder current = new StringBuilder();
		int tokenStart = -1;
		boolean insideQuotes = false;
		boolean exclusion = false;
		boolean escaped = false;

		for (int i = 0; i < filterText.length(); i++) {
			char c = filterText.charAt(i);

			if (escaped) {
				current.append(c);
				escaped = false;
				continue;
			}

			if (c == '\\') {
				escaped = true;
				if (current.isEmpty()) {
					tokenStart = i;
				}
				continue;
			}

			if (c == '"') {
				if (insideQuotes) {
					// Closing quote
					addToken(tokens, current, exclusion);
					current.setLength(0);
					tokenStart = -1;
					insideQuotes = false;
					exclusion = false;
				} else {
					// Opening quote - finish any previous unquoted token first
					if (current.isEmpty()) {
						tokenStart = i + 1;
					}
					insideQuotes = true;
				}
				continue;
			}

			if (!insideQuotes && Character.isWhitespace(c)) {
				if (!current.isEmpty()) {
					addToken(tokens, current, exclusion);
					current.setLength(0);
					tokenStart = -1;
				}
				exclusion = false;
				continue;
			}

			// Handle exclusion only at the start of a token (outside quotes)
			if (!insideQuotes && current.isEmpty() && c == '-') {
				exclusion = true;
				continue;
			}

			if (current.isEmpty()) {
				tokenStart = i;
			}
			current.append(c);
		}

		// Handle remaining text
		if (!current.isEmpty() || insideQuotes) {
			addToken(tokens, current, exclusion);
		}

		return tokens;
	}

	private void addToken(List<Token> tokens, StringBuilder content, boolean exclusion) {
		String text = content.toString().trim();
		if (!text.isEmpty()) {
			tokens.add(new Token(text, exclusion));
		}
	}
}
