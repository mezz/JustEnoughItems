package mezz.jei.gui.search;

import java.util.ArrayList;
import java.util.List;

public class SearchTokenizer {

	private SearchTokenizer() {
	}

	public static List<Token> tokenize(String filterText) {
		List<Token> tokens = new ArrayList<>();

		if (filterText.isEmpty()) {
			return tokens;
		}

		StringBuilder current = new StringBuilder();
		boolean insideQuotes = false;
		boolean exclusion = false;
		boolean escaped = false;
		int tokenStart = -1;
		int tokenEnd = -1;

		for (int i = 0; i < filterText.length(); i++) {
			char c = filterText.charAt(i);

			if (escaped) {
				current.append(c);
				tokenEnd = i + 1;
				escaped = false;
				continue;
			}

			if (c == '\\') {
				escaped = true;
				if (tokenStart == -1) {
					tokenStart = i;
				}
				tokenEnd = i + 1;
				continue;
			}

			if (c == '"') {
				if (insideQuotes) {
					// Closing quote
					addToken(tokens, current, exclusion, tokenStart, i + 1);
					current.setLength(0);
					insideQuotes = false;
					exclusion = false;
					tokenStart = -1;
					tokenEnd = -1;
				} else if (!current.isEmpty() && !isPrefix(current.toString())) {
					addToken(tokens, current, exclusion, tokenStart, tokenEnd);
					current.setLength(0);
					exclusion = false;
					tokenStart = -1;
					tokenEnd = -1;
				} else {
					insideQuotes = true;
					if (tokenStart == -1) {
						tokenStart = i;
					}
					tokenEnd = i + 1;
				}
				continue;
			}

			if (!insideQuotes && Character.isWhitespace(c)) {
				if (!current.isEmpty()) {
					addToken(tokens, current, exclusion, tokenStart, tokenEnd);
					current.setLength(0);
				}
				exclusion = false;
				tokenStart = -1;
				tokenEnd = -1;
				continue;
			}

			if (!insideQuotes && c == '|') {
				if (!current.isEmpty()) {
					addToken(tokens, current, exclusion, tokenStart, tokenEnd);
					current.setLength(0);
				}
				tokens.add(Token.operator(i));
				exclusion = false;
				tokenStart = -1;
				tokenEnd = -1;
				continue;
			}

			// Handle exclusion only at the start of a token (outside quotes)
			if (!insideQuotes && current.isEmpty() && c == '-') {
				exclusion = true;
				tokenStart = i;
				tokenEnd = i + 1;
				continue;
			}

			if (tokenStart == -1) {
				tokenStart = i;
			}
			tokenEnd = i + 1;
			current.append(c);
		}

		// Handle remaining text
		if (!current.isEmpty() || insideQuotes) {
			addToken(tokens, current, exclusion, tokenStart, tokenEnd);
		}

		return tokens;
	}

	public static List<List<Token>> splitByOperators(List<Token> tokens) {
		List<List<Token>> groups = new ArrayList<>();
		List<Token> current = new ArrayList<>();
		for (Token token : tokens) {
			if (token.operator()) {
				groups.add(current);
				current = new ArrayList<>();
			} else {
				current.add(token);
			}
		}
		groups.add(current);
		return groups;
	}

	private static void addToken(List<Token> tokens, StringBuilder content, boolean exclusion, int start, int end) {
		String text = content.toString().trim();
		if (!text.isEmpty()) {
			tokens.add(new Token(text, exclusion, false, start, end));
		}
	}

	private static boolean isPrefix(String text) {
		if (text.length() != 1) {
			return false;
		}
		char c = text.charAt(0);
		return !Character.isLetterOrDigit(c) && c != '-' && c != '|';
	}
}
