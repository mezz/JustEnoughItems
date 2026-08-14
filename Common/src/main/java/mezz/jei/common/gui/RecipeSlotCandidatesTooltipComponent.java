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

import java.util.List;
import java.util.function.BooleanSupplier;

public final class RecipeSlotCandidatesTooltipComponent implements ClientTooltipComponent, TooltipComponent {
	private static final int LINE_HEIGHT = 10;

	private final Component text;
	private final BooleanSupplier hideText;
	private boolean forceVisible;

	public RecipeSlotCandidatesTooltipComponent(IJeiKeyMapping keyMapping, BooleanSupplier hideText) {
		MutableComponent translatedKeyMessage = keyMapping.getTranslatedKeyMessage().copy();
		Component boldKeyMapping = translatedKeyMessage.withStyle(ChatFormatting.BOLD);
		this.text = Component.translatable("jei.tooltip.recipe.slot.options", boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY);
		this.hideText = hideText;
	}

	private List<FormattedCharSequence> getLines(Font font) {
		return font.split(this.text, IngredientGridTooltipComponent.getMaximumWidth());
	}

	@Override
	public int getHeight(Font font) {
		// Always reserve the text dimensions so holding the key does not resize or move the tooltip.
		return getLines(font).size() * LINE_HEIGHT;
	}

	@Override
	public int getWidth(Font font) {
		return getLines(font).stream()
			.mapToInt(font::width)
			.max()
			.orElse(0);
	}

	@Override
	public void extractText(GuiGraphicsExtractor guiGraphics, Font font, int x, int y) {
		if (this.forceVisible || !this.hideText.getAsBoolean()) {
			List<FormattedCharSequence> lines = getLines(font);
			for (int i = 0; i < lines.size(); i++) {
				FormattedCharSequence line = lines.get(i);
				guiGraphics.text(font, line, x, y + (i * LINE_HEIGHT), -1, true);
			}
		}
	}

	public void forceVisible() {
		this.forceVisible = true;
	}
}
