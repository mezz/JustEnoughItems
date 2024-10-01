package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
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
	private ImmutableRect2i availableArea;

	private HorizontalAlignment horizontalAlignment;
	private VerticalAlignment verticalAlignment;
	private Font font;
	private int color;
	private boolean shadow;
	private int lineSpacing;

	private @Nullable List<FormattedText> wrappedText;
	private boolean truncated = false;

	public TextWidget(List<FormattedText> text, int xPos, int yPos, int maxWidth, int maxHeight) {
		this.availableArea = new ImmutableRect2i(xPos, yPos, maxWidth, maxHeight);
		Minecraft minecraft = Minecraft.getInstance();
		this.font = minecraft.font;
		this.color = 0xFF000000;
		this.text = text;
		this.lineSpacing = 2;
		this.horizontalAlignment = HorizontalAlignment.LEFT;
		this.verticalAlignment = VerticalAlignment.TOP;
	}

	private void invalidateCachedValues() {
		wrappedText = null;
		truncated = false;
	}

	@Override
	public int getWidth() {
		return availableArea.width();
	}

	@Override
	public int getHeight() {
		return availableArea.height();
	}

	@Override
	public TextWidget setPosition(int xPos, int yPos) {
		this.availableArea = this.availableArea.setPosition(xPos, yPos);
		invalidateCachedValues();
		return this;
	}

	@Override
	public TextWidget setTextAlignment(HorizontalAlignment horizontalAlignment) {
		if (this.horizontalAlignment.equals(horizontalAlignment)) {
			return this;
		}
		this.horizontalAlignment = horizontalAlignment;
		invalidateCachedValues();
		return this;
	}

	@Override
	public TextWidget setTextAlignment(VerticalAlignment verticalAlignment) {
		if (this.verticalAlignment.equals(verticalAlignment)) {
			return this;
		}
		this.verticalAlignment = verticalAlignment;
		invalidateCachedValues();
		return this;
	}

	@Override
	public ITextWidget setFont(Font font) {
		this.font = font;
		invalidateCachedValues();
		return this;
	}

	@Override
	public ITextWidget setColor(int color) {
		this.color = color;
		invalidateCachedValues();
		return this;
	}

	@Override
	public ITextWidget setLineSpacing(int lineSpacing) {
		this.lineSpacing = lineSpacing;
		invalidateCachedValues();
		return this;
	}

	@Override
	public ITextWidget setShadow(boolean shadow) {
		this.shadow = shadow;
		invalidateCachedValues();
		return this;
	}

	@Override
	public ScreenPosition getPosition() {
		return availableArea.getScreenPosition();
	}

	private List<FormattedText> calculateWrappedText() {
		if (wrappedText != null) {
			return wrappedText;
		}
		int lineHeight = getLineHeight();
		int maxLines = availableArea.height() / lineHeight;
		if (maxLines * lineHeight + font.lineHeight <= availableArea.height()) {
			maxLines++;
		}
		Pair<List<FormattedText>, Boolean> result = StringUtil.splitLines(font, text, availableArea.width(), maxLines);
		this.wrappedText = result.first();
		this.truncated = result.second();
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
			guiGraphics.fill(0,0, availableArea.width(), availableArea.height(), 0xAAAAAA00);
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (mouseX >= 0 && mouseX < availableArea.width() && mouseY >= 0 && mouseY < availableArea.height()) {
			calculateWrappedText();
			if (truncated) {
				tooltip.addAll(text);
			}
		}
	}

	private int getXPos(FormattedCharSequence text) {
		return getXPos(font.width(text));
	}

	private int getXPos(int lineWidth) {
		return horizontalAlignment.getXPos(this.availableArea.width(), lineWidth);
	}

	private int getYPosStart(int lineHeight, List<FormattedText> text) {
		int linesHeight = (lineHeight * text.size()) - lineSpacing - 1;
		return verticalAlignment.getYPos(this.availableArea.height(), linesHeight);
	}
}
