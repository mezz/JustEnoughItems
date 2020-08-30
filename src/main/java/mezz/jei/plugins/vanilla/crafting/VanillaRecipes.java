package mezz.jei.plugins.vanilla.crafting;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.ErrorUtil;
import net.minecraft.util.ResourceLocation;

public final class VanillaRecipes {
	private final RecipeManager recipeManager;

	public VanillaRecipes() {
		ClientWorld world = Minecraft.getInstance().world;
		ErrorUtil.checkNotNull(world, "minecraft world");
		this.recipeManager = world.getRecipeManager();
	}

	public List<ICraftingRecipe> getCraftingRecipes(IRecipeCategory<ICraftingRecipe> craftingCategory) {
		CategoryRecipeValidator<ICraftingRecipe> validator = new CategoryRecipeValidator<>(craftingCategory, 9);
		return getValidRecipes(recipeManager, IRecipeType.CRAFTING, validator);
	}

	public List<StonecuttingRecipe> getStonecuttingRecipes(IRecipeCategory<StonecuttingRecipe> stonecuttingCategory) {
		CategoryRecipeValidator<StonecuttingRecipe> validator = new CategoryRecipeValidator<>(stonecuttingCategory, 1);
		return getValidRecipes(recipeManager, IRecipeType.STONECUTTING, validator);
	}

	public List<FurnaceRecipe> getFurnaceRecipes(IRecipeCategory<FurnaceRecipe> furnaceCategory) {
		CategoryRecipeValidator<FurnaceRecipe> validator = new CategoryRecipeValidator<>(furnaceCategory, 1);
		return getValidRecipes(recipeManager, IRecipeType.SMELTING, validator);
	}

	public List<SmokingRecipe> getSmokingRecipes(IRecipeCategory<SmokingRecipe> smokingCategory) {
		CategoryRecipeValidator<SmokingRecipe> validator = new CategoryRecipeValidator<>(smokingCategory, 1);
		return getValidRecipes(recipeManager, IRecipeType.SMOKING, validator);
	}

	public List<BlastingRecipe> getBlastingRecipes(IRecipeCategory<BlastingRecipe> blastingCategory) {
		CategoryRecipeValidator<BlastingRecipe> validator = new CategoryRecipeValidator<>(blastingCategory, 1);
		return getValidRecipes(recipeManager, IRecipeType.BLASTING, validator);
	}

	public List<CampfireCookingRecipe> getCampfireCookingRecipes(IRecipeCategory<CampfireCookingRecipe> campfireCategory) {
		CategoryRecipeValidator<CampfireCookingRecipe> campfireRecipesValidator = new CategoryRecipeValidator<>(campfireCategory, 1);
		return getValidRecipes(recipeManager, IRecipeType.CAMPFIRE_COOKING, campfireRecipesValidator);
	}

	@SuppressWarnings("unchecked")
	private static <C extends IInventory, T extends IRecipe<C>> Collection<T> getRecipes(
		RecipeManager recipeManager,
		IRecipeType<T> recipeType
	) {
		Map<ResourceLocation, IRecipe<C>> recipes = recipeManager.getRecipes(recipeType);
		return (Collection<T>) recipes.values();
	}

	private static <C extends IInventory, T extends IRecipe<C>> List<T> getValidRecipes(
		RecipeManager recipeManager,
		IRecipeType<T> recipeType,
		CategoryRecipeValidator<T> validator
	) {
		return getRecipes(recipeManager, recipeType)
			.stream()
			.filter(validator::isRecipeValid)
			.collect(Collectors.toList());
	}

}
