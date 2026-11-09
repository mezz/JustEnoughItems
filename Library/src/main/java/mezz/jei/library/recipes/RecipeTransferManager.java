package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.Constants;
import mezz.jei.common.transfer.RecipeTransferContext;
import mezz.jei.common.transfer.RecipeTransferErrorInternal;
import mezz.jei.common.transfer.RecipeTransferLifecycleManager;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RecipeTransferManager implements IRecipeTransferManager {
	private static final Logger LOGGER = LogManager.getLogger();

	private final ImmutableTable<Class<? extends AbstractContainerMenu>, IRecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers;
	private final List<IRecipeTransferListener> recipeTransferListeners;
	private final RecipeTransferLifecycleManager recipeTransferLifecycleManager;

	public RecipeTransferManager(
		ImmutableTable<Class<? extends AbstractContainerMenu>, IRecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers,
		List<IRecipeTransferListener> recipeTransferListeners
	) {
		this.recipeTransferHandlers = recipeTransferHandlers;
		this.recipeTransferListeners = List.copyOf(recipeTransferListeners);
		this.recipeTransferLifecycleManager = new RecipeTransferLifecycleManager(this.recipeTransferListeners);
	}

	@Override
	public <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		IRecipeType<R> recipeType = recipeCategory.getRecipeType();
		Optional<IRecipeTransferHandler<C, R>> handler = getHandler(container, recipeType);
		if (handler.isPresent()) {
			return handler;
		}

		return getHandler(container, Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE);
	}

	@Override
	public <C extends AbstractContainerMenu, R> Optional<IRecipeTransferError> transferRecipe(
		AbstractContainerScreen<C> screen,
		IRecipeLayoutDrawable<R> recipeLayout,
		Player player,
		boolean maxTransfer,
		boolean doTransfer
	) {
		ErrorUtil.assertMainThread();
		ErrorUtil.checkNotNull(screen, "screen");
		ErrorUtil.checkNotNull(recipeLayout, "recipeLayout");
		ErrorUtil.checkNotNull(player, "player");
		C container = screen.getMenu();
		if (container != player.containerMenu) {
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}
		IRecipeCategory<R> recipeCategory = recipeLayout.getRecipeCategory();
		Optional<IRecipeTransferHandler<C, R>> recipeTransferHandler = getRecipeTransferHandler(container, recipeCategory);
		if (recipeTransferHandler.isEmpty()) {
			if (doTransfer) {
				LOGGER.error("No Recipe Transfer handler for container {}", container.getClass());
			}
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}

		IRecipeTransferHandler<C, R> transferHandler = recipeTransferHandler.get();
		IRecipeSlotsView recipeSlotsView = recipeLayout.getRecipeSlotsView();
		RecipeTransferContext<R, C> context = new RecipeTransferContext<>(
			recipeTransferLifecycleManager.getNextTransferId(),
			recipeLayout.getRecipe(),
			recipeCategory.getRecipeType(),
			screen,
			recipeSlotsView,
			player,
			maxTransfer,
			recipeTransferLifecycleManager
		);
		if (doTransfer) {
			recipeTransferLifecycleManager.beforeRecipeTransfer(context);
		}

		try {
			IRecipeTransferError transferError = transferHandler.transferRecipe(context, doTransfer);
			return Optional.ofNullable(transferError);
		} catch (RuntimeException e) {
			LOGGER.error(
				"Recipe transfer handler '{}' for container '{}' and recipe type '{}' threw an error: ",
				transferHandler.getClass(), transferHandler.getContainerClass(), recipeCategory.getRecipeType(), e
			);
			return Optional.of(RecipeTransferErrorInternal.INSTANCE);
		}
	}

	@Override
	public List<IRecipeTransferListener> getRecipeTransferListeners() {
		return recipeTransferListeners;
	}

	@Nullable
	private static MenuType<?> getMenuType(AbstractContainerMenu container) {
		try {
			return container.getType();
		} catch (UnsupportedOperationException ignored) {
			return null;
		}
	}

	private <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getHandler(C container, IRecipeType<?> recipeType) {
		IRecipeTransferHandler<?, ?> handler = recipeTransferHandlers.get(container.getClass(), recipeType);
		if (handler == null) {
			return Optional.empty();
		}
		Optional<? extends MenuType<?>> handlerMenuType = handler.getMenuType();
		if (handlerMenuType.isEmpty() || handlerMenuType.get().equals(getMenuType(container))) {
			@SuppressWarnings("unchecked")
			IRecipeTransferHandler<C, R> cast = (IRecipeTransferHandler<C, R>) handler;
			return Optional.of(cast);
		}
		return Optional.empty();
	}
}
