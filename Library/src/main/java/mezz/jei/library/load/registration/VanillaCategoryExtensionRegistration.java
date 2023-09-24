package mezz.jei.library.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.library.runtime.JeiHelpers;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableCraftingRecipeCategory craftingCategory;
	private final JeiHelpers jeiHelpers;

	public VanillaCategoryExtensionRegistration(IExtendableCraftingRecipeCategory craftingCategory, JeiHelpers jeiHelpers) {
		this.craftingCategory = craftingCategory;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IExtendableCraftingRecipeCategory getCraftingCategory() {
		return craftingCategory;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}
}
