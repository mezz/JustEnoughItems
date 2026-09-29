package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Comparator;
import java.util.List;

public final class FuelRecipeMaker {

	private FuelRecipeMaker() {
	}

	public static List<IJeiFuelingRecipe> getFuelRecipes(IIngredientManager ingredientManager, RecipeType<?> recipeType) {
		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		return ingredientManager.getAllItemStacks().stream()
			.<IJeiFuelingRecipe>mapMulti((stack, consumer) -> {
				var fuel = itemStackHelper.getFuelProperties(stack, recipeType);
				if (fuel.burnTime() > 0) {
					consumer.accept(new FuelingRecipe(List.of(stack), fuel.burnTime(), fuel.speedMultiplier()));
				}
			})
			.sorted(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime))
			.toList();
	}
}
