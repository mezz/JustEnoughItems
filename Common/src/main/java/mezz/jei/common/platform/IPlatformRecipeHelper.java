package mezz.jei.common.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;

import java.util.List;
import java.util.Optional;

public interface IPlatformRecipeHelper {
	Ingredient getBase(SmithingRecipe recipe);
	Optional<Ingredient> getAddition(SmithingRecipe recipe);
	Optional<Ingredient> getTemplate(SmithingRecipe recipe);

	List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory, PotionBrewing potionBrewing);

	String[] shrinkShapedRecipePattern(List<String> pattern);
}
