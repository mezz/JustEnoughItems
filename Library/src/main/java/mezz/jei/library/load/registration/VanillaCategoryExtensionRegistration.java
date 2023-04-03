package mezz.jei.library.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.library.runtime.JeiHelpers;
import net.minecraft.world.item.crafting.CraftingRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory;
	private final JeiHelpers jeiHelpers;

	public VanillaCategoryExtensionRegistration(IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory, JeiHelpers jeiHelpers) {
		this.craftingCategory = craftingCategory;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> getCraftingCategory() {
		return craftingCategory;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}
}
