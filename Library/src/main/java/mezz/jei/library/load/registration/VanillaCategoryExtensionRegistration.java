package mezz.jei.library.load.registration;

import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.cooking.IExtendableCookingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.IExtendableCraftingRecipeCategory;
import mezz.jei.api.recipe.category.extensions.vanilla.smithing.IExtendableSmithingRecipeCategory;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.library.runtime.JeiHelpers;

public class VanillaCategoryExtensionRegistration implements IVanillaCategoryExtensionRegistration {
	private final IExtendableCraftingRecipeCategory craftingCategory;
	private final IExtendableSmithingRecipeCategory smithingCategory;
	private final IExtendableBrewingRecipeCategory brewingCategory;
	private final IExtendableCookingRecipeCategory smeltingCategory;
	private final IExtendableCookingRecipeCategory blastingCategory;
	private final IExtendableCookingRecipeCategory smokingCategory;
	private final IExtendableCookingRecipeCategory campfireCookingCategory;
	private final JeiHelpers jeiHelpers;

	public VanillaCategoryExtensionRegistration(
		IExtendableCraftingRecipeCategory craftingCategory,
		IExtendableSmithingRecipeCategory smithingCategory,
		IExtendableBrewingRecipeCategory brewingCategory,
		IExtendableCookingRecipeCategory smeltingCategory,
		IExtendableCookingRecipeCategory blastingCategory,
		IExtendableCookingRecipeCategory smokingCategory,
		IExtendableCookingRecipeCategory campfireCookingCategory,
		JeiHelpers jeiHelpers
	) {
		this.craftingCategory = craftingCategory;
		this.smithingCategory = smithingCategory;
		this.brewingCategory = brewingCategory;
		this.smeltingCategory = smeltingCategory;
		this.blastingCategory = blastingCategory;
		this.smokingCategory = smokingCategory;
		this.campfireCookingCategory = campfireCookingCategory;
		this.jeiHelpers = jeiHelpers;
	}

	@Override
	public IExtendableCraftingRecipeCategory getCraftingCategory() {
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

	@Override
	public IExtendableCookingRecipeCategory getSmeltingCategory() {
		return smeltingCategory;
	}

	@Override
	public IExtendableCookingRecipeCategory getBlastingCategory() {
		return blastingCategory;
	}

	@Override
	public IExtendableCookingRecipeCategory getSmokingCategory() {
		return smokingCategory;
	}

	@Override
	public IExtendableCookingRecipeCategory getCampfireCookingCategory() {
		return campfireCookingCategory;
	}
}
