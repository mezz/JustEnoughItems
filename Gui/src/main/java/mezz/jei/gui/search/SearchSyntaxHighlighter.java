package mezz.jei.gui.search;

import mezz.jei.common.search.PrefixInfo;
import mezz.jei.common.search.SearchMode;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public class SearchSyntaxHighlighter {
	private static final int PREFIX_COLOR = 0xFF55FFFF;
	private static final int PREFIXED_CONTENT_COLOR = 0xFF55FF55;
	private static final int OPERATOR_COLOR = 0xFFFFAA00;
	private static final int DEFAULT_CONTENT_COLOR = 0xFFFFFFFF;
	private static final int NO_MATCH_COLOR = 0xFFFF0000;

	private final BooleanSupplier filterEmpty;
	private final Supplier<String> textSupplier;
	private final ISearchCompletionProvider completionProvider;

	private String lastText = "";
	private boolean lastEmpty;
	private Set<Character> lastPrefixes = Set.of();
	private int @Nullable [] lastColors;

	public SearchSyntaxHighlighter(BooleanSupplier filterEmpty, Supplier<String> textSupplier, ISearchCompletionProvider completionProvider) {
		this.filterEmpty = filterEmpty;
		this.textSupplier = textSupplier;
		this.completionProvider = completionProvider;
	}

	public FormattedCharSequence format(String text, int offset) {
		if (text.isEmpty()) {
			return FormattedCharSequence.EMPTY;
		}

		int[] colors = getColors();
		if (offset >= colors.length) {
			return FormattedCharSequence.EMPTY;
		}

		List<FormattedCharSequence> parts = new ArrayList<>();
		int end = Math.min(offset + text.length(), colors.length);
		int i = offset;
		while (i < end) {
			int color = colors[i];
			int j = i + 1;
			while (j < end && colors[j] == color) {
				j++;
			}
			String segment = text.substring(i - offset, j - offset);
			parts.add(FormattedCharSequence.forward(segment, Style.EMPTY.withColor(color)));
			i = j;
		}
		return FormattedCharSequence.composite(parts);
	}

	private int[] getColors() {
		String fullText = textSupplier.get();
		boolean empty = filterEmpty.getAsBoolean();
		Set<Character> prefixes = getEnabledPrefixes();
		if (fullText.equals(lastText) && lastEmpty == empty && prefixes.equals(lastPrefixes) && lastColors != null) {
			return lastColors;
		}

		int length = fullText.length();
		int[] colors = new int[length];
		Arrays.fill(colors, empty ? NO_MATCH_COLOR : DEFAULT_CONTENT_COLOR);
		if (!empty) {
			Matcher matcher = SearchTokenizer.fullTokenMatcher(fullText);
			while (matcher.find()) {
				colorToken(fullText, matcher.group(), matcher.start(), matcher.end(), colors, prefixes);
			}
		}

		this.lastText = fullText;
		this.lastEmpty = empty;
		this.lastPrefixes = prefixes;
		return this.lastColors = colors;
	}

	private void colorToken(String fullText, String token, int start, int end, int[] colors, Set<Character> prefixes) {
		if (token.equals("|")) {
			fill(colors, start, end, OPERATOR_COLOR);
			return;
		}

		int pos = start;
		if (pos < end && fullText.charAt(pos) == '-') {
			colors[pos++] = OPERATOR_COLOR;
		}
		if (pos < end && prefixes.contains(fullText.charAt(pos))) {
			colors[pos++] = PREFIX_COLOR;
			int color = (pos < end && fullText.charAt(pos) == '"') ? DEFAULT_CONTENT_COLOR : PREFIXED_CONTENT_COLOR;
			fill(colors, pos, end, color);
			return;
		}
		if (pos < end && fullText.charAt(pos) == '"') {
			fill(colors, pos, end, DEFAULT_CONTENT_COLOR);
			return;
		}
		if (pos < end) {
			fill(colors, pos, end, DEFAULT_CONTENT_COLOR);
		}
	}

	private static void fill(int[] colors, int start, int end, int color) {
		for (int i = start; i < end; i++) {
			colors[i] = color;
		}
	}

	private Set<Character> getEnabledPrefixes() {
		Set<Character> prefixes = new HashSet<>();
		for (PrefixInfo<IListElementInfo<?>, IListElement<?>> info : completionProvider.getAllPrefixInfos()) {
			if (info.getMode() != SearchMode.DISABLED) {
				char prefix = info.getPrefix();
				if (prefix != '\0') {
					prefixes.add(prefix);
				}
			}
		}
		return prefixes;
	}
}
