package mezz.jei.api.recipe;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * The {@link IRecipeManager} offers several functions for retrieving and handling recipes.
 * Get the instance from {@link IJeiRuntime#getRecipeManager()}.
 */
public interface IRecipeManager {
	/**
	 * Create a recipe lookup for the given recipe type.
	 *
	 * {@link IRecipeLookup} is a helper class that lets you choose
	 * the results you want, and then get them.
	 *
	 * @since 9.5.0
	 */
	<R> IRecipeLookup<R> createRecipeLookup(RecipeType<R> recipeType);

	/**
	 * Create a recipe category lookup for the given recipe type.
	 *
	 * {@link IRecipeCategoriesLookup} is a helper class that lets you choose
	 * the results you want, and then get them.
	 *
	 * @since 9.5.0
	 */
	IRecipeCategoriesLookup createRecipeCategoryLookup();

	/**
	 * Create a recipe catalyst lookup for the given recipe type.
	 *
	 * {@link IRecipeCatalystLookup} is a helper class that lets you choose
	 * the results you want, and then get them.
	 *
	 * @since 9.5.0
	 */
	IRecipeCatalystLookup createRecipeCatalystLookup(RecipeType<?> recipeType);

	/**
	 * Hides recipes so that they will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the recipe type for this recipe.
	 * @param recipes    the recipes to hide.
	 *
	 * @see #unhideRecipes(RecipeType, Collection)
	 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
	 *
	 * @since 9.5.0
	 */
	<T> void hideRecipes(RecipeType<T> recipeType, Collection<T> recipes);

	/**
	 * Unhides recipes that were hidden by {@link #hideRecipes(RecipeType, Collection)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the recipe type for this recipe.
	 * @param recipes    the recipes to unhide.
	 *
	 * @see #hideRecipes(RecipeType, Collection)
	 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
	 *
	 * @since 9.5.0
	 */
	<T> void unhideRecipes(RecipeType<T> recipeType, Collection<T> recipes);

	/**
	 * Add new recipes while the game is running.
	 *
	 * @see RecipeTypes for all the built-in recipe types that are added by JEI.
	 *
	 * @since 9.5.0
	 */
	<T> void addRecipes(RecipeType<T> recipeType, List<T> recipes);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the unique ID for the recipe category
	 * @see #unhideRecipeCategory(RecipeType)
	 *
	 * @since 9.5.0
	 */
	void hideRecipeCategory(RecipeType<?> recipeType);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(RecipeType)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeType the unique ID for the recipe category
	 * @see #hideRecipeCategory(RecipeType)
	 *
	 * @since 9.5.0
	 */
	void unhideRecipeCategory(RecipeType<?> recipeType);

	/**
	 * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
	 * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
	 *
	 * @param recipeCategory the recipe category that the recipe belongs to
	 * @param recipe         the specific recipe to draw.
	 * @param focus          the focus of the recipe layout.
	 */
	<T> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipe, @Nullable IFocus<?> focus);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #unhideRecipeCategory(RecipeType)
	 *
	 * @deprecated use {@link #hideRecipeCategory(RecipeType)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	void hideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(RecipeType)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #hideRecipeCategory(RecipeType)
	 *
	 * @deprecated use {@link #unhideRecipeCategory(RecipeType)}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	void unhideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Returns a list of Recipe Categories for the focus.
	 * @param focus an optional search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since 7.7.1
	 * @deprecated Use {@link #createRecipeCategoryLookup()}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<V> List<IRecipeCategory<?>> getRecipeCategories(@Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns a list of Recipe Categories for multiple focuses.
	 *
	 * @param focuses       an optional collection of search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 * @since 9.3.0
	 * @deprecated Use {@link #createRecipeCategoryLookup()}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	List<IRecipeCategory<?>> getRecipeCategories(Collection<? extends IFocus<?>> focuses, boolean includeHidden);

	/**
	 * Returns a list of Recipe Categories for the focus
	 * @param recipeCategoryUids a list of recipe category uids to retrieve
	 * @param focus an optional search focus to narrow the results on
	 * @param includeHidden set true to include recipe categories that are hidden or have no recipes.
	 *
	 * @since 7.7.1
	 *
	 * @deprecated recipeCategoryUids are being phased out in favor of {@link RecipeType}.
	 * Use {@link #createRecipeCategoryLookup()}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<V> List<IRecipeCategory<?>> getRecipeCategories(Collection<ResourceLocation> recipeCategoryUids, @Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 * @since 7.7.1
	 *
	 * @deprecated use the new {@link #createRecipeCategoryLookup()}
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	@Nullable
	IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid, boolean includeHidden);

	/**
	 * Returns a list of recipes in the recipeCategory that have the given focuses.
	 *
	 * @param recipeCategory the recipe category to find recipes in
	 * @param focuses the current search focuses, or an empty list if there is no focus.
	 * @param includeHidden set true to include recipes that are hidden.
	 *
	 * @since 9.3.0
	 *
	 * @deprecated use {@link #createRecipeLookup(RecipeType)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<T> List<T> getRecipes(IRecipeCategory<T> recipeCategory, List<? extends IFocus<?>> focuses, boolean includeHidden);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(IIngredientType, Object, RecipeType[])}.
	 *
	 * @since 9.3.0
	 * @deprecated use {@link #createRecipeCatalystLookup(RecipeType)} and
	 * 	           {@link IRecipeCatalystLookup#get()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	List<ITypedIngredient<?>> getRecipeCatalystsTyped(IRecipeCategory<?> recipeCategory, boolean includeHidden);

	/**
	 * Hides a recipe so that it will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to hide.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *
	 * @see #unhideRecipes(RecipeType, Collection)
	 *
	 * @deprecated use the typed {@link #hideRecipes(RecipeType, Collection)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<T> void hideRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe that was hidden by {@link #hideRecipes(RecipeType, Collection)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to unhide.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *
	 * @see #hideRecipes(RecipeType, Collection)
	 *
	 * @deprecated use the typed {@link #unhideRecipes(RecipeType, Collection)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<T> void unhideRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Add a new recipe while the game is running.
	 *
	 * @deprecated use the typed {@link #addRecipes(RecipeType, List)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<T> void addRecipe(T recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Returns a new focus.
	 *
	 * @deprecated Use {@link IFocusFactory#createFocus(RecipeIngredientRole, IIngredientType, Object)} instead.
	 */
	@SuppressWarnings("removal")
	@Deprecated(forRemoval = true, since = "9.3.0")
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);

	/**
	 * Returns a list of recipes in the recipeCategory that have the focus.
	 * @param recipeCategory the recipe category to find recipes in
	 * @param focus the current search focus, or null if there is no focus.
	 * @param includeHidden set true to include recipes that are hidden.
	 * @since 7.7.1
	 *
	 * @deprecated use {@link #createRecipeLookup(RecipeType)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	<T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, @Nullable IFocus<V> focus, boolean includeHidden);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(IIngredientType, Object, RecipeType[])}.
	 * @since 7.7.1
	 *
	 * @deprecated use {@link #createRecipeCatalystLookup(RecipeType)} and
	 * 	 	           {@link IRecipeCatalystLookup#get()} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.3.0")
	List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory, boolean includeHidden);
}
