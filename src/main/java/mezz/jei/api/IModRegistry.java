package mezz.jei.api;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.IAdvancedGuiHandler;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;

/**
 * Entry point for the JEI API, functions for registering recipes are available from here.
 * The IModRegistry instance is passed to your mod plugin in {@link IModPlugin#register(IModRegistry)}.
 */
public interface IModRegistry {

	/**
	 * Get helpers and tools for implementing JEI plugins.
	 *
	 * @since JEI 2.27.0
	 */
	IJeiHelpers getJeiHelpers();

	/**
	 * Get useful functions relating to recipe ingredients.
	 *
	 * @since JEI 3.11.0
	 */
	IIngredientRegistry getIngredientRegistry();

	/**
	 * Add the recipe categories provided by this plugin.
	 */
	void addRecipeCategories(IRecipeCategory... recipeCategories);

	/**
	 * Add the recipe handlers provided by this plugin.
	 */
	void addRecipeHandlers(IRecipeHandler... recipeHandlers);

	/**
	 * Add the recipes provided by the plugin.
	 * These can be regular recipes, they will get wrapped by the provided recipe handlers.
	 * Recipes that are already registered with minecraft's recipe managers don't need to be added here.
	 */
	void addRecipes(Collection recipes);

	/**
	 * Add a clickable area on a gui to jump to specific categories of recipes in JEI.
	 *
	 * @param guiContainerClass  the gui class for JEI to detect.
	 * @param xPos               left x position of the clickable area, relative to the left edge of the gui.
	 * @param yPos               top y position of the clickable area, relative to the top edge of the gui.
	 * @param width              the width of the clickable area.
	 * @param height             the height of the clickable area.
	 * @param recipeCategoryUids the recipe categories that JEI should display.
	 */
	void addRecipeClickArea(Class<? extends GuiContainer> guiContainerClass, int xPos, int yPos, int width, int height, String... recipeCategoryUids);

	/**
	 * Add an association between an item and what it can craft. (i.e. Furnace ItemStack -> Smelting and Fuel Recipes)
	 * Allows players to see what item they need to craft in order to make recipes in that recipe category.
	 *
	 * @param craftingItem       the item that can craft recipes (like a furnace or crafting table item)
	 * @param recipeCategoryUids the recipe categories handled by the item
	 * @since JEI 3.3.0
	 */
	void addRecipeCategoryCraftingItem(ItemStack craftingItem, String... recipeCategoryUids);

	/**
	 * Add a handler to give JEI extra information about how to layout the item list next to a specific type of GuiContainer.
	 * Used for guis with tabs on the side that would normally intersect with JEI's item list.
	 */
	void addAdvancedGuiHandlers(IAdvancedGuiHandler<?>... advancedGuiHandlers);

	/**
	 * Add a description page for an itemStack.
	 * Description pages show in the recipes for an itemStack and tell the player a little bit about it.
	 *
	 * @param itemStack       the itemStack(s) to describe
	 * @param descriptionKeys Localization keys for description text.
	 *                        New lines can be added with "\n" or by giving multiple descriptionKeys.
	 *                        Long lines are wrapped automatically.
	 *                        Very long entries will span multiple pages automatically.
	 */
	void addDescription(ItemStack itemStack, String... descriptionKeys);

	void addDescription(List<ItemStack> itemStacks, String... descriptionKeys);

	/**
	 * Adds an anvil recipe for the given inputs and output.
	 * @param leftInput The itemStack placed on the left slot.
	 * @param rightInputs The itemStack(s) placed on the right slot.
	 * @param outputs The resulting itemStack(s).
	 * @since JEI 4.2.6
	 */
	void addAnvilRecipe(ItemStack leftInput, List<ItemStack> rightInputs, List<ItemStack> outputs);

	/**
	 * Get the registry for setting up recipe transfer.
	 */
	IRecipeTransferRegistry getRecipeTransferRegistry();

	/**
	 * Register your own Recipe Registry Plugin here.
	 *
	 * @see IRecipeRegistryPlugin
	 * @since JEI 3.12.0
	 */
	void addRecipeRegistryPlugin(IRecipeRegistryPlugin recipeRegistryPlugin);
}
