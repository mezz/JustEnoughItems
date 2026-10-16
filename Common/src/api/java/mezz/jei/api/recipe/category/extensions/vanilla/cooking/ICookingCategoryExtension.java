package mezz.jei.api.recipe.category.extensions.vanilla.cooking;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Implement this interface to have your recipe extension work as part of
 * {@link RecipeTypes#SMELTING}, {@link RecipeTypes#BLASTING}, {@link RecipeTypes#SMOKING},
 * or {@link RecipeTypes#CAMPFIRE_COOKING} recipes.
 *
 * Register this extension by getting the extendable cooking category from:
 * {@link IVanillaCategoryExtensionRegistration#getSmeltingCategory()},
 * {@link IVanillaCategoryExtensionRegistration#getBlastingCategory()},
 * {@link IVanillaCategoryExtensionRegistration#getSmokingCategory()}, or
 * {@link IVanillaCategoryExtensionRegistration#getCampfireCookingCategory()}
 * and then registering it with {@link IExtendableCookingRecipeCategory#addExtension(Class, ICookingCategoryExtension)}.
 * The same extension instance can be registered with multiple cooking categories.
 *
 * @param <R> the recipe class or an interface shared by multiple cooking recipe classes
 * @since 30.46.0
 */
@FunctionalInterface
public interface ICookingCategoryExtension<R> {
	/**
	 * Override the default {@link IRecipeCategory} behavior.
	 *
	 * Set the input and output ingredients in the slots provided by the category.
	 * The slots already have positions and backgrounds.
	 *
	 * @param recipe the current cooking recipe
	 * @param builder the layout builder
	 * @param inputSlot the empty cooking input slot
	 * @param outputSlot the empty cooking result slot
	 * @param focuses the current focuses
	 *
	 * @see IRecipeCategory#setRecipe(IRecipeLayoutBuilder, Object, IFocusGroup)
	 *
	 * @since 30.46.0
	 */
	void setRecipe(R recipe, IRecipeLayoutBuilder builder, IRecipeSlotBuilder inputSlot, IRecipeSlotBuilder outputSlot, IFocusGroup focuses);

	/**
	 * Get the fuel for the given cooking recipe. Campfire cooking does not use fuel.
	 *
	 * @param fuel the default fuel from the recipe display, or {@link SlotDisplay.AnyFuel#INSTANCE}
	 * @return the fuel display, optionally with a remainder, or {@link SlotDisplay.Empty#INSTANCE} for no fuel slot
	 * @since 30.46.0
	 */
	default SlotDisplay getFuel(R recipe, SlotDisplay fuel) {
		return fuel;
	}

	/**
	 * Get the cooking time for the given cooking recipe.
	 *
	 * @param cookingTime the default cooking time in ticks
	 * @return the cooking time in ticks, must be greater than zero
	 * @since 30.46.0
	 */
	default int getCookingTime(R recipe, int cookingTime) {
		return cookingTime;
	}

	/**
	 * Get the experience for the given cooking recipe.
	 *
	 * @param experience the default experience, or zero for campfire cooking
	 * @return the experience, or zero to hide the experience label
	 * @since 30.46.0
	 */
	default float getExperience(R recipe, float experience) {
		return experience;
	}

	/**
	 * Return false when the recipe should not be displayed.
	 * JEI does not use the default extension for recipes that return false.
	 *
	 * @return true if the given recipe can be handled by this category extension.
	 * @since 30.46.0
	 */
	default boolean isHandled(R recipe) {
		return true;
	}

	/**
	 * Sets the extras for the recipe category, like input handlers and recipe widgets.
	 *
	 * Recipe Widgets persist as long as a recipe layout is on screen,
	 * so they can be used for caching and displaying recipe-specific
	 * information more easily than from the recipe category directly.
	 *
	 * @see IRecipeCategory#createRecipeExtras
	 *
	 * @since 30.46.0
	 */
	default void createRecipeExtras(R recipe, IRecipeExtrasBuilder builder, IFocusGroup focuses) {

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
	 * @param recipe the current cooking recipe being drawn.
	 * @param recipeSlots the current recipe slots being drawn, including fuel and remainder slots when present.
	 * @param focuses the current focuses
	 *
	 * @see IRecipeCategory#onDisplayedIngredientsUpdate
	 *
	 * @since 30.46.0
	 */
	default void onDisplayedIngredientsUpdate(R recipe, List<IRecipeSlotDrawable> recipeSlots, IFocusGroup focuses) {

	}

	/**
	 * Override the underlying recipe identifiers shown in advanced tooltips and copied as recipe IDs.
	 * See {@link IRecipeCategory#getSourceRecipeIds(Object)} for the identifier contract.
	 *
	 * @return source identifiers, an empty list for no underlying recipe, or null to use the category default
	 * @since 30.46.0
	 */
	default @Nullable List<Identifier> getSourceRecipeIds(R recipe) {
		return null;
	}
}
