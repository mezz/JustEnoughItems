package mezz.jei.gui.search;

import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.search.PrefixInfo;
import mezz.jei.common.search.SearchMode;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.input.GuiTextFieldFilter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class SearchCompletionOverlay {
	private static final int LINE_HEIGHT = 11;
	private static final int MIN_ROW_HEIGHT = 14;
	private static final int ROW_SPACING = 2;
	private static final int SYMBOL_COLUMN_WIDTH = 20;
	private static final int PADDING = 3;
	private static final int BORDER = 1;
	private static final int MAX_OVERLAY_WIDTH = 200;
	private static final int MAX_OVERLAY_WIDTH_DYNAMIC = 320;
	private static final int MAX_VISIBLE_ROWS = 9;
	private static final int MAX_DYNAMIC_CANDIDATES = 100;
	private static final int SCREEN_MARGIN = 4;
	private static final int SELECTED_COLOR = 0xFF4040A0;
	private static final int SYMBOL_COLOR = 0xFFFFFF00;
	private static final int DYNAMIC_SYMBOL_COLOR = 0xFF80FF80;
	private static final int DESCRIPTION_COLOR = 0xFFAAAAAA;
	private static final int SCROLLBAR_WIDTH = 2;
	private static final int SCROLLBAR_TRACK_COLOR = 0xFF404040;
	private static final int SCROLLBAR_THUMB_COLOR = 0xFF808080;

	private final ISearchCompletionProvider completionProvider;
	private final ScalableDrawable background;

	private final List<CompletionCandidate> filteredCandidates = new ArrayList<>();
	private boolean visible = false;
	private int selectedIndex = 0;
	private int scrollOffset = 0;
	private @Nullable ImmutableRect2i overlayArea;
	private @Nullable ImmutableRect2i searchFieldArea;

	private final List<RowLayout> rowLayouts = new ArrayList<>();

	public SearchCompletionOverlay(ISearchCompletionProvider completionProvider, ScalableDrawable background) {
		this.completionProvider = completionProvider;
		this.background = background;
	}

	public void render(GuiGraphicsExtractor guiGraphics, String text, int cursorPos, int mouseX, int mouseY) {
		update(text, cursorPos);
		draw(guiGraphics, mouseX, mouseY);
	}

	private void update(String text, int cursorPos) {
		List<Token> tokens = SearchTokenizer.tokenize(text);
		String currentToken = extractCurrentToken(tokens, text, cursorPos);
		boolean hasPredicateBefore = hasPredicateBefore(tokens, cursorPos);
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> prefixInfos = completionProvider.getAllPrefixInfos();
		Collection<IListElementInfo<?>> elementInfos = completionProvider.getAllElementInfos();

		filteredCandidates.clear();
		if (!currentToken.isEmpty()) {
			PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo = findPrefixInfo(prefixInfos, currentToken.charAt(0));
			if (prefixInfo != null && prefixInfo.getMode() != SearchMode.DISABLED) {
				if (prefixInfo.supportsDynamicCompletion()) {
					addDynamicCandidates(prefixInfo, currentToken, elementInfos);
				} else {
					addStaticCandidates(prefixInfos, currentToken, hasPredicateBefore);
				}
			} else {
				addStaticCandidates(prefixInfos, currentToken, hasPredicateBefore);
				addNoPrefixDynamicCandidates(prefixInfos, currentToken, elementInfos);
			}
		} else {
			addStaticCandidates(prefixInfos, currentToken, hasPredicateBefore);
		}

		if (filteredCandidates.isEmpty()) {
			visible = false;
			return;
		}
		visible = true;
		if (selectedIndex >= filteredCandidates.size()) {
			selectedIndex = 0;
		}
	}

	private @Nullable PrefixInfo<IListElementInfo<?>, IListElement<?>> findPrefixInfo(
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> prefixInfos,
		char prefix
	) {
		for (PrefixInfo<IListElementInfo<?>, IListElement<?>> info : prefixInfos) {
			if (info.getPrefix() == prefix) {
				return info;
			}
		}
		return null;
	}

	private void addStaticCandidates(
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> prefixInfos,
		String currentToken,
		boolean hasPredicateBefore
	) {
		for (PrefixInfo<IListElementInfo<?>, IListElement<?>> info : prefixInfos) {
			if (info.getMode() == SearchMode.DISABLED) {
				continue;
			}
			char prefix = info.getPrefix();
			if (prefix == '\0') {
				continue;
			}
			String prefixStr = String.valueOf(prefix);
			if (prefixStr.startsWith(currentToken)) {
				filteredCandidates.add(new CompletionCandidate(prefixStr, prefixStr, info.getDescription(), CandidateCategory.PREFIX));
			}
		}

		addNotOperatorIfMatches(currentToken);
		if (hasPredicateBefore && !currentToken.equals("|")) {
			filteredCandidates.add(new CompletionCandidate("|", "|", Component.translatable("jei.search.completion.operator.or"), CandidateCategory.OPERATOR));
		}
	}

	private void addNotOperatorIfMatches(String currentToken) {
		String symbol = "-";
		if (symbol.startsWith(currentToken)) {
			filteredCandidates.add(new CompletionCandidate(symbol, symbol, Component.translatable("jei.search.completion.operator.not"), CandidateCategory.OPERATOR));
		}
	}

	private void addDynamicCandidates(
		PrefixInfo<IListElementInfo<?>, IListElement<?>> prefixInfo,
		String currentToken,
		Collection<IListElementInfo<?>> elementInfos
	) {
		String query = stripMatchingQuotes(currentToken.substring(1));
		String queryLower = query.toLowerCase(Locale.ENGLISH);
		Set<String> uniqueStrings = new TreeSet<>();
		for (IListElementInfo<?> info : elementInfos) {
			uniqueStrings.addAll(prefixInfo.getStrings(info));
		}

		String prefixStr = String.valueOf(prefixInfo.getPrefix());
		boolean quoteWrapper = currentToken.length() > 1 && currentToken.charAt(1) == '"';
		int count = 0;
		for (String s : uniqueStrings) {
			if (count >= MAX_DYNAMIC_CANDIDATES) {
				break;
			}
			if (s.toLowerCase(Locale.ENGLISH).startsWith(queryLower)) {
				String insertion;
				if (quoteWrapper) {
					insertion = prefixStr + '"' + s + '"';
				} else {
					insertion = prefixStr + s;
				}
				if (insertion.equals(currentToken)) {
					continue;
				}
				filteredCandidates.add(new CompletionCandidate(insertion, s, prefixInfo.getDescription(), CandidateCategory.DYNAMIC));
				count++;
			}
		}
	}

	private void addNoPrefixDynamicCandidates(
		Collection<PrefixInfo<IListElementInfo<?>, IListElement<?>>> prefixInfos,
		String currentToken,
		Collection<IListElementInfo<?>> elementInfos
	) {
		String query = stripMatchingQuotes(currentToken);
		String queryLower = query.toLowerCase(Locale.ENGLISH);
		boolean quoteWrapper = currentToken.startsWith("\"");
		Map<String, CompletionCandidate> uniqueCandidates = new TreeMap<>();
		outer : for (PrefixInfo<IListElementInfo<?>, IListElement<?>> info : prefixInfos) {
			if (info.getMode() != SearchMode.ENABLED || !info.supportsDynamicCompletion()) {
				continue;
			}
			Set<String> strings = new TreeSet<>();
			for (IListElementInfo<?> elementInfo : elementInfos) {
				strings.addAll(info.getStrings(elementInfo));
			}
			for (String s : strings) {
				if (uniqueCandidates.size() >= MAX_DYNAMIC_CANDIDATES) {
					break outer;
				}
				if (s.toLowerCase(Locale.ENGLISH).startsWith(queryLower)) {
					String insertion;
					if (quoteWrapper) {
						insertion = '"' + s + '"';
					} else {
						insertion = s;
					}
					uniqueCandidates.computeIfAbsent(s, ignored -> new CompletionCandidate(insertion, s, info.getDescription(), CandidateCategory.DYNAMIC));
				}
			}
		}
		filteredCandidates.addAll(uniqueCandidates.values());
	}

	private static int findTokenBoundary(List<Token> tokens, int cursorPos, boolean start) {
		for (Token token : tokens) {
			if (cursorPos >= token.start() && cursorPos <= token.end()) {
				if (start) {
					return token.start();
				}
				return token.end();
			}
		}
		return cursorPos;
	}

	private static String extractCurrentToken(List<Token> tokens, String text, int cursorPos) {
		if (cursorPos <= 0) {
			return "";
		}
		int start = findTokenBoundary(tokens, cursorPos, true);
		String token = text.substring(start, cursorPos);
		if (token.startsWith("-") && token.length() > 1) {
			token = token.substring(1);
		}
		return token;
	}

	private static boolean hasPredicateBefore(List<Token> tokens, int cursorPos) {
		boolean foundPredicate = false;
		for (Token token : tokens) {
			if (cursorPos >= token.start() && cursorPos < token.end()) {
				return foundPredicate;
			}
			if (!token.operator()) {
				foundPredicate = true;
			}
		}
		return foundPredicate;
	}

	private static String stripMatchingQuotes(String s) {
		if (s.length() >= 2 && s.startsWith("\"") && s.endsWith("\"")) {
			return s.substring(1, s.length() - 1);
		}
		if (s.startsWith("\"")) {
			return s.substring(1);
		}
		return s;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return visible && overlayArea != null && overlayArea.contains(mouseX, mouseY);
	}

	public void close() {
		visible = false;
	}

	public void moveSelection(int delta) {
		if (filteredCandidates.isEmpty()) {
			return;
		}
		int count = filteredCandidates.size();
		selectedIndex = Math.floorMod(selectedIndex + delta, count);
	}

	public void accept(GuiTextFieldFilter searchField) {
		if (filteredCandidates.isEmpty() || selectedIndex < 0 || selectedIndex >= filteredCandidates.size()) {
			return;
		}
		CompletionCandidate candidate = filteredCandidates.get(selectedIndex);
		String text = searchField.getValue();
		int cursorPos = searchField.getCursorPosition();

		List<Token> tokens = SearchTokenizer.tokenize(text);
		int tokenStart;
		int tokenEnd;
		if (candidate.category() == CandidateCategory.OPERATOR) {
			tokenStart = cursorPos;
			tokenEnd = cursorPos;
		} else {
			tokenStart = findTokenBoundary(tokens, cursorPos, true);
			tokenEnd = findTokenBoundary(tokens, cursorPos, false);
		}

		String insertion = candidate.insertion();
		if (candidate.category() != CandidateCategory.OPERATOR &&
			tokenStart < text.length() && text.charAt(tokenStart) == '-'
		) {
			insertion = "-" + insertion;
		}

		String newText = text.substring(0, tokenStart) + insertion + text.substring(tokenEnd);
		int newCursorPos = tokenStart + insertion.length();

		searchField.setValue(newText);
		searchField.setCursorPosition(newCursorPos);

		if (candidate.category() == CandidateCategory.DYNAMIC || candidate.category() == CandidateCategory.OPERATOR) {
			close();
		} else {
			update(newText, newCursorPos);
		}
	}

	public boolean handleMouseClicked(double mouseX, double mouseY, GuiTextFieldFilter searchField) {
		if (!visible || overlayArea == null) {
			return false;
		}
		if (!overlayArea.contains(mouseX, mouseY)) {
			return false;
		}
		int overlayY = overlayArea.getY();
		for (int i = 0; i < rowLayouts.size(); i++) {
			RowLayout row = rowLayouts.get(i);
			int rowY = overlayY + BORDER + row.y();
			if (mouseY >= rowY && mouseY < rowY + row.height()) {
				selectedIndex = scrollOffset + i;
				accept(searchField);
				return true;
			}
		}
		return false;
	}

	public void updateBounds(ImmutableRect2i searchFieldArea) {
		this.searchFieldArea = searchFieldArea;
	}

	private int calculateRowLayouts(int visibleRowCount, int descAreaWidth, Font font) {
		rowLayouts.clear();
		int totalHeight = 0;
		for (int i = 0; i < visibleRowCount; i++) {
			int candidateIndex = scrollOffset + i;
			if (candidateIndex >= filteredCandidates.size()) {
				break;
			}
			CompletionCandidate candidate = filteredCandidates.get(candidateIndex);
			int rowHeight = MIN_ROW_HEIGHT;
			if (candidate.category() != CandidateCategory.DYNAMIC) {
				Component description = candidate.description();
				List<FormattedText> wrappedLines = wrapDescription(font, description, descAreaWidth);
				rowHeight = Math.max(MIN_ROW_HEIGHT, wrappedLines.size() * LINE_HEIGHT + ROW_SPACING);
				rowLayouts.add(new RowLayout(totalHeight, rowHeight, wrappedLines));
			} else {
				rowLayouts.add(new RowLayout(totalHeight, rowHeight, List.of()));
			}
			totalHeight += rowHeight;
		}
		return totalHeight;
	}

	private static List<FormattedText> wrapDescription(Font font, Component description, int maxWidth) {
		if (maxWidth <= 0) {
			return List.of(description);
		}
		return font.getSplitter().splitLines(description, maxWidth, Style.EMPTY);
	}

	private int calculateOverlayWidth(Font font, int screenWidth) {
		int fieldWidth;
		if (searchFieldArea != null) {
			fieldWidth = searchFieldArea.getWidth();
		} else {
			fieldWidth = 120;
		}

		boolean hasDynamic = filteredCandidates.stream().anyMatch(c -> c.category() == CandidateCategory.DYNAMIC);
		int maxWidth;
		if (hasDynamic) {
			maxWidth = Math.min(MAX_OVERLAY_WIDTH_DYNAMIC, screenWidth);
		} else {
			maxWidth = Math.min(MAX_OVERLAY_WIDTH, screenWidth);
		}
		int minContentWidth = fieldWidth;

		for (CompletionCandidate candidate : filteredCandidates) {
			int symbolW = font.width(candidate.displayName());
			int descW = 0;
			if (candidate.category() != CandidateCategory.DYNAMIC) {
				descW = font.width(candidate.description());
			}
			int rowWidth = PADDING + symbolW + PADDING + descW + PADDING;
			if (rowWidth > minContentWidth) {
				minContentWidth = rowWidth;
			}
		}
		return Math.min(minContentWidth, maxWidth);
	}

	private void draw(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (!visible || searchFieldArea == null || filteredCandidates.isEmpty()) {
			overlayArea = null;
			rowLayouts.clear();
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		int screenHeight = minecraft.getWindow().getGuiScaledHeight();

		int overlayWidth = calculateOverlayWidth(font, screenWidth);
		int descAreaWidth = overlayWidth - PADDING * 3 - SYMBOL_COLUMN_WIDTH;

		int availableHeightAbove = searchFieldArea.getY() - SCREEN_MARGIN;
		int availableHeightBelow = screenHeight - (searchFieldArea.getY() + searchFieldArea.getHeight()) - SCREEN_MARGIN;
		int maxAvailableHeight = Math.max(availableHeightAbove, availableHeightBelow);
		int maxRowsByHeight = (maxAvailableHeight - BORDER * 2) / MIN_ROW_HEIGHT;
		int visibleRowCount = Math.min(Math.min(MAX_VISIBLE_ROWS, maxRowsByHeight), filteredCandidates.size());
		if (visibleRowCount <= 0) {
			overlayArea = null;
			rowLayouts.clear();
			return;
		}

		int maxScroll = Math.max(0, filteredCandidates.size() - visibleRowCount);
		if (scrollOffset > maxScroll) {
			scrollOffset = maxScroll;
		}
		if (selectedIndex < scrollOffset) {
			scrollOffset = selectedIndex;
		} else if (selectedIndex >= scrollOffset + visibleRowCount) {
			scrollOffset = selectedIndex - visibleRowCount + 1;
		}

		int totalHeight = calculateRowLayouts(visibleRowCount, descAreaWidth, font);
		int overlayHeight = totalHeight + BORDER * 2;

		int x = searchFieldArea.getX();
		if (x + overlayWidth > screenWidth - SCREEN_MARGIN) {
			x = screenWidth - overlayWidth - SCREEN_MARGIN;
		}
		if (x < SCREEN_MARGIN) {
			x = SCREEN_MARGIN;
		}

		int y = searchFieldArea.getY() - overlayHeight;
		if (y < 0) {
			y = searchFieldArea.getY() + searchFieldArea.getHeight();
			if (y + overlayHeight > screenHeight - SCREEN_MARGIN) {
				int availableBelow = screenHeight - SCREEN_MARGIN - y - BORDER * 2;
				int fitRows = availableBelow / MIN_ROW_HEIGHT;
				if (fitRows <= 0) {
					overlayArea = null;
					rowLayouts.clear();
					return;
				}
				visibleRowCount = Math.min(fitRows, visibleRowCount);
				totalHeight = calculateRowLayouts(visibleRowCount, descAreaWidth, font);
				overlayHeight = totalHeight + BORDER * 2;
			}
		}

		overlayArea = new ImmutableRect2i(x, y, overlayWidth, overlayHeight);
		background.draw(guiGraphics, overlayArea);

		for (int i = 0; i < rowLayouts.size(); i++) {
			int candidateIndex = scrollOffset + i;
			if (candidateIndex >= filteredCandidates.size()) {
				break;
			}
			CompletionCandidate candidate = filteredCandidates.get(candidateIndex);
			RowLayout row = rowLayouts.get(i);
			int rowY = y + BORDER + row.y();
			boolean selected = (candidateIndex == selectedIndex);
			boolean hovered = overlayArea.contains(mouseX, mouseY) &&
				mouseY >= rowY && mouseY < rowY + row.height();

			if (selected || hovered) {
				guiGraphics.fill(x + BORDER, rowY, x + overlayWidth - BORDER, rowY + row.height(), SELECTED_COLOR);
			}

			String symbol = candidate.displayName();
			int symbolY = rowY + (row.height() - LINE_HEIGHT) / 2 + 1;
			int symbolColor;
			if (candidate.category() == CandidateCategory.DYNAMIC) {
				symbolColor = DYNAMIC_SYMBOL_COLOR;
			} else {
				symbolColor = SYMBOL_COLOR;
			}
			int maxSymbolWidth = overlayWidth - PADDING * 2;
			String truncatedSymbol = font.plainSubstrByWidth(symbol, maxSymbolWidth);
			guiGraphics.text(font, truncatedSymbol, x + PADDING, symbolY, symbolColor, true);

			if (candidate.category() != CandidateCategory.DYNAMIC && !row.wrappedLines().isEmpty()) {
				int rightEdge = x + overlayWidth - PADDING;
				Language language = Language.getInstance();
				int descY = rowY + (row.height() - row.wrappedLines().size() * LINE_HEIGHT) / 2 + 1;
				for (FormattedText line : row.wrappedLines()) {
					FormattedCharSequence charSequence = language.getVisualOrder(line);
					int lineWidth = font.width(charSequence);
					int descX = rightEdge - lineWidth;
					guiGraphics.text(font, charSequence, descX, descY, DESCRIPTION_COLOR, true);
					descY += LINE_HEIGHT;
				}
			}
		}

		if (filteredCandidates.size() > visibleRowCount) {
			int scrollbarX = x + overlayWidth - BORDER - SCROLLBAR_WIDTH;
			int scrollbarY = y + BORDER;
			int scrollbarH = overlayHeight - BORDER * 2;
			guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + SCROLLBAR_WIDTH, scrollbarY + scrollbarH, SCROLLBAR_TRACK_COLOR);
			double ratio = (double) scrollOffset / (filteredCandidates.size() - visibleRowCount);
			int thumbH = Math.max(MIN_ROW_HEIGHT, (int) ((double) scrollbarH * visibleRowCount / filteredCandidates.size()));
			int thumbY = scrollbarY + (int) ((scrollbarH - thumbH) * ratio);
			guiGraphics.fill(scrollbarX, thumbY, scrollbarX + SCROLLBAR_WIDTH, thumbY + thumbH, SCROLLBAR_THUMB_COLOR);
		}
	}

	private record RowLayout(int y, int height, List<FormattedText> wrappedLines) {}
}
