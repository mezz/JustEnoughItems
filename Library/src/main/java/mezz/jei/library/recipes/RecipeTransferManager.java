package mezz.jei.library.recipes;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferManager;
import mezz.jei.common.Constants;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RecipeTransferManager implements IRecipeTransferManager {
	private final ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers;
	private final Set<AbstractContainerMenu> unsupportedContainers = new HashSet<>();

	public RecipeTransferManager(ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers) {
		this.recipeTransferHandlers = recipeTransferHandlers;
	}

	@Override
	public <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		MenuType<C> menuType = getMenuType(container);
		RecipeType<R> recipeType = recipeCategory.getRecipeType();
		@SuppressWarnings("unchecked")
		Class<? extends C> containerClass = (Class<? extends C>) container.getClass();
		Optional<IRecipeTransferHandler<C, R>> handler = getHandler(containerClass, menuType, recipeType);
		if (handler.isPresent()) {
			return handler;
		}

		return getHandler(containerClass, menuType, Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE);
	}

	@Nullable
	private <C extends AbstractContainerMenu> MenuType<C> getMenuType(C container) {
		if (unsupportedContainers.contains(container)) {
			return null;
		}
		try {
			@SuppressWarnings({"UnnecessaryLocalVariable", "unchecked"})
			MenuType<C> cast = (MenuType<C>) container.getType();
			return cast;
		} catch (UnsupportedOperationException ignored) {
			unsupportedContainers.add(container);
			return null;
		}
	}

	private <C extends AbstractContainerMenu, R> Optional<IRecipeTransferHandler<C, R>> getHandler(Class<? extends C> containerClass, @Nullable MenuType<C> menuType, RecipeType<?> recipeType) {
		IRecipeTransferHandler<?, ?> handler = recipeTransferHandlers.get(containerClass, recipeType);
		if (handler == null) {
			return Optional.empty();
		}
		Optional<? extends MenuType<?>> handlerMenuType = handler.getMenuType();
		if (handlerMenuType.isEmpty() || handlerMenuType.get().equals(menuType)) {
			@SuppressWarnings("unchecked")
			IRecipeTransferHandler<C, R> cast = (IRecipeTransferHandler<C, R>) handler;
			return Optional.of(cast);
		}
		return Optional.empty();
	}
}
