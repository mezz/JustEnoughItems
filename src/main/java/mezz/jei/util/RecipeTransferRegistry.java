package mezz.jei.util;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import mezz.jei.config.Constants;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.transfer.BasicRecipeTransferInfo;
import net.minecraft.inventory.Container;

public class RecipeTransferRegistry implements IRecipeTransferRegistry {
	private final Table<Class, String, IRecipeTransferHandler> recipeTransferHandlers = HashBasedTable.create();
	private final StackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;

	public RecipeTransferRegistry(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
	}

	@Override
	public <C extends Container> void addRecipeTransferHandler(@Nullable Class<C> containerClass, @Nullable String recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		Preconditions.checkNotNull(containerClass, "containerClass cannot be null");
		Preconditions.checkNotNull(recipeCategoryUid, "recipeCategoryUid cannot be null");

		IRecipeTransferInfo<C> recipeTransferHelper = new BasicRecipeTransferInfo<C>(containerClass, recipeCategoryUid, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		addRecipeTransferHandler(recipeTransferHelper);
	}

	@Override
	public <C extends Container> void addRecipeTransferHandler(@Nullable IRecipeTransferInfo<C> recipeTransferInfo) {
		Preconditions.checkNotNull(recipeTransferInfo, "recipeTransferInfo cannot be null");

		IRecipeTransferHandler<C> recipeTransferHandler = new BasicRecipeTransferHandler<C>(stackHelper, handlerHelper, recipeTransferInfo);
		addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeCategoryUid());
	}

	@Override
	public void addRecipeTransferHandler(@Nullable IRecipeTransferHandler<?> recipeTransferHandler, @Nullable String recipeCategoryUid) {
		Preconditions.checkNotNull(recipeTransferHandler, "recipeTransferHandler cannot be null");
		Preconditions.checkNotNull(recipeCategoryUid, "recipeCategoryUid cannot be null");

		Class<?> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, recipeCategoryUid, recipeTransferHandler);
	}

	@Override
	public void addUniversalRecipeTransferHandler(@Nullable IRecipeTransferHandler<?> recipeTransferHandler) {
		Preconditions.checkNotNull(recipeTransferHandler, "recipeTransferHandler cannot be null");

		Class<?> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID, recipeTransferHandler);
	}

	public ImmutableTable<Class, String, IRecipeTransferHandler> getRecipeTransferHandlers() {
		return ImmutableTable.copyOf(recipeTransferHandlers);
	}
}
