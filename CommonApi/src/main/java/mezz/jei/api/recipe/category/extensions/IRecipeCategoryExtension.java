package mezz.jei.api.recipe.category.extensions;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Collections;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

/**
 * An extension to a recipe category with methods that allow JEI to make sense of it.
 * Plugins implement these for recipe categories that support it, for each type of recipe they have.
 *
 * @apiNote Since 16.0.0, extensions have the recipe passed to them in each method,
 * so they can be singleton instances instead of creating many of them to wrap recipes.
 */
public interface IRecipeCategoryExtension<T> {
	/**
	 * Draw additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltipStrings(Object, double, double)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @since 16.0.0
	 */
	default void drawInfo(T recipe, int recipeWidth, int recipeHeight, GuiGraphics guiGraphics, double mouseX, double mouseY) {
		drawInfo(recipeWidth, recipeHeight, guiGraphics, mouseX, mouseY);
	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
	 * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltip}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @since 19.5.4
	 */
	default void getTooltip(ITooltipBuilder tooltip, T recipe, double mouseX, double mouseY) {
		List<Component> tooltipStrings = getTooltipStrings(recipe, mouseX, mouseY);
		tooltip.addAll(tooltipStrings);
	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
	 * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltip}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 * @since 16.0.0
	 * @deprecated use {@link #getTooltip}
	 */
	@Deprecated(since = "19.5.4", forRemoval = true)
	default List<Component> getTooltipStrings(T recipe, double mouseX, double mouseY) {
		return getTooltipStrings(mouseX, mouseY);
	}

	/**
	 * Called when a player inputs while hovering over the recipe.
	 * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @param input  the current input from the player.
	 * @return true if the input was handled, false otherwise
	 * @since 16.0.0
	 */
	default boolean handleInput(T recipe, double mouseX, double mouseY, InputConstants.Key input) {
		return handleInput(mouseX, mouseY, input);
	}

	/**
	 * @return true if the given recipe can be handled by this category extension.
	 * @since 16.0.0
	 */
	default boolean isHandled(T recipe) {
		return true;
	}

	/**
	 * Draw additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltipStrings(Object, double, double)}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @deprecated use {@link #drawInfo(Object, int, int, GuiGraphics, double, double)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "16.0.0", forRemoval = true)
	default void drawInfo(int recipeWidth, int recipeHeight, GuiGraphics guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * ItemStack and fluid tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
	 * To add tooltips for a recipe category, see {@link IRecipeCategory#getTooltip}
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 * @deprecated use {@link #getTooltipStrings(Object, double, double)}
	 */
	@Deprecated(since = "16.0.0", forRemoval = true)
	default List<Component> getTooltipStrings(double mouseX, double mouseY) {
		return Collections.emptyList();
	}

	/**
	 * Called when a player inputs while hovering over the recipe.
	 * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
	 *
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @param input  the current input from the player.
	 * @return true if the input was handled, false otherwise
	 * @since 8.3.0
	 * @deprecated use {@link #handleInput(Object, double, double, InputConstants.Key)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "16.0.0", forRemoval = true)
	default boolean handleInput(double mouseX, double mouseY, InputConstants.Key input) {
		return false;
	}
}
