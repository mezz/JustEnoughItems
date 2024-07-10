package mezz.jei.common.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;

import java.util.List;

public interface IPlatformRecipeHelper {
	Ingredient getBase(SmithingRecipe recipe);
	Ingredient getAddition(SmithingRecipe recipe);
	Ingredient getTemplate(SmithingRecipe recipe);
	boolean isHandled(SmithingRecipe recipe);

	List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory, PotionBrewing potionBrewing);
}
