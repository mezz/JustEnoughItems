package mezz.jei.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.ingredients.IIngredientRegistry;

public class BrewingRecipeMaker {

	private final Set<Class> unhandledRecipeClasses = new HashSet<>();
	private final Set<BrewingRecipeWrapper> disabledRecipes = new HashSet<>();
	private final IIngredientRegistry ingredientRegistry;

	public static List<BrewingRecipeWrapper> getBrewingRecipes(IIngredientRegistry ingredientRegistry) {
		BrewingRecipeMaker brewingRecipeMaker = new BrewingRecipeMaker(ingredientRegistry);
		return brewingRecipeMaker.getBrewingRecipes();
	}

	private BrewingRecipeMaker(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
	}

	private List<BrewingRecipeWrapper> getBrewingRecipes() {
		unhandledRecipeClasses.clear();

		Set<BrewingRecipeWrapper> recipes = new HashSet<>();

		addVanillaBrewingRecipes(recipes);
		addModdedBrewingRecipes(recipes);

		List<BrewingRecipeWrapper> recipeList = new ArrayList<>(recipes);
		recipeList.sort(Comparator.comparingInt(BrewingRecipeWrapper::getBrewingSteps));

		return recipeList;
	}

	private void addVanillaBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
		List<ItemStack> potionIngredients = ingredientRegistry.getPotionIngredients();
		List<ItemStack> knownPotions = new ArrayList<>();

		knownPotions.add(BrewingRecipeUtil.WATER_BOTTLE);

		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(knownPotions, potionIngredients, recipes);
			foundNewPotions = !newPotions.isEmpty();
			knownPotions.addAll(newPotions);
		} while (foundNewPotions);
	}

	private List<ItemStack> getNewPotions(List<ItemStack> knownPotions, List<ItemStack> potionIngredients, Collection<BrewingRecipeWrapper> recipes) {
		List<ItemStack> newPotions = new ArrayList<>();
		for (ItemStack potionInput : knownPotions) {
			for (ItemStack potionIngredient : potionIngredients) {
				ItemStack potionOutput = PotionBrewing.doReaction(potionIngredient, potionInput.copy());
				if (potionOutput.equals(potionInput)) {
					continue;
				}

				if (potionInput.getItem() == potionOutput.getItem()) {
					PotionType potionOutputType = PotionUtils.getPotionFromItem(potionOutput);
					if (potionOutputType == PotionTypes.WATER) {
						continue;
					}

					PotionType potionInputType = PotionUtils.getPotionFromItem(potionInput);
					ResourceLocation inputId = ForgeRegistries.POTION_TYPES.getKey(potionInputType);
					ResourceLocation outputId = ForgeRegistries.POTION_TYPES.getKey(potionOutputType);
					if (Objects.equals(inputId, outputId)) {
						continue;
					}
				}

				BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(Collections.singletonList(potionIngredient), potionInput.copy(), potionOutput);
				if (!recipes.contains(recipe) && !disabledRecipes.contains(recipe)) {
					if (BrewingRecipeRegistry.hasOutput(potionInput, potionIngredient)) {
						recipes.add(recipe);
					} else {
						disabledRecipes.add(recipe);
					}
					newPotions.add(potionOutput);
				}
			}
		}
		return newPotions;
	}

	private void addModdedBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
		Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
		addModdedBrewingRecipes(brewingRecipes, recipes);
	}

	private void addModdedBrewingRecipes(Collection<IBrewingRecipe> brewingRecipes, Collection<BrewingRecipeWrapper> recipes) {
		// TODO 1.13
//		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
//			if (iBrewingRecipe instanceof AbstractBrewingRecipe) {
//				AbstractBrewingRecipe brewingRecipe = (AbstractBrewingRecipe) iBrewingRecipe;
//				NonNullList<ItemStack> ingredientList = Internal.getStackHelper().toItemStackList(brewingRecipe.getIngredient());
//
//				if (!ingredientList.isEmpty()) {
//					ItemStack input = brewingRecipe.getInput();
//					// AbstractBrewingRecipe.isInput treats any uncraftable potion here as a water bottle in the brewing stand
//					if (ItemStack.areItemStacksEqual(input, BrewingRecipeUtil.POTION)) {
//						input = BrewingRecipeUtil.WATER_BOTTLE;
//					}
//					ItemStack output = brewingRecipe.getOutput();
//					BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(ingredientList, input, output);
//					recipes.add(recipe);
//				}
//			} else if (!(iBrewingRecipe instanceof VanillaBrewingRecipe)) {
//				Class recipeClass = iBrewingRecipe.getClass();
//				if (!unhandledRecipeClasses.contains(recipeClass)) {
//					unhandledRecipeClasses.add(recipeClass);
//					if (ClientConfig.getInstance().isDebugModeEnabled()) {
//						Log.get().debug("Can't handle brewing recipe class: {}", recipeClass);
//					}
//				}
//			}
//		}
	}
}
