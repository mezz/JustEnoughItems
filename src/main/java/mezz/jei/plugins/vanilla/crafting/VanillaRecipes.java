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
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.ErrorUtil;
import net.minecraft.util.ResourceLocation;

public final class VanillaRecipes {
	private final RecipeManager recipeManager;

	public VanillaRecipes() {
		Minecraft minecraft = Minecraft.getInstance();
		ErrorUtil.checkNotNull(minecraft, "minecraft");
		ClientWorld world = Minecraft.getInstance().level;
		ErrorUtil.checkNotNull(world, "minecraft world");
		this.recipeManager = world.getRecipeManager();
	}

	public Map<Boolean, List<ICraftingRecipe>> getCraftingRecipes(IRecipeCategory<ICraftingRecipe> craftingCategory) {
		CategoryRecipeValidator<ICraftingRecipe> validator = new CategoryRecipeValidator<>(craftingCategory, 9);
		Collection<ICraftingRecipe> recipes = getRecipes(recipeManager, IRecipeType.CRAFTING);
		return recipes.parallelStream()
			.filter(validator::isRecipeValid)
			.collect(Collectors.partitioningBy(validator::isRecipeHandled));
	}

	public List<StonecuttingRecipe> getStonecuttingRecipes(IRecipeCategory<StonecuttingRecipe> stonecuttingCategory) {
		CategoryRecipeValidator<StonecuttingRecipe> validator = new CategoryRecipeValidator<>(stonecuttingCategory, 1);
		return getValidHandledRecipes(recipeManager, IRecipeType.STONECUTTING, validator);
	}

	public List<FurnaceRecipe> getFurnaceRecipes(IRecipeCategory<FurnaceRecipe> furnaceCategory) {
		CategoryRecipeValidator<FurnaceRecipe> validator = new CategoryRecipeValidator<>(furnaceCategory, 1);
		return getValidHandledRecipes(recipeManager, IRecipeType.SMELTING, validator);
	}

	public List<SmokingRecipe> getSmokingRecipes(IRecipeCategory<SmokingRecipe> smokingCategory) {
		CategoryRecipeValidator<SmokingRecipe> validator = new CategoryRecipeValidator<>(smokingCategory, 1);
		return getValidHandledRecipes(recipeManager, IRecipeType.SMOKING, validator);
	}

	public List<BlastingRecipe> getBlastingRecipes(IRecipeCategory<BlastingRecipe> blastingCategory) {
		CategoryRecipeValidator<BlastingRecipe> validator = new CategoryRecipeValidator<>(blastingCategory, 1);
		return getValidHandledRecipes(recipeManager, IRecipeType.BLASTING, validator);
	}

	public List<CampfireCookingRecipe> getCampfireCookingRecipes(IRecipeCategory<CampfireCookingRecipe> campfireCategory) {
		CategoryRecipeValidator<CampfireCookingRecipe> validator = new CategoryRecipeValidator<>(campfireCategory, 1);
		return getValidHandledRecipes(recipeManager, IRecipeType.CAMPFIRE_COOKING, validator);
	}

	public List<SmithingRecipe> getSmithingRecipes(IRecipeCategory<SmithingRecipe> smithingCategory) {
		CategoryRecipeValidator<SmithingRecipe> validator = new CategoryRecipeValidator<>(smithingCategory, 0);
		return getValidHandledRecipes(recipeManager, IRecipeType.SMITHING, validator);
	}

	@SuppressWarnings("unchecked")
	private static <C extends IInventory, T extends IRecipe<C>> Collection<T> getRecipes(
		RecipeManager recipeManager,
		IRecipeType<T> recipeType
	) {
		Map<ResourceLocation, IRecipe<C>> recipes = recipeManager.byType(recipeType);
		return (Collection<T>) recipes.values();
	}

	private static <C extends IInventory, T extends IRecipe<C>> List<T> getValidHandledRecipes(
		RecipeManager recipeManager,
		IRecipeType<T> recipeType,
		CategoryRecipeValidator<T> validator
	) {
		return getRecipes(recipeManager, recipeType)
			.stream()
			.filter(r -> validator.isRecipeValid(r) && validator.isRecipeHandled(r))
			.collect(Collectors.toList());
	}

}
