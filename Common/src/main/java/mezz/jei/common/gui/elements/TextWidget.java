package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.placement.VerticalAlignment;
import mezz.jei.api.gui.widgets.ITextWidget;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import mezz.jei.common.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TextWidget extends AbstractRecipeWidgetBuilder<ITextWidget> implements ITextWidget {
	private final List<FormattedText> text;
	private ImmutableRect2i availableArea;

	private HorizontalAlignment horizontalAlignment;
	private VerticalAlignment verticalAlignment;
	private Font font;
	private @Nullable Integer colorOverride;
	private boolean shadow;
	private int lineSpacing;

	private @Nullable List<FormattedText> wrappedText;
	private boolean truncated = false;

	public TextWidget(List<FormattedText> text, int xPos, int yPos, int maxWidth, int maxHeight) {
		this(text, xPos, yPos, maxWidth, maxHeight, Minecraft.getInstance().font);
	}

	TextWidget(List<FormattedText> text, int xPos, int yPos, int maxWidth, int maxHeight, Font font) {
		super(xPos, yPos);
		this.availableArea = new ImmutableRect2i(0, 0, maxWidth, maxHeight);
		this.font = font;
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
	protected ITextWidget getThis() {
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
		this.colorOverride = color;
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
		Integer colorOverride = this.colorOverride;
		int color;
		if (colorOverride == null) {
			color = JeiGuiColors.getColor(GuiColor.RECIPE_TEXT_WIDGET_TEXT);
		} else {
			color = colorOverride;
		}
		for (FormattedText line : lines) {
			FormattedCharSequence charSequence = language.getVisualOrder(line);
			int xPos = getXPos(charSequence);
			guiGraphics.drawString(font, charSequence, xPos, yPos, color, shadow);
			yPos += lineHeight;
		}

		if (DebugConfig.isDebugGuisEnabled()) {
			guiGraphics.fill(0, 0, availableArea.width(), availableArea.height(), JeiGuiColors.getColor(GuiColor.DEBUG_WIDGET_AREA));
		}
	}

	@Override
	public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
		if (!isMouseOver(mouseX, mouseY)) {
			return;
		}
		calculateWrappedText();
		if (truncated) {
			tooltip.addAll(text);
		}
		if (hasConfiguredTooltip()) {
			addConfiguredTooltip(tooltip);
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
