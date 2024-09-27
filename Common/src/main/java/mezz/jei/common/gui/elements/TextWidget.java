package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.HorizontalAlignment;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import mezz.jei.common.util.VerticalAlignment;
import mezz.jei.core.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextWidget implements ITextWidget, IRecipeWidget {
	private final List<FormattedText> text;
	private final ImmutableRect2i area;

	private HorizontalAlignment horizontalAlignment;
	private VerticalAlignment verticalAlignment;
	private Font font;
	private int color;
	private boolean shadow;
	private int lineSpacing;
	private List<FormattedText> tooltipText = List.of();
	private @Nullable List<FormattedText> wrappedText;

	public TextWidget(List<FormattedText> text, int xPos, int yPos, int maxWidth, int maxHeight) {
		this.area = new ImmutableRect2i(xPos, yPos, maxWidth, maxHeight);
		Minecraft minecraft = Minecraft.getInstance();
		this.font = minecraft.font;
		this.color = 0xFF000000;
		this.text = text;
		this.lineSpacing = 2;
		this.horizontalAlignment = HorizontalAlignment.LEFT;
		this.verticalAlignment = VerticalAlignment.TOP;
	}

	@Override
	public ITextWidget alignHorizontalLeft() {
		this.horizontalAlignment = HorizontalAlignment.LEFT;
		return this;
	}

	@Override
	public ITextWidget alignHorizontalRight() {
		this.horizontalAlignment = HorizontalAlignment.RIGHT;
		return this;
	}

	@Override
	public ITextWidget alignHorizontalCenter() {
		this.horizontalAlignment = HorizontalAlignment.CENTER;
		return this;
	}

	@Override
	public ITextWidget alignVerticalTop() {
		this.verticalAlignment = VerticalAlignment.TOP;
		return this;
	}

	@Override
	public ITextWidget alignVerticalCenter() {
		this.verticalAlignment = VerticalAlignment.CENTER;
		return this;
	}

	@Override
	public ITextWidget alignVerticalBottom() {
		this.verticalAlignment = VerticalAlignment.BOTTOM;
		return this;
	}

	@Override
	public ITextWidget setFont(Font font) {
		this.font = font;
		return this;
	}

	@Override
	public ITextWidget setColor(int color) {
		this.color = color;
		return this;
	}

	@Override
	public ITextWidget setLineSpacing(int lineSpacing) {
		this.lineSpacing = lineSpacing;
		return this;
	}

	@Override
	public ITextWidget setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	@Override
	public ScreenPosition getPosition() {
		return area.getScreenPosition();
	}

	private List<FormattedText> calculateWrappedText() {
		if (wrappedText != null) {
			return wrappedText;
		}
		int lineHeight = getLineHeight();
		int maxLines = area.height() / lineHeight;
		if (maxLines * lineHeight + font.lineHeight <= area.height()) {
			maxLines++;
		}
		Pair<List<FormattedText>, Boolean> result = StringUtil.splitLines(font, text, area.width(), maxLines);
		this.wrappedText = result.first();
		boolean truncated = result.second();
		if (truncated) {
			this.tooltipText = text;
		} else {
			this.tooltipText = List.of();
		}
		return wrappedText;
	}

	private int getLineHeight() {
		return font.lineHeight + lineSpacing;
	}

	@Override
	public void drawWidget(GuiGraphics guiGraphics, double mouseX, double mouseY) {
		Language language = Language.getInstance();

		final int lineHeight = getLineHeight();
		List<FormattedText> lines = calculateWrappedText();
		int yPos = getYPosStart(lineHeight, lines);
		for (FormattedText line : lines) {
			FormattedCharSequence charSequence = language.getVisualOrder(line);
			int xPos = getXPos(charSequence);
			guiGraphics.drawString(font, charSequence, xPos, yPos, color, shadow);
			yPos += lineHeight;
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			guiGraphics.fill(0,0, area.width(), area.height(), 0xAAAAAA00);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (mouseX >= 0 && mouseX < area.width() && mouseY >= 0 && mouseY < area.height()) {
			calculateWrappedText();
			tooltip.addAll(tooltipText);
		}
	}

	private int getXPos(FormattedCharSequence text) {
		return switch (horizontalAlignment) {
			case LEFT -> 0;
			case RIGHT -> this.area.width() - font.width(text);
			case CENTER -> Math.round((this.area.width() - font.width(text)) / 2f);
		};
	}

	private int getYPosStart(int lineHeight, List<FormattedText> text) {
		if (verticalAlignment == VerticalAlignment.TOP) {
			return 0;
		}

		int linesHeight = (lineHeight * text.size()) - lineSpacing - 1;
		if (verticalAlignment == VerticalAlignment.BOTTOM) {
			return area.height() - linesHeight;
		} else if (verticalAlignment == VerticalAlignment.CENTER) {
			return Math.round((area.height() - linesHeight) / 2f);
		} else {
			throw new IllegalArgumentException("Unknown verticalAlignment " + verticalAlignment);
		}
	}
}
