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
	 * @param stack  The current {@link PoseStack} for rendering the ingredient.
	 * @param ingredient the ingredient to render.
	 *
	 * @since 9.3.0
	 */
	default void render(PoseStack stack, T ingredient) {
		// if not implemented, this calls the old render function for backward compatibility
		render(stack, 0, 0, ingredient);
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
	 * Get the width of the ingredient drawn on screen by this renderer.
	 *
	 * @since 9.3.0
	 */
	default int getWidth() {
		return 16;
	}

	/**
	 * Get the height of the ingredient drawn on screen by this renderer.
	 *
	 * @since 9.3.0
	 */
	default int getHeight() {
		return 16;
	}

	/**
	 * Renders an ingredient at a specific location.
	 *
	 * @param xPosition  The x position to render the ingredient.
	 * @param yPosition  The y position to render the ingredient.
	 * @param ingredient the ingredient to render.
	 *                   May be null, some renderers (like fluid tanks) will render an empty background.
	 *
	 * @deprecated Use {@link #render(PoseStack, Object)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	default void render(PoseStack stack, int xPosition, int yPosition, @Nullable T ingredient) {

	}
}
