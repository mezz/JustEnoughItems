package mezz.jei.neoforge.platform;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformBrewingHelper;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import mezz.jei.library.util.BrewingRecipeMakerCommon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.neoforged.neoforge.common.brewing.BrewingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BrewingHelper implements IPlatformBrewingHelper {
	@Override
	public void registerCategoryExtensions(
		IExtendableBrewingRecipeCategory brewingCategory,
		IIngredientManager ingredientManager
	) {
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		brewingCategory.addExtension(BrewingRecipe.class, new BrewingRecipeCategoryExtension(itemStackHelper));
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		PotionBrewing potionBrewing,
		BrewingExtensionHelper brewingExtensionHelper
	) {
		Set<IJeiBrewingRecipe> recipes = BrewingRecipeMakerCommon.getVanillaBrewingRecipes(
			vanillaRecipeFactory,
			ingredientManager,
			potionBrewing
		);
		recipes.addAll(
			brewingExtensionHelper.getBrewingRecipes(
				potionBrewing.getRecipes(),
				vanillaRecipeFactory
			)
		);
		return new ArrayList<>(recipes);
	}
}
