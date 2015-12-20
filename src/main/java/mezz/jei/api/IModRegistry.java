package mezz.jei.api;

import java.util.List;

import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeTransferHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;

/**
 * Passed to IModPlugins so they can register themselves.
 */
public interface IModRegistry {

	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(IRecipeCategory... recipeCategories);

	/**
	 * Add the recipe handlers provided by this plugin.
	 */
	void addRecipeHandlers(IRecipeHandler... recipeHandlers);

	/**
	 * Add the recipe transfer helpers provided by the plugin.
	 * @deprecated use getRecipeTransferRegistry().addRecipeTransferHandler
	 */
	@Deprecated
	void addRecipeTransferHelpers(IRecipeTransferHelper... recipeTransferHelpers);

	/**
	 * Add a basic recipe transfer helper.
	 * Gives JEI the information it needs to transfer recipes from the player's inventory into the crafting area.
	 * @deprecated use getRecipeTransferRegistry().addRecipeTransferHandler
	 */
	@Deprecated
	void addBasicRecipeTransferHelper(Class<? extends Container> containerClass, String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);

	/**
	 * Add the recipes provided by the plugin.
	 * These can be regular recipes, they will get wrapped by the provided recipe handlers.
	 * Recipes that are already registered with minecraft's recipe managers don't need to be added here.
	 */
	void addRecipes(List recipes);

	/**
	 * Add a description page for an itemStack.
	 * Description pages show in the recipes for an itemStack and tell the player a little bit about it.
	 *
	 * @param itemStacks      the itemStack(s) to describe
	 * @param descriptionKeys Localization keys for description text.
	 *                        New lines can be added with "\n" or by giving multiple descriptionKeys.
	 *                     	  Long lines are wrapped automatically.
	 *                     	  Very long entries will span multiple pages automatically.
	 */
	void addDescription(List<ItemStack> itemStacks, String... descriptionKeys);

	/**
	 * Get the registry for setting up recipe transfer.
	 */
	IRecipeTransferRegistry getRecipeTransferRegistry();

	/** Turns out this isn't useful at all */
	@Deprecated
	void addIgnoredRecipeClasses(Class... ignoredRecipeClasses);
}
