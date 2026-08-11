package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.List;

public final class FurnaceRecipeMaker {
	private static final ResourceLocation SPONGE_RECIPE = new ResourceLocation("minecraft", "sponge");
	private static final ResourceLocation SPONGE_WITH_BUCKET = new ResourceLocation("minecraft", "sponge_with_bucket");

	private FurnaceRecipeMaker() {
	}

	public static List<SmeltingRecipe> getRecipes(
		IVanillaRecipeFactory recipeFactory,
		RecipeManager recipeManager
	) {
		Recipe<?> recipeValue = recipeManager.byKey(SPONGE_RECIPE).orElse(null);
		if (!(recipeValue instanceof SmeltingRecipe recipe)) {
			return List.of();
		}

		ItemStack wetSponge = new ItemStack(Items.WET_SPONGE);
		if (!recipe.getIngredients().get(0).test(wetSponge)) {
			return List.of();
		}

		ItemStack output = recipe.getResultItem().copy();
		if (output.isEmpty()) {
			return List.of();
		}

		SmeltingRecipe jeiRecipe = recipeFactory.createSmeltingRecipe(
			Ingredient.of(Items.WET_SPONGE),
			Ingredient.of(Items.BUCKET),
			new ItemStack(Items.WATER_BUCKET),
			output,
			recipe.getCookingTime(),
			recipe.getExperience(),
			SPONGE_WITH_BUCKET
		);
		return List.of(jeiRecipe);
	}
}
