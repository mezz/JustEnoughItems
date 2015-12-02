package mezz.jei.gui;

import javax.annotation.Nonnull;

import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.ICraftingGridHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.recipe.IRecipeTransferHelper;

public class GuiHelper implements IGuiHelper {

	@Nonnull
	@Override
	public IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height) {
		return new DrawableResource(resourceLocation, u, v, width, height);
	}

	@Nonnull
	@Override
	public IDrawable createDrawable(@Nonnull ResourceLocation resourceLocation, int u, int v, int width, int height, int paddingTop, int paddingBottom, int paddingLeft, int paddingRight) {
		return new DrawableResource(resourceLocation, u, v, width, height, paddingTop, paddingBottom, paddingLeft, paddingRight);
	}

	@Nonnull
	@Override
	public ICraftingGridHelper createCraftingGridHelper(int craftInputSlot1, int craftOutputSlot) {
		return new CraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

	@Nonnull
	@Override
	public IRecipeTransferHelper createRecipeTransferHelper(Class<? extends Container> containerClass, String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		return new BasicRecipeTransferHelper(containerClass, recipeCategoryUid, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
	}
}
