package mezz.jei.load.registration;

import net.minecraft.item.crafting.IRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableRecipeCategory<IRecipe, ICraftingCategoryExtension> craftingCategory;

	public VanillaCategoryExtensionRegistration(IExtendableRecipeCategory<IRecipe, ICraftingCategoryExtension> craftingCategory) {
		this.craftingCategory = craftingCategory;
	}

	@Override
	public IExtendableRecipeCategory<IRecipe, ICraftingCategoryExtension> getCraftingCategory() {
		return craftingCategory;
	}
}
