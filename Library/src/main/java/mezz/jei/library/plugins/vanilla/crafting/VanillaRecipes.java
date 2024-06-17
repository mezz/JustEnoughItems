package mezz.jei.library.plugins.vanilla.crafting;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class VanillaRecipes {
	private final RecipeManager recipeManager;
	private final IIngredientManager ingredientManager;

	public VanillaRecipes(IIngredientManager ingredientManager) {
		Minecraft minecraft = Minecraft.getInstance();
		ErrorUtil.checkNotNull(minecraft, "minecraft");
		ClientLevel world = minecraft.level;
		ErrorUtil.checkNotNull(world, "minecraft world");
		this.recipeManager = world.getRecipeManager();
		this.ingredientManager = ingredientManager;
	}

	public Map<Boolean, List<RecipeHolder<CraftingRecipe>>> getCraftingRecipes(IRecipeCategory<RecipeHolder<CraftingRecipe>> craftingCategory) {
		var validator = new CategoryRecipeValidator<>(craftingCategory, ingredientManager, 9);
		return recipeManager.getAllRecipesFor(RecipeType.CRAFTING)
			.stream()
			.filter(validator::isRecipeValid)
			.collect(Collectors.partitioningBy(validator::isRecipeHandled));
	}

	public List<RecipeHolder<StonecutterRecipe>> getStonecuttingRecipes(IRecipeCategory<RecipeHolder<StonecutterRecipe>> stonecuttingCategory) {
		var validator = new CategoryRecipeValidator<>(stonecuttingCategory, ingredientManager, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.STONECUTTING, validator);
	}

	public List<RecipeHolder<SmeltingRecipe>> getFurnaceRecipes(IRecipeCategory<RecipeHolder<SmeltingRecipe>> furnaceCategory) {
		CategoryRecipeValidator<SmeltingRecipe> validator = new CategoryRecipeValidator<>(furnaceCategory, ingredientManager, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.SMELTING, validator);
	}

	public List<RecipeHolder<SmokingRecipe>> getSmokingRecipes(IRecipeCategory<RecipeHolder<SmokingRecipe>> smokingCategory) {
		CategoryRecipeValidator<SmokingRecipe> validator = new CategoryRecipeValidator<>(smokingCategory, ingredientManager, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.SMOKING, validator);
	}

	public List<RecipeHolder<BlastingRecipe>> getBlastingRecipes(IRecipeCategory<RecipeHolder<BlastingRecipe>> blastingCategory) {
		CategoryRecipeValidator<BlastingRecipe> validator = new CategoryRecipeValidator<>(blastingCategory, ingredientManager, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.BLASTING, validator);
	}

	public List<RecipeHolder<CampfireCookingRecipe>> getCampfireCookingRecipes(IRecipeCategory<RecipeHolder<CampfireCookingRecipe>> campfireCategory) {
		CategoryRecipeValidator<CampfireCookingRecipe> validator = new CategoryRecipeValidator<>(campfireCategory, ingredientManager, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.CAMPFIRE_COOKING, validator);
	}

	public List<RecipeHolder<SmithingRecipe>> getSmithingRecipes(IRecipeCategory<RecipeHolder<SmithingRecipe>> smithingCategory) {
		CategoryRecipeValidator<SmithingRecipe> validator = new CategoryRecipeValidator<>(smithingCategory, ingredientManager, 0);
		return getValidHandledRecipes(recipeManager, RecipeType.SMITHING, validator);
	}

	private static <C extends RecipeInput, T extends Recipe<C>> List<RecipeHolder<T>> getValidHandledRecipes(
		RecipeManager recipeManager,
		RecipeType<T> recipeType,
		CategoryRecipeValidator<T> validator
	) {
		return recipeManager.getAllRecipesFor(recipeType)
			.stream()
			.filter(r -> validator.isRecipeValid(r) && validator.isRecipeHandled(r))
			.toList();
	}

}
