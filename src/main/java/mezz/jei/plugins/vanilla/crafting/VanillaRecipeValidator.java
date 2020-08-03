package mezz.jei.plugins.vanilla.crafting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.BlastingRecipe;
import net.minecraft.item.crafting.CampfireCookingRecipe;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.item.crafting.SmokingRecipe;
import net.minecraft.item.crafting.StonecuttingRecipe;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class VanillaRecipeValidator {
	private static final Logger LOGGER = LogManager.getLogger();

	public static class Results {
		private final List<IRecipe<?>> craftingRecipes = new ArrayList<>();
		private final List<FurnaceRecipe> furnaceRecipes = new ArrayList<>();
		private final List<SmokingRecipe> smokingRecipes = new ArrayList<>();
		private final List<BlastingRecipe> blastingRecipes = new ArrayList<>();
		private final List<CampfireCookingRecipe> campfireRecipes = new ArrayList<>();
		private final List<StonecuttingRecipe> stonecuttingRecipes = new ArrayList<>();

		public List<IRecipe<?>> getCraftingRecipes() {
			return craftingRecipes;
		}

		public List<StonecuttingRecipe> getStonecuttingRecipes() {
			return stonecuttingRecipes;
		}

		public List<FurnaceRecipe> getFurnaceRecipes() {
			return furnaceRecipes;
		}

		public List<SmokingRecipe> getSmokingRecipes() {
			return smokingRecipes;
		}

		public List<BlastingRecipe> getBlastingRecipes() {
			return blastingRecipes;
		}

		public List<CampfireCookingRecipe> getCampfireRecipes() {
			return campfireRecipes;
		}
	}

	private VanillaRecipeValidator() {
	}

	public static Results getValidRecipes(IRecipeCategory<ICraftingRecipe> craftingCategory, IRecipeCategory<StonecuttingRecipe> stonecuttingCategory, IRecipeCategory<FurnaceRecipe> furnaceCategory, IRecipeCategory<SmokingRecipe> smokingCategory, IRecipeCategory<BlastingRecipe> blastingCategory, IRecipeCategory<CampfireCookingRecipe> campfireCategory) {
		CategoryRecipeValidator<ICraftingRecipe> craftingRecipesValidator = new CategoryRecipeValidator<>(craftingCategory, 9);
		CategoryRecipeValidator<StonecuttingRecipe> stonecuttingRecipesValidator = new CategoryRecipeValidator<>(stonecuttingCategory, 1);
		CategoryRecipeValidator<FurnaceRecipe> furnaceRecipesValidator = new CategoryRecipeValidator<>(furnaceCategory, 1);
		CategoryRecipeValidator<SmokingRecipe> smokingRecipesValidator = new CategoryRecipeValidator<>(smokingCategory, 1);
		CategoryRecipeValidator<BlastingRecipe> blastingRecipesValidator = new CategoryRecipeValidator<>(blastingCategory, 1);
		CategoryRecipeValidator<CampfireCookingRecipe> campfireRecipesValidator = new CategoryRecipeValidator<>(campfireCategory, 1);

		Results results = new Results();
		ClientWorld world = Minecraft.getInstance().world;
		ErrorUtil.checkNotNull(world, "minecraft world");
		RecipeManager recipeManager = world.getRecipeManager();
		for (ICraftingRecipe recipe : getRecipes(recipeManager, IRecipeType.CRAFTING)) {
			if (craftingRecipesValidator.isRecipeValid(recipe)) {
				results.craftingRecipes.add(recipe);
			}
		}
		for (StonecuttingRecipe recipe : getRecipes(recipeManager, IRecipeType.STONECUTTING)) {
			if (stonecuttingRecipesValidator.isRecipeValid(recipe)) {
				results.stonecuttingRecipes.add(recipe);
			}
		}
		for (FurnaceRecipe recipe : getRecipes(recipeManager, IRecipeType.SMELTING)) {
			if (furnaceRecipesValidator.isRecipeValid(recipe)) {
				results.furnaceRecipes.add(recipe);
			}

		}
		for (SmokingRecipe recipe : getRecipes(recipeManager, IRecipeType.SMOKING)) {
			if (smokingRecipesValidator.isRecipeValid(recipe)) {
				results.smokingRecipes.add(recipe);
			}
		}
		for (BlastingRecipe recipe : getRecipes(recipeManager, IRecipeType.BLASTING)) {
			if (blastingRecipesValidator.isRecipeValid(recipe)) {
				results.blastingRecipes.add(recipe);
			}
		}
		for (CampfireCookingRecipe recipe : getRecipes(recipeManager, IRecipeType.CAMPFIRE_COOKING)) {
			if (campfireRecipesValidator.isRecipeValid(recipe)) {
				results.campfireRecipes.add(recipe);
			}
		}
		return results;
	}

	private static <C extends IInventory, T extends IRecipe<C>> Collection<T> getRecipes(RecipeManager recipeManager, IRecipeType<T> recipeType) {
		Map<ResourceLocation, IRecipe<C>> recipesMap = recipeManager.getRecipes(recipeType);
		//noinspection unchecked
		return (Collection<T>) recipesMap.values();
	}

	private static final class CategoryRecipeValidator<T extends IRecipe<?>> {
		private static final int INVALID_COUNT = -1;
		private final IRecipeCategory<T> recipeCategory;
		private final int maxInputs;

		public CategoryRecipeValidator(IRecipeCategory<T> recipeCategory, int maxInputs) {
			this.recipeCategory = recipeCategory;
			this.maxInputs = maxInputs;
		}

		@SuppressWarnings("ConstantConditions")
		public boolean isRecipeValid(T recipe) {
			if (recipe.isDynamic()) {
				return false;
			}
			ItemStack recipeOutput = recipe.getRecipeOutput();
			if (recipeOutput == null || recipeOutput.isEmpty()) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has no output. {}", recipeInfo);
				return false;
			}
			List<Ingredient> ingredients = recipe.getIngredients();
			if (ingredients == null) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has no input Ingredients. {}", recipeInfo);
				return false;
			}
			int inputCount = getInputCount(ingredients);
			if (inputCount == INVALID_COUNT) {
				return false;
			} else if (inputCount > maxInputs) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has too many inputs. {}", recipeInfo);
				return false;
			} else if (inputCount == 0) {
				String recipeInfo = getInfo(recipe);
				LOGGER.error("Recipe has no inputs. {}", recipeInfo);
				return false;
			}
			return true;
		}

		private String getInfo(T recipe) {
			return ErrorUtil.getInfoFromRecipe(recipe, recipeCategory);
		}

		@SuppressWarnings("ConstantConditions")
		protected static int getInputCount(List<Ingredient> ingredientList) {
			int inputCount = 0;
			for (Ingredient ingredient : ingredientList) {
				ItemStack[] input = ingredient.getMatchingStacks();
				if (input == null) {
					return INVALID_COUNT;
				} else {
					inputCount++;
				}
			}
			return inputCount;
		}
	}
}
