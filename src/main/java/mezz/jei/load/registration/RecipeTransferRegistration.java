package mezz.jei.load.registration;

import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.collect.Table;
import mezz.jei.config.Constants;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.transfer.BasicRecipeTransferInfo;
import mezz.jei.util.ErrorUtil;

public class RecipeTransferRegistration implements IRecipeTransferRegistration {
	private final Table<Class<?>, ResourceLocation, IRecipeTransferHandler<?>> recipeTransferHandlers = Table.hashBasedTable();
	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IJeiHelpers jeiHelpers;

	public RecipeTransferRegistration(IStackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper, IJeiHelpers jeiHelpers) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IRecipeTransferHandlerHelper getTransferHelper() {
		return handlerHelper;
	}

	@Override
	public <C extends Container> void addRecipeTransferHandler(Class<C> containerClass, ResourceLocation recipeCategoryUid, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		ErrorUtil.checkNotNull(containerClass, "containerClass");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		IRecipeTransferInfo<C> recipeTransferHelper = new BasicRecipeTransferInfo<>(containerClass, recipeCategoryUid, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		addRecipeTransferHandler(recipeTransferHelper);
	}

	@Override
	public <C extends Container> void addRecipeTransferHandler(IRecipeTransferInfo<C> recipeTransferInfo) {
		ErrorUtil.checkNotNull(recipeTransferInfo, "recipeTransferInfo");

		IRecipeTransferHandler<C> recipeTransferHandler = new BasicRecipeTransferHandler<>(stackHelper, handlerHelper, recipeTransferInfo);
		addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeCategoryUid());
	}

	@Override
	public void addRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler, ResourceLocation recipeCategoryUid) {
		ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");
		ErrorUtil.checkNotNull(recipeCategoryUid, "recipeCategoryUid");

		Class<?> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, recipeCategoryUid, recipeTransferHandler);
	}

	@Override
	public void addUniversalRecipeTransferHandler(IRecipeTransferHandler<?> recipeTransferHandler) {
		ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");

		Class<?> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID, recipeTransferHandler);
	}

	public ImmutableTable<Class<?>, ResourceLocation, IRecipeTransferHandler<?>> getRecipeTransferHandlers() {
		return recipeTransferHandlers.toImmutable();
	}
}
