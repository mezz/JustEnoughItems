package mezz.jei.plugins.vanilla.crafting;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.ErrorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.StonecutterRecipe;
import net.minecraft.world.item.crafting.UpgradeRecipe;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class VanillaRecipes {
	private final RecipeManager recipeManager;

	public VanillaRecipes() {
		Minecraft minecraft = Minecraft.getInstance();
		ErrorUtil.checkNotNull(minecraft, "minecraft");
		ClientLevel world = minecraft.level;
		ErrorUtil.checkNotNull(world, "minecraft world");
		this.recipeManager = world.getRecipeManager();
	}

	public Map<Boolean, List<CraftingRecipe>> getCraftingRecipes(IRecipeCategory<CraftingRecipe> craftingCategory) {
		CategoryRecipeValidator<CraftingRecipe> validator = new CategoryRecipeValidator<>(craftingCategory, 9);
		return recipeManager.getAllRecipesFor(RecipeType.CRAFTING)
			.stream()
			.filter(validator::isRecipeValid)
			.collect(Collectors.partitioningBy(validator::isRecipeHandled));
	}

	public List<StonecutterRecipe> getStonecuttingRecipes(IRecipeCategory<StonecutterRecipe> stonecuttingCategory) {
		CategoryRecipeValidator<StonecutterRecipe> validator = new CategoryRecipeValidator<>(stonecuttingCategory, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.STONECUTTING, validator);
	}

	public List<SmeltingRecipe> getFurnaceRecipes(IRecipeCategory<SmeltingRecipe> furnaceCategory) {
		CategoryRecipeValidator<SmeltingRecipe> validator = new CategoryRecipeValidator<>(furnaceCategory, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.SMELTING, validator);
	}

	public List<SmokingRecipe> getSmokingRecipes(IRecipeCategory<SmokingRecipe> smokingCategory) {
		CategoryRecipeValidator<SmokingRecipe> validator = new CategoryRecipeValidator<>(smokingCategory, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.SMOKING, validator);
	}

	public List<BlastingRecipe> getBlastingRecipes(IRecipeCategory<BlastingRecipe> blastingCategory) {
		CategoryRecipeValidator<BlastingRecipe> validator = new CategoryRecipeValidator<>(blastingCategory, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.BLASTING, validator);
	}

	public List<CampfireCookingRecipe> getCampfireCookingRecipes(IRecipeCategory<CampfireCookingRecipe> campfireCategory) {
		CategoryRecipeValidator<CampfireCookingRecipe> validator = new CategoryRecipeValidator<>(campfireCategory, 1);
		return getValidHandledRecipes(recipeManager, RecipeType.CAMPFIRE_COOKING, validator);
	}

	public List<UpgradeRecipe> getSmithingRecipes(IRecipeCategory<UpgradeRecipe> smithingCategory) {
		CategoryRecipeValidator<UpgradeRecipe> validator = new CategoryRecipeValidator<>(smithingCategory, 0);
		return getValidHandledRecipes(recipeManager, RecipeType.SMITHING, validator);
	}

	private static <C extends Container, T extends Recipe<C>> List<T> getValidHandledRecipes(
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
