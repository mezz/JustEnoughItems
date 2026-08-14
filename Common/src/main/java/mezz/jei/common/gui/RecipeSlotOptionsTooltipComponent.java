package mezz.jei.common.gui;

import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;

public final class RecipeSlotOptionsTooltipComponent implements ClientTooltipComponent, TooltipComponent {
	private static final int LINE_HEIGHT = 10;

	private final Component text;

	public RecipeSlotOptionsTooltipComponent(IJeiKeyMapping keyMapping) {
		Component boldKeyMapping = keyMapping.getTranslatedKeyMessage()
			.copy()
			.withStyle(ChatFormatting.BOLD);
		this.text = Component.translatable("jei.tooltip.recipe.slot.options", boldKeyMapping)
			.withStyle(ChatFormatting.ITALIC)
			.withStyle(ChatFormatting.GRAY);
	}

	private List<FormattedCharSequence> getLines(Font font) {
		return font.split(this.text, IngredientGridTooltipComponent.getMaximumWidth());
	}

	@Override
	public int getHeight(Font font) {
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
	public void renderText(GuiGraphics guiGraphics, Font font, int x, int y) {
		List<FormattedCharSequence> lines = getLines(font);
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			guiGraphics.drawString(font, line, x, y + (i * LINE_HEIGHT), -1, true);
		}
	}
}
