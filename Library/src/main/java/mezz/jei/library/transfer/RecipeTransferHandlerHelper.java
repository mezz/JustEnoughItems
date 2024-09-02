package mezz.jei.library.transfer;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.common.Internal;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.ImmutableSize2i;
import mezz.jei.library.gui.helpers.CraftingGridHelper;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RecipeTransferHandlerHelper implements IRecipeTransferHandlerHelper {
	private final IStackHelper stackHelper;
	private final IConnectionToServer serverConnection;
	private final @Nullable CraftingRecipeCategory craftingRecipeCategory;

	public RecipeTransferHandlerHelper(IStackHelper stackHelper) {
		this(stackHelper, Internal.getServerConnection(), null);
	}

	public RecipeTransferHandlerHelper(
		IStackHelper stackHelper,
		IConnectionToServer serverConnection
	) {
		this(stackHelper, serverConnection, null);
	}

	public RecipeTransferHandlerHelper(IStackHelper stackHelper, CraftingRecipeCategory craftingRecipeCategory) {
		this(stackHelper, Internal.getServerConnection(), craftingRecipeCategory);
	}

	public RecipeTransferHandlerHelper(
		IStackHelper stackHelper,
		IConnectionToServer serverConnection,
		@Nullable CraftingRecipeCategory craftingRecipeCategory
	) {
		this.stackHelper = stackHelper;
		this.serverConnection = serverConnection;
		this.craftingRecipeCategory = craftingRecipeCategory;
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
		return () -> slotViews;
	}

	@Override
	public boolean recipeTransferHasServerSupport() {
		return serverConnection.isJeiOnServer();
	}

	@Override
	public Map<Integer, Ingredient> getGuiSlotIndexToIngredientMap(CraftingRecipe recipe) {
		ImmutableSize2i recipeSize = craftingRecipeCategory == null ?
			ImmutableSize2i.EMPTY :
			craftingRecipeCategory.getRecipeSize(recipe);
		return CraftingGridHelper.getGuiSlotToIngredientMap(recipe, recipeSize.width(), recipeSize.height());
	}
}
