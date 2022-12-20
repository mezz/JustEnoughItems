package mezz.jei.library.transfer;

import java.util.Collection;
import java.util.List;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.Internal;
import mezz.jei.library.gui.ingredients.RecipeSlotsView;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	private final IStackHelper stackHelper;

	public RecipeTransferHandlerHelper(IStackHelper stackHelper) {
		this.stackHelper = stackHelper;
	}

	@Override
	public IRecipeTransferError createInternalError() {
		return RecipeTransferErrorInternal.INSTANCE;
	}

	@Override
	public IRecipeTransferError createUserErrorWithTooltip(Component tooltipMessage) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");

		return new RecipeTransferErrorTooltip(tooltipMessage);
	}

	@Override
	public <C extends AbstractContainerMenu, R> IRecipeTransferInfo<C, R> createBasicRecipeTransferInfo(
		Class<? extends C> containerClass,
		@Nullable MenuType<C> menuType,
		RecipeType<R> recipeType,
		int recipeSlotStart,
		int recipeSlotCount,
		int inventorySlotStart,
		int inventorySlotCount
	) {
		ErrorUtil.checkNotNull(containerClass, "containerClass");
		ErrorUtil.checkNotNull(recipeType, "recipeType");
		return new BasicRecipeTransferInfo<>(containerClass, menuType, recipeType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
	}

	@Override
	public <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> createUnregisteredRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
		ErrorUtil.checkNotNull(recipeTransferInfo, "recipeTransferInfo");
		IConnectionToServer serverConnection = Internal.getServerConnection();
		return new BasicRecipeTransferHandler<>(serverConnection, stackHelper, this, recipeTransferInfo);

	}

	@Override
	public IRecipeTransferError createUserErrorForMissingSlots(Component tooltipMessage, Collection<IRecipeSlotView> missingItemSlots) {
		ErrorUtil.checkNotNull(tooltipMessage, "tooltipMessage");
		ErrorUtil.checkNotEmpty(missingItemSlots, "missingItemSlots");

		return new RecipeTransferErrorMissingSlots(tooltipMessage, missingItemSlots);
	}

	@Override
	public IRecipeSlotsView createRecipeSlotsView(List<IRecipeSlotView> slotViews) {
		return new RecipeSlotsView(slotViews);
	}

	@Override
	public boolean recipeTransferHasServerSupport() {
		IConnectionToServer serverConnection = Internal.getServerConnection();
		return serverConnection.isJeiOnServer();
	}
}
