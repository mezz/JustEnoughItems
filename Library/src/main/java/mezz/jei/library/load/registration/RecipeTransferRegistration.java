package mezz.jei.library.load.registration;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.common.Constants;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.library.transfer.BasicRecipeTransferHandler;
import mezz.jei.library.transfer.BasicRecipeTransferInfo;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.core.collect.Table;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class RecipeTransferRegistration implements IRecipeTransferRegistration {
	private final Table<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers = Table.hashBasedTable();
	private final IStackHelper stackHelper;
	private final IRecipeTransferHandlerHelper handlerHelper;
	private final IJeiHelpers jeiHelpers;
	private final IConnectionToServer serverConnection;

	public RecipeTransferRegistration(
		IStackHelper stackHelper,
		IRecipeTransferHandlerHelper handlerHelper,
		IJeiHelpers jeiHelpers,
		IConnectionToServer serverConnection
	) {
		this.stackHelper = stackHelper;
		this.handlerHelper = handlerHelper;
		this.jeiHelpers = jeiHelpers;
		this.serverConnection = serverConnection;
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
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(Class<? extends C> containerClass, @Nullable MenuType<C> menuType, RecipeType<R> recipeType, int recipeSlotStart, int recipeSlotCount, int inventorySlotStart, int inventorySlotCount) {
		ErrorUtil.checkNotNull(containerClass, "containerClass");
		ErrorUtil.checkNotNull(recipeType, "recipeType");

		IRecipeTransferInfo<C, R> recipeTransferInfo = new BasicRecipeTransferInfo<>(containerClass, menuType, recipeType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		addRecipeTransferHandler(recipeTransferInfo);
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
		ErrorUtil.checkNotNull(recipeTransferInfo, "recipeTransferInfo");

		IRecipeTransferHandler<C, R> recipeTransferHandler = new BasicRecipeTransferHandler<>(serverConnection, stackHelper, handlerHelper, recipeTransferInfo);
		addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeType());
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, RecipeType<R> recipeType) {
		ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");
		ErrorUtil.checkNotNull(recipeType, "recipeType");

		Class<? extends C> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, recipeType, recipeTransferHandler);
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addUniversalRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler) {
		ErrorUtil.checkNotNull(recipeTransferHandler, "recipeTransferHandler");

		Class<? extends C> containerClass = recipeTransferHandler.getContainerClass();
		this.recipeTransferHandlers.put(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE, recipeTransferHandler);
	}

	public ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> getRecipeTransferHandlers() {
		return recipeTransferHandlers.toImmutable();
	}
}
