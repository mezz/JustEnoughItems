package mezz.jei.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.config.Config;
import mezz.jei.util.Java6Helper;
import mezz.jei.util.Log;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraftforge.common.brewing.BrewingOreRecipe;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

public class BrewingRecipeMaker {
	private static final Set<Class> unhandledRecipeClasses = new HashSet<Class>();

	public static List<BrewingRecipeWrapper> getBrewingRecipes(IIngredientRegistry ingredientRegistry) {
		Set<BrewingRecipeWrapper> recipes = new HashSet<BrewingRecipeWrapper>();

		addVanillaBrewingRecipes(ingredientRegistry, recipes);
		addModdedBrewingRecipes(recipes);

		List<BrewingRecipeWrapper> recipeList = new ArrayList<BrewingRecipeWrapper>(recipes);
		Collections.sort(recipeList, new Comparator<BrewingRecipeWrapper>() {
			@Override
			public int compare(BrewingRecipeWrapper o1, BrewingRecipeWrapper o2) {
				return Java6Helper.compare(o1.getBrewingSteps(), o2.getBrewingSteps());
			}
		});

		return recipeList;
	}

	private static void addVanillaBrewingRecipes(IIngredientRegistry ingredientRegistry, Collection<BrewingRecipeWrapper> recipes) {
		ImmutableList<ItemStack> potionIngredients = ingredientRegistry.getPotionIngredients();
		List<ItemStack> knownPotions = new ArrayList<ItemStack>();
		ItemStack waterBottle = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
		knownPotions.add(waterBottle);

		int brewingStep = 1;
		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(brewingStep, knownPotions, potionIngredients, recipes);
			foundNewPotions = !newPotions.isEmpty();
			knownPotions.addAll(newPotions);

			brewingStep++;
			if (brewingStep > 100) {
				Log.error("Calculation of vanilla brewing recipes is broken, aborting after 100 brewing steps.");
				return;
			}
		} while (foundNewPotions);
	}

	private static List<ItemStack> getNewPotions(final int brewingStep, List<ItemStack> knownPotions, ImmutableList<ItemStack> potionIngredients, Collection<BrewingRecipeWrapper> recipes) {
		List<ItemStack> newPotions = new ArrayList<ItemStack>();
		for (ItemStack potionInput : knownPotions) {
			for (ItemStack potionIngredient : potionIngredients) {
				ItemStack potionOutput = PotionHelper.doReaction(potionIngredient, potionInput.copy());
				if (potionOutput == null) {
					continue;
				}

				if (potionInput.getItem() == potionOutput.getItem()) {
					PotionType potionOutputType = PotionUtils.getPotionFromItem(potionOutput);
					if (potionOutputType == PotionTypes.WATER) {
						continue;
					}

					PotionType potionInputType = PotionUtils.getPotionFromItem(potionInput);
					int inputId = PotionType.getID(potionInputType);
					int outputId = PotionType.getID(potionOutputType);
					if (inputId == outputId) {
						continue;
					}
				}

				BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(potionIngredient, potionInput.copy(), potionOutput, brewingStep);
				if (!recipes.contains(recipe)) {
					recipes.add(recipe);
					newPotions.add(potionOutput);
				}
			}
		}
		return newPotions;
	}

	private static void addModdedBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
		List<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			if (iBrewingRecipe instanceof BrewingRecipe) {
				BrewingRecipe brewingRecipe = (BrewingRecipe) iBrewingRecipe;
				BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(brewingRecipe.getIngredient(), brewingRecipe.getInput(), brewingRecipe.getOutput(), 0);
				recipes.add(recipe);
			} else if (iBrewingRecipe instanceof BrewingOreRecipe) {
				BrewingOreRecipe brewingRecipe = (BrewingOreRecipe) iBrewingRecipe;
				BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(brewingRecipe.getIngredient(), brewingRecipe.getInput(), brewingRecipe.getOutput(), 0);
				recipes.add(recipe);
			} else if (!(iBrewingRecipe instanceof VanillaBrewingRecipe)) {
				Class recipeClass = iBrewingRecipe.getClass();
				if (!unhandledRecipeClasses.contains(recipeClass)) {
					unhandledRecipeClasses.add(recipeClass);
					if (Config.isDebugModeEnabled()) {
						Log.debug("Can't handle brewing recipe class: {}", recipeClass);
					}
				}
			}
		}
	}
}
