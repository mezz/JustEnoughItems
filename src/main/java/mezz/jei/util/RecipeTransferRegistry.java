package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Container;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.transfer.BasicRecipeTransferInfo;

public class RecipeTransferRegistry implements IRecipeTransferRegistry {
	private final List<IRecipeTransferHandler> recipeTransferHandlers = new ArrayList<>();

	@Override
	public void addRecipeTransferHandler(@Nullable Class<? extends Container> containerClass, @Nullable String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		if (containerClass == null) {
			Log.error("Null containerClass", new NullPointerException());
			return;
		}
		if (recipeCategoryUid == null) {
			Log.error("Null recipeCategoryUid", new NullPointerException());
			return;
		}

		IRecipeTransferInfo recipeTransferHelper = new BasicRecipeTransferInfo(containerClass, recipeCategoryUid, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		IRecipeTransferHandler recipeTransferHandler = new BasicRecipeTransferHandler(recipeTransferHelper);
		this.recipeTransferHandlers.add(recipeTransferHandler);
	}

	@Override
	public void addRecipeTransferHandler(@Nullable IRecipeTransferInfo recipeTransferInfo) {
		if (recipeTransferInfo == null) {
			Log.error("Null recipeTransferInfo", new NullPointerException());
			return;
		}
		IRecipeTransferHandler recipeTransferHandler = new BasicRecipeTransferHandler(recipeTransferInfo);
		this.recipeTransferHandlers.add(recipeTransferHandler);
	}

	@Override
	public void addRecipeTransferHandler(@Nullable IRecipeTransferHandler recipeTransferHandler) {
		if (recipeTransferHandler == null) {
			Log.error("Null recipeTransferHandler", new NullPointerException());
			return;
		}
		this.recipeTransferHandlers.add(recipeTransferHandler);
	}

	public List<IRecipeTransferHandler> getRecipeTransferHandlers() {
		return recipeTransferHandlers;
	}
}
