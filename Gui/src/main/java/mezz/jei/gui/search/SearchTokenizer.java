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
        int tokenStart = 0;
        boolean insideQuotes = false, exclusion = false;

        for (int i = 0; i < filterText.length(); i++) {
            char c = filterText.charAt(i);

            if (c == '"') {
                if(insideQuotes || !current.isEmpty()) {
                    addToken(tokens, current, tokenStart, i, insideQuotes, exclusion);
                    exclusion = false;
                }
                insideQuotes = !insideQuotes;
                tokenStart = i + 1;
                continue;
            }

            if (!insideQuotes && Character.isWhitespace(c)) {
                if (!current.isEmpty()) {
                    addToken(tokens, current, tokenStart, i, false, exclusion);
                    exclusion = false;
                }
                tokenStart = i + 1;
                continue;
            }

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