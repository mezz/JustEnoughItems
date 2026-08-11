package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.List;

public final class FurnaceRecipeMaker {
	private static final ResourceLocation SPONGE_RECIPE = ResourceLocation.withDefaultNamespace("sponge");
	private static final ResourceLocation SPONGE_WITH_BUCKET = ResourceLocation.withDefaultNamespace("sponge_with_bucket");

	private FurnaceRecipeMaker() {
	}

	public static List<RecipeHolder<SmeltingRecipe>> getRecipes(
		IVanillaRecipeFactory recipeFactory,
		RecipeManager recipeManager,
		HolderLookup.Provider registries
	) {
		RecipeHolder<?> recipeHolder = recipeManager.byKey(SPONGE_RECIPE).orElse(null);
		if (recipeHolder == null || !(recipeHolder.value() instanceof SmeltingRecipe recipe)) {
			return List.of();
		}

		ItemStack wetSponge = new ItemStack(Items.WET_SPONGE);
		if (!recipe.getIngredients().getFirst().test(wetSponge)) {
			return List.of();
		}

		ItemStack output = recipe.getResultItem(registries).copy();
		if (output.isEmpty()) {
			return List.of();
		}

		RecipeHolder<SmeltingRecipe> jeiRecipe = recipeFactory.createSmeltingRecipe(
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
