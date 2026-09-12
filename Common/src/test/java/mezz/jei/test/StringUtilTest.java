package mezz.jei.test;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import mezz.jei.common.util.Pair;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Function;

public class StringUtilTest {
	private static final FontSet FONT_SET = new MonospaceFontSet();
	private static final Font FONT = new Font(resourceLocation -> FONT_SET, false);

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

	private static class MonospaceFontSet extends FontSet {
		private static final GlyphInfo GLYPH_INFO = new MonospaceGlyphInfo();

		public MonospaceFontSet() {
			super(null, ResourceLocation.fromNamespaceAndPath("jei", "test_font"));
		}

		@Override
		public GlyphInfo getGlyphInfo(int codepoint, boolean filterFishyGlyphs) {
			return GLYPH_INFO;
		}
	}

	private static class MonospaceGlyphInfo implements GlyphInfo {
		@Override
		public float getAdvance() {
			return 1;
		}

		@Override
		public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> glyphRenderer) {
			return null;
		}
	}
}
