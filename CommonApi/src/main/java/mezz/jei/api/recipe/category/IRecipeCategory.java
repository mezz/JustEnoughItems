package mezz.jei.api.recipe.category;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.serialization.Codec;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.ICodecHelper;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Defines a category of recipe, (i.e. Crafting Table Recipe, Furnace Recipe).
 * Register it with {@link IRecipeCategoryRegistration#addRecipeCategories(IRecipeCategory[])}
 *
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
	 * Called every time JEI updates the cycling displayed ingredients on a recipe.
	 *
	 * Use this (for example) to compute recipe outputs that result from complex relationships between ingredients.
	 *
	 * Use {@link IRecipeSlotDrawable#getDisplayedIngredient()} from your regular slots to see what is
	 * currently being drawn, and calculate what you need from there.
	 * You can override any slot's displayed ingredient with {@link IRecipeSlotDrawable#createDisplayOverrides()}.
	 *
	 * Note that overrides set this way are not searchable via recipe lookups in JEI,
	 * it is only for displaying things too complex for normal lookups to handle.
	 *
	 * @param recipe the current recipe being drawn.
	 * @param recipeSlots the current recipe slots being drawn.
	 * @param focuses the current focuses
	 *
	 * @since 19.8.3
	 */
	default void onDisplayedIngredientsUpdate(T recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {

	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * Ingredient tooltips from recipe slots are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addRichTooltipCallback(IRecipeSlotRichTooltipCallback)}
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
	@Deprecated(since = "19.5.4", forRemoval = true)
	default List<Component> getTooltipStrings(T recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
		return List.of();
	}

	/**
	 * Get the tooltip for whatever is under the mouse.
	 * Ingredient tooltips from recipe slots are already handled by JEI, this is for anything else.
	 *
	 * To add to ingredient tooltips, see {@link IRecipeSlotBuilder#addRichTooltipCallback(IRecipeSlotRichTooltipCallback)}
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
	 * Get a codec for this type of recipe.
	 *
	 * The default implementation uses {@link #getRegistryName} to look up the recipes in an inefficient way.
	 *
	 * Override this method to provide a more efficient implementation,
	 * or an implementation that doesn't depend on {@link #getRegistryName}
	 *
	 * @since 19.9.0
	 */
	default Codec<T> getCodec(ICodecHelper codecHelper, IRecipeManager recipeManager) {
		RecipeType<T> recipeType = getRecipeType();
		if (RecipeHolder.class.isAssignableFrom(recipeType.getRecipeClass())) {
			@SuppressWarnings("unchecked")
			Codec<T> recipeHolderCodec = (Codec<T>) codecHelper.getRecipeHolderCodec();
			return recipeHolderCodec;
		}
		return codecHelper.getSlowRecipeCategoryCodec(this, recipeManager);
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
