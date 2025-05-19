package mezz.jei.library.plugins.vanilla.cooking.fuel;

import mezz.jei.api.recipe.vanilla.IJeiFuelingRecipe;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class FuelRecipeMaker {

	private FuelRecipeMaker() {
	}

	public static List<IJeiFuelingRecipe> getFuelRecipes(IIngredientManager ingredientManager, RecipeType<?> recipeType) {
		ClientLevel level = Objects.requireNonNull(Minecraft.getInstance().level);

		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		return ingredientManager.getAllItemStacks().stream()
			.<IJeiFuelingRecipe>mapMulti((stack, consumer) -> {
				int burnTime = itemStackHelper.getBurnTime(stack, recipeType, level.fuelValues());
				if (burnTime > 0) {
					consumer.accept(new FuelingRecipe(List.of(stack), burnTime));
				}
			})
			.sorted(Comparator.comparingInt(IJeiFuelingRecipe::getBurnTime))
			.toList();
	}
}
