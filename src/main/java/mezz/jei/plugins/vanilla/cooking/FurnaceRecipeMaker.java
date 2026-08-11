package mezz.jei.plugins.vanilla.cooking;

import java.util.Collections;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.util.ResourceLocation;

public final class FurnaceRecipeMaker {
	private static final ResourceLocation SPONGE_RECIPE = new ResourceLocation("minecraft", "sponge");
	private static final ResourceLocation SPONGE_WITH_BUCKET = new ResourceLocation("minecraft", "sponge_with_bucket");

	private FurnaceRecipeMaker() {
	}

	public static List<FurnaceRecipe> getRecipes(RecipeManager recipeManager) {
		IRecipe<?> recipeValue = recipeManager.byKey(SPONGE_RECIPE).orElse(null);
		if (!(recipeValue instanceof FurnaceRecipe)) {
			return Collections.emptyList();
		}
		FurnaceRecipe recipe = (FurnaceRecipe) recipeValue;
		if (!recipe.getIngredients().get(0).test(new ItemStack(Items.WET_SPONGE))) {
			return Collections.emptyList();
		}
		ItemStack output = recipe.getResultItem().copy();
		if (output.isEmpty()) {
			return Collections.emptyList();
		}
		FurnaceRecipe jeiRecipe = new JeiFurnaceRecipe(
			SPONGE_WITH_BUCKET,
			Ingredient.of(Items.WET_SPONGE),
			Ingredient.of(Items.BUCKET),
			new ItemStack(Items.WATER_BUCKET),
			output,
			recipe.getExperience(),
			recipe.getCookingTime()
		);
		return Collections.singletonList(jeiRecipe);
	}
}
