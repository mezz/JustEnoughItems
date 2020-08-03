package mezz.jei.api.recipe;

import javax.annotation.Nullable;
import java.util.List;

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
	 * Returns an unmodifiable list of all Recipe Categories
	 */
	List<IRecipeCategory<?>> getRecipeCategories();

	/**
	 * Returns an unmodifiable list of Recipe Categories
	 */
	List<IRecipeCategory<?>> getRecipeCategories(List<ResourceLocation> recipeCategoryUids);

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 */
	@Nullable
	IRecipeCategory<?> getRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Returns a new focus.
	 */
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);

	/**
	 * Returns a list of Recipe Categories for the focus.
	 */
	<V> List<IRecipeCategory<?>> getRecipeCategories(IFocus<V> focus);

	/**
	 * Returns a list of recipes in the recipeCategory that have the focus.
	 */
	<T, V> List<T> getRecipes(IRecipeCategory<T> recipeCategory, IFocus<V> focus);

	/**
	 * Returns a list of recipes in recipeCategory.
	 */
	<T> List<T> getRecipes(IRecipeCategory<T> recipeCategory);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IRecipeCatalystRegistration#addRecipeCatalyst(Object, ResourceLocation...)}.
	 */
	List<Object> getRecipeCatalysts(IRecipeCategory<?> recipeCategory);

	/**
	 * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
	 * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
	 *
	 * @param recipeCategory the recipe category that the recipe belongs to
	 * @param recipe         the specific recipe to draw.
	 * @param focus          the focus of the recipe layout.
	 */
	@Nullable
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
}
