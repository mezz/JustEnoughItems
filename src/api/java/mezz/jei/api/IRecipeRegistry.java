package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

/**
 * The IRecipeManager offers several functions for retrieving and handling recipes.
 * Get the instance from {@link IJeiRuntime#getRecipeRegistry()}.
 */
public interface IRecipeRegistry {

	/**
	 * Returns an unmodifiable list of all Recipe Categories
	 */
	List<IRecipeCategory> getRecipeCategories();

	/**
	 * Returns an unmodifiable list of Recipe Categories
	 */
	List<IRecipeCategory> getRecipeCategories(List<ResourceLocation> recipeCategoryUids);

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 *
	 * @since JEI 4.7.7
	 */
	@Nullable
	IRecipeCategory getRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Returns a new focus.
	 */
	<V> IFocus<V> createFocus(IFocus.Mode mode, V ingredient);

	/**
	 * Returns a list of Recipe Categories for the focus.
	 *
	 * @since JEI 3.11.0
	 */
	<V> List<IRecipeCategory> getRecipeCategories(IFocus<V> focus);

	/**
	 * Returns a list of Recipe Wrappers in the recipeCategory that have the focus.
	 *
	 * @since JEI 3.12.0
	 */
	<T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus);

	/**
	 * Returns a list of Recipe Wrappers in recipeCategory.
	 *
	 * @since JEI 3.12.0
	 */
	<T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory);

	/**
	 * Returns the {@link IRecipeWrapper} for this recipe.
	 *
	 * @param recipe            the recipe to get a wrapper for.
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @return the {@link IRecipeWrapper} for this recipe. returns null if the recipe cannot be handled by JEI or its addons.
	 * @since JEI 4.3.0
	 */
	@Nullable
	IRecipeWrapper getRecipeWrapper(Object recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IModRegistry#addRecipeCatalyst(Object, ResourceLocation...)}.
	 *
	 * @since JEI 4.5.0
	 */
	List<Object> getRecipeCatalysts(IRecipeCategory recipeCategory);

	/**
	 * Returns the recipe transfer handler for the given container and category, if one exists.
	 *
	 * @param container      The container to transfer items in.
	 * @param recipeCategory The type of recipe that the recipe transfer handler acts on.
	 * @see IRecipeTransferRegistry
	 * @since JEI 3.13.2
	 */
	@Nullable
	IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory);

	/**
	 * Returns a drawable recipe layout, for addons that want to draw the layouts somewhere.
	 * Layouts created this way do not have recipe transfer buttons, they are not useful for this purpose.
	 *
	 * @param recipeCategory the recipe category that the recipe belongs to
	 * @param recipeWrapper  the specific recipe wrapper to draw.
	 * @param focus          the focus of the recipe layout.
	 * @since JEI 3.13.2
	 */
	@Nullable
	<T extends IRecipeWrapper> IRecipeLayoutDrawable createRecipeLayoutDrawable(IRecipeCategory<T> recipeCategory, T recipeWrapper, IFocus<?> focus);

	/**
	 * Hides a recipe so that it will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to hide.
	 *                          Get an instance using {@link #getRecipeWrapper(Object, ResourceLocation)}
	 *                          or {@link #getRecipeWrappers(IRecipeCategory)}
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #unhideRecipe(IRecipeWrapper, ResourceLocation)
	 * @since JEI 4.9.0
	 */
	void hideRecipe(IRecipeWrapper recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe that was hidden by {@link #hideRecipe(IRecipeWrapper, ResourceLocation)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to unhide.
	 *                          Get an instance using {@link #getRecipeWrapper(Object, ResourceLocation)}
	 *                          or {@link #getRecipeWrappers(IRecipeCategory)}
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #hideRecipe(IRecipeWrapper, ResourceLocation)
	 * @since JEI 4.9.0
	 */
	void unhideRecipe(IRecipeWrapper recipe, ResourceLocation recipeCategoryUid);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #unhideRecipeCategory(ResourceLocation)
	 * @since JEI 4.13.0
	 */
	void hideRecipeCategory(ResourceLocation recipeCategoryUid);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(ResourceLocation)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #hideRecipeCategory(ResourceLocation)
	 * @since JEI 4.13.0
	 */
	void unhideRecipeCategory(ResourceLocation recipeCategoryUid);

	// DEPRECATED BELOW

	/**
	 * Add a new recipe while the game is running.
	 *
	 * @since JEI 4.3.0
	 * @deprecated since JEI 4.7.3. This is not supported by Minecraft 1.12 and using this is discouraged.
	 */
	@Deprecated
	void addRecipe(IRecipeWrapper recipe, ResourceLocation recipeCategoryUid);
}
