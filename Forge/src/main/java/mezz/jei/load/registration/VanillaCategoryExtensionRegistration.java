package mezz.jei.load.registration;

import net.minecraft.world.item.crafting.CraftingRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory;

	public VanillaCategoryExtensionRegistration(IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory) {
		this.craftingCategory = craftingCategory;
	}

	@Override
	public IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> getCraftingCategory() {
		return craftingCategory;
	}
}
