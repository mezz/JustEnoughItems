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
	private final ImmutableTable<Class<?>, ResourceLocation, IRecipeTransferHandler<?>> recipeTransferHandlers;

	public RecipeTransferManager(ImmutableTable<Class<?>, ResourceLocation, IRecipeTransferHandler<?>> recipeTransferHandlers) {
		this.recipeTransferHandlers = recipeTransferHandlers;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends AbstractContainerMenu> IRecipeTransferHandler<? super T> getRecipeTransferHandler(T container, IRecipeCategory<?> recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends AbstractContainerMenu> containerClass = container.getClass();
		IRecipeTransferHandler<?> recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			return (IRecipeTransferHandler<? super T>) recipeTransferHandler;
		}

		return (IRecipeTransferHandler<? super T>) recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}
}
