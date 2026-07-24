package mezz.jei.library.plugins;

import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceLocation;

public interface RecipeCategoryWithType<T> extends IRecipeCategory<T> {
	@Override
	default ResourceLocation getUid() {
		return getRecipeType().getUid();
	}

	@Override
	default Class<? extends T> getRecipeClass() {
		return getRecipeType().getRecipeClass();
	}
}
