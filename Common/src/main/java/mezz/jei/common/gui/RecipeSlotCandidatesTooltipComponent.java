package mezz.jei.common.gui;

import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.function.BooleanSupplier;

public final class RecipeSlotCandidatesTooltipComponent implements ClientTooltipComponent, TooltipComponent {
	private final FormattedCharSequence text;
	private final BooleanSupplier hideText;
	private boolean forceVisible;

	public RecipeSlotCandidatesTooltipComponent(String translationKey, IJeiKeyMapping keyMapping, BooleanSupplier hideText) {
		MutableComponent translatedKeyMessage = keyMapping.getTranslatedKeyMessage().copy();
		Component boldKeyMapping = translatedKeyMessage.withStyle(ChatFormatting.BOLD);
		this.text = Component.translatable(translationKey, boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY)
			.getVisualOrderText();
		this.hideText = hideText;
	}

	@Override
	public int getHeight(Font font) {
		// Always reserve the text dimensions so holding the key does not resize or move the tooltip.
		return 10;
	}

	@Override
	public int getWidth(Font font) {
		return font.width(this.text);
	}

	@Override
	public void extractText(GuiGraphicsExtractor guiGraphics, Font font, int x, int y) {
		if (this.forceVisible || !this.hideText.getAsBoolean()) {
			guiGraphics.text(font, this.text, x, y, -1, true);
		}
	}

	public void forceVisible() {
		this.forceVisible = true;
	}
}
