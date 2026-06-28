package mezz.jei.neoforge.tests.recipe.transfer;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.api.recipe.transfer.IUniversalRecipeTransferHandler;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.library.plugins.vanilla.crafting.CraftingCategoryExtension;
import mezz.jei.library.plugins.vanilla.crafting.CraftingRecipeCategory;
import mezz.jei.library.transfer.RecipeTransferHandlerHelper;
import mezz.jei.neoforge.tests.lib.TestGuiHelper;
import mezz.jei.neoforge.tests.lib.TestStackHelper;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

final class TestRecipeTransferRegistration implements IRecipeTransferRegistration {
	private final Map<RecipeTransferKey, IRecipeTransferHandler<?, ?>> recipeTransferHandlers = new HashMap<>();
	private final IRecipeTransferHandlerHelper handlerHelper;

	public TestRecipeTransferRegistration(IConnectionToServer serverConnection) {
		var stackHelper = new TestStackHelper();
		this.handlerHelper = new RecipeTransferHandlerHelper(stackHelper, createCraftingCategory(), serverConnection);
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IRecipeTransferHandlerHelper getTransferHelper() {
		return handlerHelper;
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(
		Class<? extends C> containerClass,
		@Nullable MenuType<C> menuType,
		IRecipeType<R> recipeType,
		int recipeSlotStart,
		int recipeSlotCount,
		int inventorySlotStart,
		int inventorySlotCount
	) {
		IRecipeTransferInfo<C, R> transferInfo = handlerHelper.createBasicRecipeTransferInfo(containerClass, menuType, recipeType, recipeSlotStart, recipeSlotCount, inventorySlotStart, inventorySlotCount);
		addRecipeTransferHandler(transferInfo);
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferInfo<C, R> recipeTransferInfo) {
		IRecipeTransferHandler<C, R> recipeTransferHandler = handlerHelper.createUnregisteredRecipeTransferHandler(recipeTransferInfo);
		addRecipeTransferHandler(recipeTransferHandler, recipeTransferInfo.getRecipeType());
	}

	@Override
	public <C extends AbstractContainerMenu, R> void addRecipeTransferHandler(IRecipeTransferHandler<C, R> recipeTransferHandler, IRecipeType<R> recipeType) {
		RecipeTransferKey key = new RecipeTransferKey(recipeTransferHandler.getContainerClass(), recipeType);
		recipeTransferHandlers.put(key, recipeTransferHandler);
	}

	@Override
	public <C extends AbstractContainerMenu> void addUniversalRecipeTransferHandler(IUniversalRecipeTransferHandler<C> universalRecipeTransferHandler) {
		throw new UnsupportedOperationException();
	}

	public <C extends AbstractContainerMenu> IRecipeTransferHandler<C, Object> getTransferHandler(C menu, IRecipeType<?> recipeType) {
		IRecipeTransferHandler<?, ?> handler = recipeTransferHandlers.get(new RecipeTransferKey(menu.getClass(), recipeType));
		if (handler == null) {
			throw new IllegalStateException("No recipe transfer handler for %s and %s".formatted(menu.getClass(), recipeType.getUid()));
		}

		@SuppressWarnings("unchecked")
		IRecipeTransferHandler<C, Object> castHandler = (IRecipeTransferHandler<C, Object>) handler;
		return castHandler;
	}

	private static CraftingRecipeCategory createCraftingCategory() {
		CraftingRecipeCategory craftingRecipeCategory = new CraftingRecipeCategory(TestGuiHelper.INSTANCE);
		craftingRecipeCategory.addExtension(CraftingRecipe.class, new CraftingCategoryExtension());
		return craftingRecipeCategory;
	}

	private record RecipeTransferKey(Class<? extends AbstractContainerMenu> menuClass, IRecipeType<?> recipeType) {
	}
}
