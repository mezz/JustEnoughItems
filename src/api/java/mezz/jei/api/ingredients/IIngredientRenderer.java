package mezz.jei.api.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;

import mezz.jei.api.recipe.IIngredientType;

/**
 * Renders a type of ingredient in JEI's item list and recipes.
 * <p>
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 *
 * @since JEI 3.11.0
 */
public interface IIngredientRenderer<T> {
	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param minecraft  The minecraft instance.
	 * @param xPosition  The x position to render the ingredient.
	 * @param yPosition  The y position to render the ingredient.
	 * @param ingredient the ingredient to render. May be null, some renderers (like fluid tanks) will render a
	 *                   background even if there is no ingredient.
	 */
	void render(Minecraft minecraft, int xPosition, int yPosition, @Nullable T ingredient);

	/**
	 * Get the tooltip text for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param minecraft   The minecraft instance.
	 * @param ingredient  The ingredient to get the tooltip for.
	 * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 * @since JEI 4.6.0
	 */
	default List<String> getTooltip(Minecraft minecraft, T ingredient, ITooltipFlag tooltipFlag) {
		// you should override this method. this default method is to keep old JEI plugins from crashing.
		return getTooltip(minecraft, ingredient, tooltipFlag == ITooltipFlag.TooltipFlags.ADVANCED);
	}

	/**
	 * Get the tooltip font renderer for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param minecraft  The minecraft instance.
	 * @param ingredient The ingredient to get the tooltip for.
	 * @return The font renderer for the ingredient.
	 */
	default FontRenderer getFontRenderer(Minecraft minecraft, T ingredient) {
		return minecraft.fontRenderer;
	}

	// DEPRECATED BELOW

	/**
	 * Get the tooltip text for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param minecraft  The minecraft instance.
	 * @param ingredient The ingredient to get the tooltip for.
	 * @param advanced   Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 * @since JEI 4.3.6
	 * @deprecated since JEI 4.6.0.
	 */
	@Deprecated
	default List<String> getTooltip(Minecraft minecraft, T ingredient, boolean advanced) {
		return getTooltip(minecraft, ingredient);
	}

	/**
	 * Get the tooltip text for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param minecraft  The minecraft instance.
	 * @param ingredient The ingredient to get the tooltip for.
	 * @return The tooltip text for the ingredient.
	 * @deprecated since JEI 4.3.6, use {@link #getTooltip(Minecraft, T, ITooltipFlag)}
	 */
	@Deprecated
	default List<String> getTooltip(Minecraft minecraft, T ingredient) {
		throw new UnsupportedOperationException("Calling old getTooltip is not supported.");
	}
}
