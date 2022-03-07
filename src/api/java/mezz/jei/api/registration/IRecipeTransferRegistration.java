package mezz.jei.api.registration;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.resources.ResourceLocation;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;

/**
 * Register recipe transfer handlers here to give JEI the information it needs to transfer recipes into the crafting area.
 * Get the instance passed in to your plugin's {@link IModPlugin#registerRecipeTransferHandlers}.
 */
public interface IRecipeTransferRegistration {
	IJeiHelpers getJeiHelpers();

	IRecipeTransferHandlerHelper getTransferHelper();

	/**
	 * Basic method for adding a recipe transfer handler.
	 *
	 * @param containerClass     the class of the container that this recipe transfer handler is for
	 * @param recipeType         the recipe type that this container can use
	 * @param recipeSlotStart    the first slot for recipe inputs
	 * @param recipeSlotCount    the number of slots for recipe inputs
	 * @param inventorySlotStart the first slot of the available inventory (usually player inventory)
	 * @param inventorySlotCount the number of slots of the available inventory
	 *
	 * @since 9.5.0
	 */
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(Class<C> containerClass, RecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);

	/**
	 * Advanced method for adding a recipe transfer handler.
	 *
	 * Use this when recipe slots or inventory slots are spread out in different number ranges.
	 */
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo);

	/**
	 * Complete control over recipe transfer.
	 * Use this when the container has a non-standard inventory or crafting area.
	 *
	 * @since 9.5.0
	 */
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, RecipeType<R> recipeType);

	/**
	 * Add a universal handler that can handle any category of recipe.
	 * Useful for mods with recipe pattern encoding, for automated recipe systems.
	 */
	<C extends AbstractContainerMenu, R> void addUniversalRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler);

	/**
	 * Basic method for adding a recipe transfer handler.
	 *
	 * @param containerClass     the class of the container that this recipe transfer handler is for
	 * @param recipeCategoryUid  the recipe categories that this container can use
	 * @param recipeSlotStart    the first slot for recipe inputs
	 * @param recipeSlotCount    the number of slots for recipe inputs
	 * @param inventorySlotStart the first slot of the available inventory (usually player inventory)
	 * @param inventorySlotCount the number of slots of the available inventory
	 *
	 * @deprecated use {@link #addRecipeTransferHandler(Class, RecipeType, int, int, int, int)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<C extends AbstractContainerMenu> void addRecipeTransferHandler(Class<C> containerClass, ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);

	/**
	 * Complete control over recipe transfer.
	 * Use this when the container has a non-standard inventory or crafting area.
	 *
	 * @deprecated use {@link #addRecipeTransferHandler(IRecipeTransferHandler, RecipeType)} instead.
	 */
	@Deprecated(forRemoval = true, since = "9.5.0")
	<C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, ResourceLocation recipeCategoryUid);
}
