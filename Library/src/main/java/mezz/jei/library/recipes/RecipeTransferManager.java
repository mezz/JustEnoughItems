package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferListener;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.common.Constants;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class RecipeTransferManager implements IRecipeTransferManager {
	private final ImmutableTable<Class<? extends AbstractContainerMenu>, IRecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers;
	private final List<IRecipeTransferListener> recipeTransferListeners;

	public RecipeTransferManager(
		ImmutableTable<Class<? extends AbstractContainerMenu>, IRecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers,
		List<IRecipeTransferListener> recipeTransferListeners
	) {
		this.recipeTransferHandlers = recipeTransferHandlers;
		this.recipeTransferListeners = List.copyOf(recipeTransferListeners);
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
