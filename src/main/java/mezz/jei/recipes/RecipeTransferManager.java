package mezz.jei.recipes;

import javax.annotation.Nullable;

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

	@SuppressWarnings("unchecked")
	@Nullable
	public <C extends AbstractContainerMenu, R> IRecipeTransferHandler<C, R> getRecipeTransferHandler(C container, IRecipeCategory<R> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends AbstractContainerMenu> containerClass = container.getClass();
		IRecipeTransferHandler<?, ?> recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			if (recipeTransferHandler.getRecipeClass().isAssignableFrom(recipeCategory.getRecipeClass())) {
				return (IRecipeTransferHandler<C, R>) recipeTransferHandler;
			}
		}

		return (IRecipeTransferHandler<C, R>) recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}
}
