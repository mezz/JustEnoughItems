package mezz.jei.neoforge.platform;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformRecipeHelper;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.crafting.SmithingTrimRecipe;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

public class RecipeHelper implements IPlatformRecipeHelper {
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
	public List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory, PotionBrewing potionBrewing) {
		return BrewingRecipeMaker.getBrewingRecipes(ingredientManager, vanillaRecipeFactory, potionBrewing);
	}

	@Override
	public boolean isItemEnchantable(ItemStack stack, Holder<Enchantment> enchantment) {
		return stack.getItem().isEnchantable(stack) && stack.supportsEnchantment(enchantment);
	}
}
