package mezz.jei.test;

import mezz.jei.common.util.Pair;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class StringUtilTest {
	private static final Font FONT = new MonospaceFont();

	@Test
	public void splitLinesWrapsAtSpacesBeforeHyphenating() {
		Pair<List<FormattedText>, Boolean> result = StringUtil.splitLines(
			FONT,
			List.of(FormattedText.of("alpha beta gamma")),
			10,
			5
		);

		Assertions.assertFalse(result.second());
		Assertions.assertEquals(List.of("alpha beta", "gamma"), getStrings(result));
	}

	@Test
	public void splitLinesHyphenatesLongWordsWithLongestFittingSegments() {
		Pair<List<FormattedText>, Boolean> result = StringUtil.splitLines(
			FONT,
			List.of(FormattedText.of("alpha supercalifragilistic beta")),
			10,
			10
		);

		Assertions.assertFalse(result.second());
		Assertions.assertEquals(
			List.of("alpha", "supercali-", "fragilist-", "ic", "beta"),
			getStrings(result)
		);
	}

	@Test
	public void splitLinesEllipsizesWhenHyphenatedTextExceedsMaxLines() {
		Pair<List<FormattedText>, Boolean> result = StringUtil.splitLines(
			FONT,
			List.of(FormattedText.of("alpha supercalifragilistic beta")),
			10,
			2
		);

		Assertions.assertTrue(result.second());
		Assertions.assertEquals(List.of("alpha", "superca..."), getStrings(result));
	}

	private static List<String> getStrings(Pair<List<FormattedText>, Boolean> result) {
		return result.first()
			.stream()
			.map(FormattedText::getString)
			.toList();
	}

	private static class MonospaceFont extends Font {
		private MonospaceFont() {
			super(null);
		}

		@Override
		public int width(String text) {
			return text.length();
		}

		@Override
		public int width(FormattedText text) {
			return text.getString().length();
		}

		@Override
		public FormattedText substrByWidth(FormattedText text, int width) {
			String string = text.getString();
			return FormattedText.of(string.substring(0, Math.min(width, string.length())));
		}
	}
}
