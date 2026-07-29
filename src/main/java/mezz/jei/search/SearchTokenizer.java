package mezz.jei.search;

import java.util.ArrayList;
import java.util.List;

public final class SearchTokenizer {
	public List<SearchToken> tokenize(String filterText) {
		List<SearchToken> tokens = new ArrayList<>();
		if (filterText.isEmpty()) {
			return tokens;
		}

		StringBuilder current = new StringBuilder();
		boolean insideQuotes = false;
		boolean exclusion = false;
		boolean escaped = false;

		for (int i = 0; i < filterText.length(); i++) {
			char character = filterText.charAt(i);
			if (escaped) {
				current.append(character);
				escaped = false;
				continue;
			}
			if (character == '\\') {
				escaped = true;
				continue;
			}
			if (character == '"') {
				if (insideQuotes) {
					addToken(tokens, current, exclusion);
					current.setLength(0);
					insideQuotes = false;
					exclusion = false;
				} else {
					insideQuotes = true;
				}
				continue;
			}
			if (!insideQuotes && Character.isWhitespace(character)) {
				if (current.length() > 0) {
					addToken(tokens, current, exclusion);
					current.setLength(0);
				}
				exclusion = false;
				continue;
			}
			if (!insideQuotes && current.length() == 0 && character == '-') {
				exclusion = true;
				continue;
			}
			current.append(character);
		}

		if (escaped) {
			current.append('\\');
		}
		if (current.length() > 0 || insideQuotes) {
			addToken(tokens, current, exclusion);
		}
		return tokens;
	}

	private static void addToken(List<SearchToken> tokens, StringBuilder content, boolean exclusion) {
		String text = content.toString().trim();
		if (!text.isEmpty()) {
			tokens.add(new SearchToken(text, exclusion));
		}
	}
}
