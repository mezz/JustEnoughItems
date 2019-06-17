package mezz.jei.recipes;

import javax.annotation.Nullable;

import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableTable;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.config.Constants;
import mezz.jei.util.ErrorUtil;

public class RecipeTransferManager {
	private final ImmutableTable<Class, ResourceLocation, IRecipeTransferHandler> recipeTransferHandlers;

	public RecipeTransferManager(ImmutableTable<Class, ResourceLocation, IRecipeTransferHandler> recipeTransferHandlers) {
		this.recipeTransferHandlers = recipeTransferHandlers;
	}

	@Nullable
	public IRecipeTransferHandler getRecipeTransferHandler(Container container, IRecipeCategory recipeCategory) {
		ErrorUtil.checkNotNull(container, "container");
		ErrorUtil.checkNotNull(recipeCategory, "recipeCategory");

		Class<? extends Container> containerClass = container.getClass();
		IRecipeTransferHandler recipeTransferHandler = recipeTransferHandlers.get(containerClass, recipeCategory.getUid());
		if (recipeTransferHandler != null) {
			return recipeTransferHandler;
		}

		return recipeTransferHandlers.get(containerClass, Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
	}
}
