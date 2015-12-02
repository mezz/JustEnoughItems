package mezz.jei.api;

import javax.annotation.Nonnull;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeTransferHelper;

/**
 * Helps with the implementation of GUIs.
 */
public interface IGuiHelper {

	@Nonnull
	IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height);

	@Nonnull
	IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight);

	@Nonnull
	ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot);

	@Nonnull
	IRecipeTransferHelper createRecipeTransferHelper(Class<? extends Container> containerClass, String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount);
}
