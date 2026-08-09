package mezz.jei.library.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.library.runtime.JeiHelpers;
import net.minecraft.world.item.crafting.CraftingRecipe;

import mezz.jei.api.recipe.category.extensions.IExtendableRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory;
	private final IExtendableSmithingRecipeCategory smithingCategory;
	private final IExtendableBrewingRecipeCategory brewingCategory;
	private final JeiHelpers jeiHelpers;

	public VanillaCategoryExtensionRegistration(
		IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> craftingCategory,
		IExtendableSmithingRecipeCategory smithingCategory,
		IExtendableBrewingRecipeCategory brewingCategory,
		JeiHelpers jeiHelpers
	) {
		this.craftingCategory = craftingCategory;
		this.smithingCategory = smithingCategory;
		this.brewingCategory = brewingCategory;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IExtendableRecipeCategory<CraftingRecipe, ICraftingCategoryExtension> getCraftingCategory() {
		return craftingCategory;
	}

	@Override
	public IExtendableSmithingRecipeCategory getSmithingCategory() {
		return smithingCategory;
	}

	@Override
	public IJeiHelpers getJeiHelpers() {
		return jeiHelpers;
	}

	@Override
	public IExtendableBrewingRecipeCategory getBrewingCategory() {
		return brewingCategory;
	}
}
