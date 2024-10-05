package mezz.jei.gui.recipes;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.MathUtil;
import mezz.jei.common.util.StringUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;

public class RecipeCategoryTitle {
	private final FormattedCharSequence visibleString;
	private final @Nullable Component tooltipString;
	private final ImmutableRect2i area;

	public static RecipeCategoryTitle create(IRecipeCategory<?> recipeCategory, Font font, ImmutableRect2i availableArea) {
		Component fullString = StringUtil.stripStyling(recipeCategory.getTitle());
		FormattedCharSequence visibleString;
		@Nullable Component tooltipString;

		final int availableTitleWidth = availableArea.getWidth();
		if (font.width(fullString) > availableTitleWidth) {
			FormattedText formattedText = StringUtil.truncateStringToWidth(fullString, availableTitleWidth, font);
			visibleString = Language.getInstance().getVisualOrder(formattedText);
			tooltipString = fullString;
		} else {
			visibleString = fullString.getVisualOrderText();
			tooltipString = null;
		}

		ImmutableRect2i area = MathUtil.centerTextArea(availableArea, font, visibleString);
		return new RecipeCategoryTitle(visibleString, tooltipString, area);
	}

	public RecipeCategoryTitle() {
		this(FormattedCharSequence.EMPTY, Component.empty(), ImmutableRect2i.EMPTY);
	}

	public RecipeCategoryTitle(FormattedCharSequence visibleString, @Nullable Component tooltipString, ImmutableRect2i area) {
		this.visibleString = visibleString;
		this.tooltipString = tooltipString;
		this.area = area;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public void getTooltip(JeiTooltip tooltip) {
		if (tooltipString != null) {
			tooltip.add(tooltipString);
		}
	}

	public void draw(GuiGraphics guiGraphics, Font font) {
		StringUtil.drawCenteredStringWithShadow(guiGraphics, font, visibleString, area);
	}
}
