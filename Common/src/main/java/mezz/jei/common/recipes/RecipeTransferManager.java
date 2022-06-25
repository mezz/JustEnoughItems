package mezz.jei.common.recipes;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.common.Constants;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class RecipeTransferManager {
	private final ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers;

	public RecipeTransferManager(ImmutableTable<Class<? extends AbstractContainerMenu>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers) {
		this.recipeTransferHandlers = recipeTransferHandlers;
	}

	@Nullable
	public <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		MenuType<C> menuType;
		try {
			@SuppressWarnings({"UnnecessaryLocalVariable", "unchecked"})
			MenuType<C> cast = (MenuType<C>) container.getType();
			menuType = cast;
		} catch (UnsupportedOperationException ignored) {
			menuType = null;
		}

		RecipeType<R> recipeType = recipeCategory.getRecipeType();
		@SuppressWarnings("unchecked")
		Class<? extends C> containerClass = (Class<? extends C>) container.getClass();
		IRecipeTransferHandler<C, R> handler = getHandler(containerClass, menuType, recipeType);
		if (handler != null) {
			return handler;
		}

		return getHandler(containerClass, menuType, Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE);
	}

	@Nullable
	private <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getHandler(Class<? extends C> containerClass, @Nullable MenuType<C> menuType, RecipeType<?> recipeType) {
		IRecipeTransferHandler<?, ?> handler = recipeTransferHandlers.get(containerClass, recipeType);
		if (handler != null) {
			Optional<? extends MenuType<?>> handlerMenuType = handler.getMenuType();
			if (handlerMenuType.isEmpty() || handlerMenuType.get().equals(menuType)) {
				@SuppressWarnings("unchecked")
				IRecipeTransferHandler<C, R> cast = (IRecipeTransferHandler<C, R>) handler;
				return cast;
			}
		}
		return null;
	}
}
