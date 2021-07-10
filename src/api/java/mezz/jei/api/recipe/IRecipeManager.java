package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaRecipeCategoryUid;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IJeiRuntime;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * Get the instance from {@link IJeiRuntime#getRecipeManager()}.
 */
public interface IRecipeManager {
	/**
	 * Returns a new focus.
	 */
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);

	/**
	 * Returns a list of Recipe Categories for the focus.
	 * @param focus an optional search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since JEI 7.7.1
	 */
	<V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns a list of Recipe Categories for the focus
	 * @param recipeCategoryUids a list of recipe category uids to retrieve
	 * @param focus an optional search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since JEI 7.7.1
	 */
	<V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 * @since JEI 7.7.1
	 */
	@Nullable
	IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden);

	/**
	 * Returns a list of recipes in the recipeCategory that have the focus.
	 * @param recipeCategory the recipe category to find recipes in
	 * @param focus the current search focus, or null if there is no focus.
	 * @param includeHidden set true to include recipes that are hidden.
	 * @since JEI 7.7.1
	 */
	<T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(Object, ResourceLocation...)}.
	 * @since JEI 7.7.1
	 */
	List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden);

	/**
	 * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
	 * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
	 *
	 * @param recipeCategory the recipe category that the recipe belongs to
	 * @param recipe         the specific recipe to draw.
	 * @param focus          the focus of the recipe layout.
	 */
	<T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, IFocus<?> focus);

	/**
	 * Hides a recipe so that it will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to hide.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #unhideRecipe(Object, ResourceLocation)
	 */
	<T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe that was hidden by {@link #hideRecipe(Object, ResourceLocation)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to unhide.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #hideRecipe(Object, ResourceLocation)
	 */
	<T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #unhideRecipeCategory(ResourceLocation)
	 */
	void hideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(ResourceLocation)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #hideRecipeCategory(ResourceLocation)
	 */
	void unhideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Add a new recipe while the game is running.
	 */
	@Deprecated
	<T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Returns an unmodifiable list of Recipe Categories that are displayed by JEI.
	 *
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipeCategories(Collection, IFocus, boolean)}
	 */
	@Deprecated
	default List<IRecipeCategory<?>> getRecipeCategories(List<ResourceLocation> recipeCategoryUids) {
		return getRecipeCategories(recipeCategoryUids, null, false);
	}

	/**
	 * Returns an unmodifiable list of all Recipe Categories that are displayed by JEI.
	 *
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipeCategories(IFocus, boolean)}
	 */
	@Deprecated
	default List<IRecipeCategory<?>> getRecipeCategories() {
		return getRecipeCategories(null, false);
	}

	/**
	 * Returns a list of Recipe Categories for the focus that are displayed by JEI.
	 *
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipeCategories(IFocus, boolean)}
	 */
	@Deprecated
	default <V> List<IRecipeCategory<?>> getRecipeCategories(IFocus<V> focus) {
		return getRecipeCategories(focus, false);
	}

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 *
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipeCategory(ResourceLocation, boolean)}
	 */
	@Nullable
	default IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid) {
		return getRecipeCategory(recipeCategoryUid, true);
	}

	/**
	 * Returns a list of recipes in recipeCategory.
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipes(IRecipeCategory, IFocus, boolean)}
	 */
	default <T> List<T> getRecipes(IRecipeCategory<T> recipeCategory) {
		return getRecipes(recipeCategory, null, false);
	}

	/**
	 * Returns a list of recipes in the recipeCategory that have the focus and are displayed by JEI.
	 *
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipes(IRecipeCategory, IFocus, boolean)}
	 */
	default <T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
		return getRecipes(recipeCategory, focus, false);
	}

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(Object, ResourceLocation...)}.
	 *
	 * @deprecated since JEI 7.7.1. Use {@link #getRecipeCatalysts(IRecipeCategory, boolean)}
	 */
	default List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory) {
		return getRecipeCatalysts(recipeCategory, false);
	}

}
