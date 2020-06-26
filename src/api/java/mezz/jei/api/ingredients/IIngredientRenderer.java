package mezz.jei.api.ingredients;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;

import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.util.text.ITextComponent;

/**
 * Renders a type of ingredient in JEI's item list and recipes.
 *
 * If you have a new type of ingredient to add to JEI, you will have to implement this in order to use
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 */
public interface IIngredientRenderer<T> {
	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param xPosition  The x position to render the ingredient.
	 * @param yPosition  The y position to render the ingredient.
	 * @param ingredient the ingredient to render. May be null, some renderers (like fluid tanks) will render a
	 */
	void render(MatrixStack matrixStack, int xPosition, int yPosition, @Nullable T ingredient);

	/**
	 * Get the tooltip text for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param ingredient  The ingredient to get the tooltip for.
	 * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 */
	List<ITextComponent> getTooltip(T ingredient, ITooltipFlag tooltipFlag);

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
}
