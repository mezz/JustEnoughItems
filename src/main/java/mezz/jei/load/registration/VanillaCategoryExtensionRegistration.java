package mezz.jei.load.registration;

import net.minecraft.item.crafting.ICraftingRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory;

	public VanillaCategoryExtensionRegistration(IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> craftingCategory) {
		this.craftingCategory = craftingCategory;
	}

	@Override
	public IExtendableRecipeCategory<ICraftingRecipe, ICraftingCategoryExtension> getCraftingCategory() {
		return craftingCategory;
	}
}
