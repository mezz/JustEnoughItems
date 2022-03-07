package mezz.jei.recipes;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Constants;
import mezz.jei.util.ErrorUtil;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;

public class RecipeTransferManager {
	private final ImmutableTable<Class<?>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers;

	public RecipeTransferManager(ImmutableTable<Class<?>, RecipeType<?>, IRecipeTransferHandler<?, ?>> recipeTransferHandlers) {
		this.recipeTransferHandlers = recipeTransferHandlers;
	}

	@Nullable
	public <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends AbstractContainerMenu> containerClass = container.getClass();
		RecipeType<R> recipeType = recipeCategory.getRecipeType();
		Class<? extends R> recipeClass = recipeType.getRecipeClass();

		IRecipeTransferHandler<C, R> handler = getHandler(recipeClass, containerClass, recipeType);
		if (handler != null) {
			return handler;
		}

		return getHandler(recipeClass, containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_TYPE);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getHandler(Class<? extends R> recipeClass, Class<? extends AbstractContainerMenu> containerClass, RecipeType<?> recipeType) {
		IRecipeTransferHandler<?, ?> handler = recipeTransferHandlers.get(containerClass, recipeType);
		if (handler != null &&
			handler.getRecipeClass().isAssignableFrom(recipeClass) &&
			handler.getContainerClass().isAssignableFrom(containerClass)
		) {
			return (IRecipeTransferHandler<C, R>) handler;
		}
		return null;
	}
}
