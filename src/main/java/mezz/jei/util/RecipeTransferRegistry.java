package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.transfer.BasicRecipeTransferInfo;
import net.minecraft.inventory.Container;

public class RecipeTransferRegistry implements IRecipeTransferRegistry {
	private final List<IRecipeTransferHandler> recipeTransferHandlers = new ArrayList<IRecipeTransferHandler>();

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
		addRecipeTransferHandler(recipeTransferHelper);
	}

	@Override
	public <C extends Container> void addRecipeTransferHandler(@Nullable IRecipeTransferInfo<C> recipeTransferInfo) {
		if (recipeTransferInfo == null) {
			Log.error("Null recipeTransferInfo", new NullPointerException());
			return;
		}
		IRecipeTransferHandler<C> recipeTransferHandler = new BasicRecipeTransferHandler<C>(recipeTransferInfo);
		addRecipeTransferHandler(recipeTransferHandler);
	}

	@Override
	public void addRecipeTransferHandler(@Nullable IRecipeTransferHandler<?> recipeTransferHandler) {
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
