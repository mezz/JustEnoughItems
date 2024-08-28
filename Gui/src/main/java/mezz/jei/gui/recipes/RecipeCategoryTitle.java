package mezz.jei.gui.recipes;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RecipeCategoryTitle {
	private final Component visibleString;
	private final Component fullString;
	private final ImmutableRect2i area;

	public static RecipeCategoryTitle create(IRecipeCategory<?> recipeCategory, Font font, ImmutableRect2i availableArea) {
		Component fullString = StringUtil.stripStyling(recipeCategory.getTitle());
		Component visibleString;

		final int availableTitleWidth = availableArea.getWidth();
		if (font.width(fullString) > availableTitleWidth) {
			visibleString = StringUtil.truncateStringToWidth(fullString, availableTitleWidth, font);
		} else {
			visibleString = fullString;
		}

		ImmutableRect2i area = MathUtil.centerTextArea(availableArea, font, visibleString);
		return new RecipeCategoryTitle(visibleString, fullString, area);
	}

	public RecipeCategoryTitle() {
		this(Component.empty(), Component.empty(), ImmutableRect2i.EMPTY);
	}

	public RecipeCategoryTitle(Component visibleString, Component fullString, ImmutableRect2i area) {
		this.visibleString = visibleString;
		this.fullString = fullString;
		this.area = area;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public void getTooltip(JeiTooltip tooltip) {
		if (!visibleString.equals(fullString)) {
			tooltip.add(fullString);
		}
	}

	public void draw(GuiGraphics guiGraphics, Font font) {
		StringUtil.drawCenteredStringWithShadow(guiGraphics, font, visibleString, area);
	}
}
