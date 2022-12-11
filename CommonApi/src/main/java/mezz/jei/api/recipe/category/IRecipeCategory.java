package mezz.jei.api.recipe.category;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Handles setting up the GUI for its recipe category in {@link #setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)}.
 * Also draws elements that are common to all recipes in the category like the background.
 */
public interface IRecipeCategory<T> {
	/**
	 * @return the type of recipe that this category handles.
	 *
	 * @since 9.5.0
	 */
	RecipeType<T> getRecipeType();

	/**
	 * Returns a text component representing the name of this recipe type.
	 * Drawn at the top of the recipe GUI pages for this category.
	 * @since 7.6.4
	 */
	Component getTitle();

	/**
	 * Returns the drawable background for a single recipe in this category.
	 */
	IDrawable getBackground();

	/**
	 * Returns the width of recipe layouts that are drawn for this recipe category.
	 *
	 * @since 11.5.0
	 */
	default int getWidth() {
		return getBackground().getWidth();
	}

	/**
	 * Returns the height of recipe layouts that are drawn for this recipe category.
	 *
	 * @since 11.5.0
	 */
	default int getHeight() {
		return getBackground().getHeight();
	}

	/**
	 * Icon for the category tab.
	 * You can use {@link IGuiHelper#createDrawableIngredient(IIngredientType, Object)}
	 * to create a drawable from an ingredient.
	 *
	 * @return icon to draw on the category tab, max size is 16x16 pixels.
	 */
	IDrawable getIcon();

	/**
	 * Sets all the recipe's ingredients by filling out an instance of {@link IRecipeLayoutBuilder}.
	 * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
	 *
	 * @since 9.4.0
	 */
	void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

	/**
	 * Draw extras or additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltipStrings(Object, IRecipeSlotsView, double, double)}
	 *
	 * @param recipe          the current recipe being drawn.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param stack           the current {@link PoseStack} for rendering.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 *
	 * @since 9.3.0
	 */
	default void draw(T recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {

	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * Ingredient tooltips are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
	 *
	 * @param recipe          the current recipe being drawn.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 * @return tooltip strings. If there is no tooltip at this position, return an empty list.
	 *
	 * @since 9.3.0
	 */
	default List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return List.of();
	}

	/**
	 * Called when a player clicks the recipe.
	 * Useful for implementing buttons, hyperlinks, and other interactions to your recipe.
	 *

	 * @param recipe the currently hovered recipe
	 * @param mouseX the X position of the mouse, relative to the recipe.
	 * @param mouseY the Y position of the mouse, relative to the recipe.
	 * @param input  the current input
	 * @return true if the input was handled, false otherwise
	 * @since 8.3.0
	 */
	default boolean handleInput(T recipe, double mouseX, double mouseY, InputConstants.Key input) {
		return false;
	}

	/**
	 * @return true if the given recipe can be handled by this category.
	 * @since 7.2.0
	 */
	default boolean isHandled(T recipe) {
		return true;
	}

	/**
	 * Return the registry name of the recipe here.
	 * With advanced tooltips on, this will show on the output item's tooltip.
	 *
	 * This will also show the modId when the recipe modId and output item modId do not match.
	 * This lets the player know where the recipe came from.
	 *
	 * @return the registry name of the recipe, or null if there is none
	 *
	 * @since 9.3.0
	 */
	@Nullable
	default ResourceLocation getRegistryName(T recipe) {
		if (recipe instanceof Recipe<?> vanillaRecipe) {
			return vanillaRecipe.getId();
		}
		return null;
	}
}
