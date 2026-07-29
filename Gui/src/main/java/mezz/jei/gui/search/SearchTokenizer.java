package mezz.jei.gui.search;

import java.util.ArrayList;
import java.util.List;

public class SearchTokenizer {

	public List<Token> tokenize(String filterText) {
		List<Token> tokens = new ArrayList<>();

		if (filterText == null || filterText.isEmpty()) {
			return tokens;
		}

		StringBuilder current = new StringBuilder();
		int tokenStart = 0;
		boolean insideQuotes = false;
		boolean exclusion = false;

		for (int i = 0; i < filterText.length(); i++) {
			char c = filterText.charAt(i);

			if (c == '"') {
				if (insideQuotes) {
					// Closing quote
					addToken(tokens, current, tokenStart, i, true, exclusion);
					current.setLength(0);
					exclusion = false;
				} else {
					// Opening quote - finish any previous unquoted token first
					if (!current.isEmpty()) {
						addToken(tokens, current, tokenStart, i, false, exclusion);
						current.setLength(0);
						exclusion = false;
					}
				}
				insideQuotes = !insideQuotes;
				tokenStart = i + 1;
				continue;
			}

			if (!insideQuotes && Character.isWhitespace(c)) {
				if (!current.isEmpty()) {
					addToken(tokens, current, tokenStart, i, false, exclusion);
					current.setLength(0);
					exclusion = false;
				}
				tokenStart = i + 1;
				continue;
			}

			// Handle exclusion only at the start of a token (outside quotes)
			if (!insideQuotes && current.isEmpty() && c == '-') {
				exclusion = true;
				tokenStart = i + 1;
				continue;
			}

			if (current.isEmpty()) {
				tokenStart = i;
			}
			current.append(c);
		}

		// Handle remaining text
		if (!current.isEmpty() || insideQuotes) {
			addToken(tokens, current, tokenStart, filterText.length(), insideQuotes, exclusion);
		}

		return tokens;
	}

	private void addToken(List<Token> tokens, StringBuilder content, int start, int end, boolean quoted, boolean exclusion) {
		String text = content.toString().trim();
		if (!text.isEmpty()) {
			tokens.add(new Token(text, start, end, quoted, exclusion));
		}
	}
}
