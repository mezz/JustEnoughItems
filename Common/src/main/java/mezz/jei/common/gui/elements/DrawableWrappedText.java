package mezz.jei.common.gui.elements;

import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

public class DrawableWrappedText implements IDrawable {
	private static final int lineSpacing = 2;

	private final List<FormattedText> descriptionLines;
	private final int lineHeight;
	private final int width;
	private final int height;

	public DrawableWrappedText(List<FormattedText> text, int maxWidth) {
		Minecraft minecraft = Minecraft.getInstance();
		this.lineHeight = minecraft.font.lineHeight + lineSpacing;
		this.descriptionLines = StringUtil.splitLines(text, maxWidth);
		this.width = maxWidth;
		this.height = lineHeight * descriptionLines.size() - lineSpacing;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void draw(GuiGraphics guiGraphics, int xOffset, int yOffset) {
		Language language = Language.getInstance();
		Minecraft minecraft = Minecraft.getInstance();
		Font font = minecraft.font;

		int yPos = 0;
		for (FormattedText descriptionLine : descriptionLines) {
			FormattedCharSequence charSequence = language.getVisualOrder(descriptionLine);
			guiGraphics.drawString(font, charSequence, 0, yPos, 0xFF000000, false);
			yPos += lineHeight;
		}
	}
}
