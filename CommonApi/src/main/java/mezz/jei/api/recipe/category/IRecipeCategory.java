package mezz.jei.api.recipe.category;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
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
		return getBackground()
				.getWidth();
	}

	/**
	 * Returns the height of recipe layouts that are drawn for this recipe category.
	 *
	 * @since 11.5.0
	 */
	default int getHeight() {
		return getBackground()
				.getHeight();
	}

	/**
	 * Icon for the category tab.
	 * You can use {@link IGuiHelper#createDrawableIngredient(IIngredientType, Object)}
	 * to create a drawable from an ingredient.
	 *
	 * If null is returned here, JEI will try to use the first recipe catalyst as the icon.
	 *
	 * @return icon to draw on the category tab, max size is 16x16 pixels.
	 */
	@Nullable
	IDrawable getIcon();

	/**
	 * Sets all the recipe's ingredients by filling out an instance of {@link IRecipeLayoutBuilder}.
	 * This is used by JEI for lookups, to figure out what ingredients are inputs and outputs for a recipe.
	 *
	 * @since 9.4.0
	 */
	void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses);

	/**
	 * Create per-recipe extras like {@link IRecipeWidget} and {@link IJeiInputHandler}.
	 *
	 * These have access to a specific recipe, and will persist as long as a recipe layout is on screen,
	 * so they can be used for caching and displaying recipe-specific
	 * information more easily than from the recipe category directly.
	 *
	 * @since 19.6.0
	 */
	default void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {

	}

	/**
	 * Draw extras or additional info about the recipe.
	 * Use the mouse position for things like button highlights.
	 * Tooltips are handled by {@link #getTooltip}
	 *
	 * @param recipe          the current recipe being drawn.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param guiGraphics     the current {@link GuiGraphics} for rendering.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @see IDrawable for a simple class for drawing things.
	 * @see IGuiHelper for useful functions.
	 * @see IRecipeSlotsView for information about the ingredients that are currently being drawn.
	 *
	 * @since 9.3.0
	 */
	default void draw(T recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {

	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * Ingredient tooltips from recipe slots are already handled by JEI, this is for anything else.
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
	 * @deprecated use {@link #getTooltip(ITooltipBuilder, Object, IRecipeSlotsView, double, double)}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "19.5.4", forRemoval = true)
	default List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return List.of();
	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * Ingredient tooltips from recipe slots are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addTooltipCallback(IRecipeSlotTooltipCallback)}
	 *
	 * @param tooltip         a tooltip builder to add tooltip lines to
	 * @param recipe          the current recipe being drawn.
	 * @param recipeSlotsView a view of the current recipe slots being drawn.
	 * @param mouseX          the X position of the mouse, relative to the recipe.
	 * @param mouseY          the Y position of the mouse, relative to the recipe.
	 *
	 * @since 19.5.4
	 */
	default void getTooltip(ITooltipBuilder tooltip, T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		List<Component> tooltipStrings = getTooltipStrings(recipe, recipeSlotsView, mouseX, mouseY);
		tooltip.addAll(tooltipStrings);
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
	 *
	 * @deprecated create an {@link IJeiInputHandler} or {@link GuiEventListener} and add it with
	 * {@link IRecipeExtrasBuilder#addInputHandler} or {@link IRecipeExtrasBuilder#addGuiEventListener}
	 */
	@SuppressWarnings("DeprecatedIsStillUsed")
	@Deprecated(since = "19.6.0", forRemoval = true)
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
	 * Since 19.1.0, this is also used for bookmarking recipes.
	 *
	 * @return the registry name of the recipe, or null if there is none
	 *
	 * @since 9.3.0
	 */
	@Nullable
	default ResourceLocation getRegistryName(T recipe) {
		if (recipe instanceof RecipeHolder<?> recipeHolder) {
			return recipeHolder.id();
		}
		return null;
	}

	/**
	 * @return true if JEI should draw a border around this recipe to
	 * 				separate it visually from other recipes near it.
	 * 				(most recipes should use this to help players navigate easily)
	 *
	 *         false if this recipe already draws a strong border that
	 *         		separates it visually from the other recipes.
	 *         		In this case, JEI will not draw another border around the recipe.
	 *
	 * @since 19.5.3
	 */
	default boolean needsRecipeBorder() {
		return true;
	}
}
