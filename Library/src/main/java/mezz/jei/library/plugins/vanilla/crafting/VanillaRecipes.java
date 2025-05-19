package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class VanillaRecipes {
	private final RecipeMap syncedRecipes;

	public VanillaRecipes(RecipeMap syncedRecipes) {
		this.syncedRecipes = syncedRecipes;
	}

	public CraftingRecipes getCraftingRecipes(IRecipeCategory<RecipeHolder<CraftingRecipe>> craftingCategory) {
		CraftingRecipes result = new CraftingRecipes();

		Collection<RecipeHolder<CraftingRecipe>> craftingRecipes = syncedRecipes.byType(RecipeType.CRAFTING);
		for (RecipeHolder<CraftingRecipe> recipe : craftingRecipes) {
			if (craftingCategory.isHandled(recipe)) {
				result.handled.add(recipe);
			} else {
				result.unhandled.add(recipe);
			}
		}
		return result;
	}

	public static class CraftingRecipes {
		private final List<RecipeHolder<CraftingRecipe>> handled = new ArrayList<>();
		private final List<RecipeHolder<CraftingRecipe>> unhandled = new ArrayList<>();

		public List<RecipeHolder<CraftingRecipe>> getHandled() {
			return Collections.unmodifiableList(handled);
		}

		public List<RecipeHolder<CraftingRecipe>> getUnhandled() {
			return Collections.unmodifiableList(unhandled);
		}
	}

	public List<RecipeHolder<StonecutterRecipe>> getStonecuttingRecipes(IRecipeCategory<RecipeHolder<StonecutterRecipe>> stonecuttingCategory) {
		return getHandledRecipes(syncedRecipes, RecipeType.STONECUTTING, stonecuttingCategory);
	}

	public List<RecipeHolder<SmeltingRecipe>> getFurnaceRecipes(IRecipeCategory<RecipeHolder<SmeltingRecipe>> furnaceCategory) {
		return getHandledRecipes(syncedRecipes, RecipeType.SMELTING, furnaceCategory);
	}

	public List<RecipeHolder<SmokingRecipe>> getSmokingRecipes(IRecipeCategory<RecipeHolder<SmokingRecipe>> smokingCategory) {
		return getHandledRecipes(syncedRecipes, RecipeType.SMOKING, smokingCategory);
	}

	public List<RecipeHolder<BlastingRecipe>> getBlastingRecipes(IRecipeCategory<RecipeHolder<BlastingRecipe>> blastingCategory) {
		return getHandledRecipes(syncedRecipes, RecipeType.BLASTING, blastingCategory);
	}

	public List<RecipeHolder<CampfireCookingRecipe>> getCampfireCookingRecipes(IRecipeCategory<RecipeHolder<CampfireCookingRecipe>> campfireCategory) {
		return getHandledRecipes(syncedRecipes, RecipeType.CAMPFIRE_COOKING, campfireCategory);
	}

	public List<RecipeHolder<SmithingRecipe>> getSmithingRecipes(IRecipeCategory<RecipeHolder<SmithingRecipe>> smithingCategory) {
		return getHandledRecipes(syncedRecipes, RecipeType.SMITHING, smithingCategory);
	}

	public static <C extends RecipeInput, T extends Recipe<C>> List<RecipeHolder<T>> getHandledRecipes(
		RecipeMap syncedRecipes,
		RecipeType<T> recipeType,
		IRecipeCategory<RecipeHolder<T>> recipeCategory
	) {
		return syncedRecipes.byType(recipeType)
			.stream()
			.filter(recipeCategory::isHandled)
			.toList();
	}

}
