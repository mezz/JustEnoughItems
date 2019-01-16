package mezz.jei.api;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.api.recipe.IVanillaRecipeFactory;
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
	List<IRecipeCategory> getRecipeCategories(List<String> recipeCategoryUids);

	/**
	 * Returns the recipe category for the given UID.
	 * Returns null if the recipe category does not exist.
	 *
	 * @since JEI 4.7.7
	 */
	@Nullable
	IRecipeCategory getRecipeCategory(String recipeCategoryUid);

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
	IRecipeWrapper getRecipeWrapper(Object recipe, String recipeCategoryUid);

	/**
	 * Returns an unmodifiable collection of ingredients that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IModRegistry#addRecipeCatalyst(Object, String...)}.
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
	 *                          Get an instance using {@link #getRecipeWrapper(Object, String)}
	 *                          or {@link #getRecipeWrappers(IRecipeCategory)}
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #unhideRecipe(IRecipeWrapper, String)
	 * @since JEI 4.9.0
	 */
	void hideRecipe(IRecipeWrapper recipe, String recipeCategoryUid);

	/**
	 * Unhides a recipe that was hidden by {@link #hideRecipe(IRecipeWrapper, String)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe            the recipe to unhide.
	 *                          Get an instance using {@link #getRecipeWrapper(Object, String)}
	 *                          or {@link #getRecipeWrappers(IRecipeCategory)}
	 * @param recipeCategoryUid the unique ID for the recipe category this recipe is a part of.
	 *                          See {@link VanillaRecipeCategoryUid} for vanilla recipe category UIDs.
	 * @see #hideRecipe(IRecipeWrapper, String)
	 * @since JEI 4.9.0
	 */
	void unhideRecipe(IRecipeWrapper recipe, String recipeCategoryUid);

	/**
	 * Hide an entire recipe category of recipes from JEI.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #unhideRecipeCategory(String)
	 * @since JEI 4.13.0
	 */
	void hideRecipeCategory(String recipeCategoryUid);

	/**
	 * Unhides a recipe category that was hidden by {@link #hideRecipeCategory(String)}.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipeCategoryUid the unique ID for the recipe category
	 * @see #hideRecipeCategory(String)
	 * @since JEI 4.13.0
	 */
	void unhideRecipeCategory(String recipeCategoryUid);

	// DEPRECATED BELOW

	/**
	 * Hides a recipe so that it will not be displayed.
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe the recipe to hide.
	 *               Get an instance using {@link #getRecipeWrapper(Object, String)}
	 *               or {@link #getRecipeWrappers(IRecipeCategory)}
	 * @see #unhideRecipe(IRecipeWrapper)
	 * @since JEI 4.7.7
	 * @deprecated since JEI 4.9.0, use {@link #hideRecipe(IRecipeWrapper, String)}
	 */
	@Deprecated
	void hideRecipe(IRecipeWrapper recipe);

	/**
	 * Unhides a recipe that was hidden by {@link #hideRecipe(IRecipeWrapper)}
	 * This can be used by mods that create recipe progression.
	 *
	 * @param recipe the recipe to unhide.
	 *               Get an instance using {@link #getRecipeWrapper(Object, String)}
	 *               or {@link #getRecipeWrappers(IRecipeCategory)}
	 * @see #hideRecipe(IRecipeWrapper)
	 * @since JEI 4.7.7
	 * @deprecated since JEI 4.9.0, use {@link #unhideRecipe(IRecipeWrapper, String)}
	 */
	@Deprecated
	void unhideRecipe(IRecipeWrapper recipe);

	/**
	 * Add a new recipe while the game is running.
	 *
	 * @since JEI 4.3.0
	 * @deprecated since JEI 4.7.3. This is not supported by Minecraft 1.12 and using this is discouraged.
	 */
	@Deprecated
	void addRecipe(IRecipeWrapper recipe, String recipeCategoryUid);

	/**
	 * Remove a recipe while the game is running.
	 *
	 * @since JEI 4.3.0
	 * @deprecated since JEI 4.7.3. Use {@link #hideRecipe(IRecipeWrapper, String)}
	 */
	@Deprecated
	void removeRecipe(IRecipeWrapper recipe, String recipeCategoryUid);

	/**
	 * Returns the IRecipeHandler associated with the recipeClass or null if there is none.
	 *
	 * @deprecated since JEI 4.3.0
	 */
	@Deprecated
	@Nullable
	<T> IRecipeHandler<T> getRecipeHandler(Class<? extends T> recipeClass);

	/**
	 * Add a new smelting recipe while the game is running.
	 * By default, all smelting recipes from {@link FurnaceRecipes#smeltingList} are already added by JEI.
	 *
	 * @since JEI 4.2.7
	 * @deprecated since JEI 4.3.3. Use {@link IVanillaRecipeFactory#createSmeltingRecipe(List, ItemStack)}
	 */
	@Deprecated
	void addSmeltingRecipe(List<ItemStack> inputs, ItemStack output);

	/**
	 * Create a new smelting recipe.
	 * By default, all smelting recipes from {@link FurnaceRecipes#smeltingList} are already added by JEI.
	 *
	 * @param inputs the list of possible inputs to rotate through
	 * @param output the output
	 * @since JEI 4.3.3
	 * @deprecated since JEI 4.5.0. Use {@link IVanillaRecipeFactory#createSmeltingRecipe(List, ItemStack)}
	 */
	@Deprecated
	IRecipeWrapper createSmeltingRecipe(List<ItemStack> inputs, ItemStack output);

	/**
	 * Create a new anvil recipe.
	 *
	 * @param leftInput   the left input
	 * @param rightInputs the list of possible right inputs to rotate through
	 * @param outputs     the list of possible outputs to rotate through
	 * @return the {@link IRecipeWrapper} for this recipe.
	 * @since JEI 4.3.3
	 * @deprecated since JEI 4.5.0. Use {@link IVanillaRecipeFactory#createAnvilRecipe(ItemStack, List, List)}
	 */
	@Deprecated
	IRecipeWrapper createAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Create a new brewing recipe.
	 * By default, all brewing recipes are already added by JEI, this is for mods that add recipes after the game has loaded.
	 *
	 * @param ingredients  the ingredients added to a potion to create a new one.
	 *                     Normally one ingredient, but a list will display several in rotation.
	 * @param potionInput  the input potion for the brewing recipe.
	 * @param potionOutput the output potion for the brewing recipe.
	 * @return the {@link IRecipeWrapper} for this recipe.
	 * @since JEI 4.3.4
	 * @deprecated since JEI 4.5.0. Use {@link IVanillaRecipeFactory#createBrewingRecipe(List, ItemStack, ItemStack)}
	 */
	@Deprecated
	IRecipeWrapper createBrewingRecipe(List<ItemStack> ingredients, ItemStack potionInput, ItemStack potionOutput);

	/**
	 * Add a new recipe while the game is running.
	 * This is only for things like gated recipes becoming available, like the ones in Thaumcraft.
	 * Use your {@link IRecipeHandler#isRecipeValid(Object)} to determine which recipes are hidden, and when a recipe becomes valid you can add it here.
	 * (note that {@link IRecipeHandler#isRecipeValid(Object)} must be true when the recipe is added here for it to work)
	 *
	 * @deprecated since JEI 4.3.0. This is not supported by Minecraft 1.12 and using this is discouraged.
	 */
	@Deprecated
	void addRecipe(Object recipe);

	/**
	 * Remove a recipe while the game is running.
	 *
	 * @since JEI 4.2.2
	 * @deprecated since JEI 4.3.0. Use {@link #hideRecipe(IRecipeWrapper, String)}
	 */
	@Deprecated
	void removeRecipe(Object recipe);

	/**
	 * Returns an unmodifiable collection of ItemStacks that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IModRegistry#addRecipeCatalyst(Object, String...)}
	 * <p>
	 * This takes the current focus into account, so that if the focus mode is set to Input
	 * and the focus is included in the craftingItems, it is the only one returned.
	 *
	 * @since JEI 3.11.0
	 * @deprecated since JEI 4.2.8. Use {@link #getRecipeCatalysts(IRecipeCategory)}
	 */
	@Deprecated
	List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory, @Nullable IFocus focus);

	/**
	 * Returns an unmodifiable collection of ItemStacks that can craft the recipes from recipeCategory.
	 * For instance, the crafting table ItemStack is returned here for Crafting recipe category.
	 * These are registered with {@link IModRegistry#addRecipeCatalyst(Object, String...)}.
	 *
	 * @since JEI 4.2.8
	 * @deprecated since JEI 4.5.0. Use {@link #getRecipeCatalysts(IRecipeCategory)}
	 */
	@Deprecated
	List<ItemStack> getCraftingItems(IRecipeCategory recipeCategory);
}
