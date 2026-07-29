package mezz.jei.gui.search;

import java.util.ArrayList;
import java.util.List;
public class SearchTokenizer {

	public List<Token> tokenize(String filterText) {
		List<Token> tokens = new ArrayList<>();

		if (filterText.isEmpty()) {
			return tokens;
		}

		StringBuilder current = new StringBuilder();
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
				continue;
			}

			if (c == '"') {
				if (insideQuotes) {
					// Closing quote
					addToken(tokens, current, exclusion);
					current.setLength(0);
					insideQuotes = false;
					exclusion = false;
				} else {
					insideQuotes = true;
				}
				continue;
			}

			if (!insideQuotes && Character.isWhitespace(c)) {
				if (!current.isEmpty()) {
					addToken(tokens, current, exclusion);
					current.setLength(0);
				}
				exclusion = false;
				continue;
			}

			// Handle exclusion only at the start of a token (outside quotes)
			if (!insideQuotes && current.isEmpty() && c == '-') {
				exclusion = true;
				continue;
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
