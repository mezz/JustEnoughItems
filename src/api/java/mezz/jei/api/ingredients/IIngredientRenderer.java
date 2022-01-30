package mezz.jei.api.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.item.TooltipFlag;

import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.network.chat.Component;

/**
 * Renders a type of ingredient in JEI's item list and recipes.
 *
 * If you have a new type of ingredient to add to JEI,
 * you will have to implement this to create a default renderer for
 * {@link IModIngredientRegistration#register(IIngredientType, Collection, IIngredientHelper, IIngredientRenderer)}
 */
public interface IIngredientRenderer<T> {
	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param xPosition  The x position to render the ingredient.
	 * @param yPosition  The y position to render the ingredient.
	 * @param width      The width to render the ingredient with.
	 * @param height  	 The height to render the ingredient with.
	 * @param ingredient the ingredient to render.
	 *
	 * @since JEI 9.3.0
	 */
	default void render(PoseStack stack, int xPosition, int yPosition, int width, int height, T ingredient) {
		// if not implemented, this calls the old render function for backward compatibility
		render(stack, xPosition, yPosition, ingredient);
	}

	/**
	 * Get the tooltip text for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param ingredient  The ingredient to get the tooltip for.
	 * @param tooltipFlag Whether to show advanced information on item tooltips, toggled by F3+H
	 * @return The tooltip text for the ingredient.
	 */
	List<Component> getTooltip(T ingredient, TooltipFlag tooltipFlag);

	/**
	 * Get the tooltip font renderer for this ingredient. JEI renders the tooltip based on this.
	 *
	 * @param minecraft  The minecraft instance.
	 * @param ingredient The ingredient to get the tooltip for.
	 * @return The font renderer for the ingredient.
	 */
	default Font getFontRenderer(Minecraft minecraft, T ingredient) {
		return minecraft.font;
	}

	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param xPosition  The x position to render the ingredient.
	 * @param yPosition  The y position to render the ingredient.
	 * @param ingredient the ingredient to render.
	 *                   May be null, some renderers (like fluid tanks) will render an empty background.
	 *
	 * @deprecated since JEI 9.3.0. Use {@link #render(PoseStack, int, int, int, int, Object)} instead.
	 */
	@Deprecated
	default void render(PoseStack stack, int xPosition, int yPosition, @Nullable T ingredient) {

	}
}
