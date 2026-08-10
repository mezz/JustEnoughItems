package mezz.jei.forge.platform;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.category.extensions.vanilla.brewing.IExtendableBrewingRecipeCategory;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformBrewingHelper;
import mezz.jei.common.recipes.BrewingExtensionHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

import java.util.List;

public class BrewingHelper implements IPlatformBrewingHelper {
	@Override
	public void registerCategoryExtensions(
		IExtendableBrewingRecipeCategory brewingCategory,
		IIngredientManager ingredientManager
	) {
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		brewingCategory.addExtension(
			VanillaBrewingRecipe.class,
			new VanillaBrewingRecipeCategoryExtension(ingredientManager)
		);
		brewingCategory.addExtension(BrewingRecipe.class, new BrewingRecipeCategoryExtension(itemStackHelper));
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(
		IIngredientManager ingredientManager,
		IVanillaRecipeFactory vanillaRecipeFactory,
		BrewingExtensionHelper brewingExtensionHelper
	) {
		List<IBrewingRecipe> recipes = BrewingRecipeRegistry.getRecipes();
		return brewingExtensionHelper.getBrewingRecipes(recipes, vanillaRecipeFactory);
	}
}
