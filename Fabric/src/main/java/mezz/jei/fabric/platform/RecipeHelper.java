package mezz.jei.fabric.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;

import java.util.List;

public class RecipeHelper implements IPlatformRecipeHelper {
	@Override
	public <T extends CraftingRecipe> int getWidth(T recipe) {
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getWidth();
		}
		return 0;
	}

	@Override
	public <T extends CraftingRecipe> int getHeight(T recipe) {
		if (recipe instanceof ShapedRecipe shapedRecipe) {
			return shapedRecipe.getHeight();
		}
		return 0;
	}

	@Override
	public Ingredient getBase(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTransformRecipe transformRecipe) {
			return transformRecipe.base;
		}
		if (recipe instanceof SmithingTrimRecipe trimRecipe) {
			return trimRecipe.base;
		}
		return Ingredient.EMPTY;
	}

	@Override
	public Ingredient getAddition(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTransformRecipe transformRecipe) {
			return transformRecipe.addition;
		}
		if (recipe instanceof SmithingTrimRecipe trimRecipe) {
			return trimRecipe.addition;
		}
		return Ingredient.EMPTY;
	}

	@Override
	public Ingredient getTemplate(SmithingRecipe recipe) {
		if (recipe instanceof SmithingTransformRecipe transformRecipe) {
			return transformRecipe.template;
		}
		if (recipe instanceof SmithingTrimRecipe trimRecipe) {
			return trimRecipe.template;
		}
		return Ingredient.EMPTY;
	}

	@Override
	public boolean isHandled(SmithingRecipe recipe) {
		return recipe instanceof SmithingTransformRecipe || recipe instanceof SmithingTrimRecipe;
	}

	@Override
	public List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory, PotionBrewing potionBrewing) {
		return BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory, potionBrewing);
	}
}
