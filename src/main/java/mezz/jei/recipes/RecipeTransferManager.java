package mezz.jei.recipes;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Constants;
import mezz.jei.util.ErrorUtil;

public class RecipeTransferManager {
	private final ImmutableTable<Class<?>, ResourceLocation, IRecipeTransferHandler<?, ?>> recipeTransferHandlers;

	public RecipeTransferManager(ImmutableTable<Class<?>, ResourceLocation, IRecipeTransferHandler<?, ?>> recipeTransferHandlers) {
		this.recipeTransferHandlers = recipeTransferHandlers;
	}

	@Nullable
	public <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends AbstractContainerMenu> containerClass = container.getClass();
		Class<? extends R> recipeClass = recipeCategory.getRecipeClass();

		IRecipeTransferHandler<C, R> handler = getHandler(recipeClass, containerClass, recipeCategory.getUid());
		if (handler != null) {
			return handler;
		}

		return getHandler(recipeClass, containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	private <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getHandler(Class<? extends R> recipeClass, Class<? extends AbstractContainerMenu> containerClass, ResourceLocation recipeCategoryUid) {
		IRecipeTransferHandler<?, ?> handler = recipeTransferHandlers.get(containerClass, recipeCategoryUid);
		if (handler != null &&
			handler.getRecipeClass().isAssignableFrom(recipeClass) &&
			handler.getContainerClass().isAssignableFrom(containerClass)
		) {
			return (IRecipeTransferHandler<C, R>) handler;
		}
		return null;
	}
}
