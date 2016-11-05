package mezz.jei.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import mezz.jei.Internal;
import mezz.jei.api.ingredients.IIngredientHelper;
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
import net.minecraftforge.common.brewing.AbstractBrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

public class BrewingRecipeMaker {
	private final Set<Class> unhandledRecipeClasses = new HashSet<Class>();
	private final Map<String, Integer> brewingSteps = new HashMap<String, Integer>();
	private final IIngredientRegistry ingredientRegistry;
	private final IIngredientHelper<ItemStack> itemStackHelper;

	public static List<BrewingRecipeWrapper> getBrewingRecipes(IIngredientRegistry ingredientRegistry) {
		BrewingRecipeMaker brewingRecipeMaker = new BrewingRecipeMaker(ingredientRegistry);
		return brewingRecipeMaker.getBrewingRecipes();
	}

	private BrewingRecipeMaker(IIngredientRegistry ingredientRegistry) {
		this.ingredientRegistry = ingredientRegistry;
		this.itemStackHelper = ingredientRegistry.getIngredientHelper(ItemStack.class);
	}

	private List<BrewingRecipeWrapper> getBrewingRecipes() {
		unhandledRecipeClasses.clear();
		brewingSteps.clear();

		Set<BrewingRecipeWrapper> recipes = new HashSet<BrewingRecipeWrapper>();

		addVanillaBrewingRecipes(recipes);
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

	private void addVanillaBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
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

	private List<ItemStack> getNewPotions(final int brewingStep, List<ItemStack> knownPotions, ImmutableList<ItemStack> potionIngredients, Collection<BrewingRecipeWrapper> recipes) {
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
					String potionUid = itemStackHelper.getUniqueId(potionOutput);
					brewingSteps.put(potionUid, brewingStep);
				}
			}
		}
		return newPotions;
	}

	private void addModdedBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
		Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
		Collection<IBrewingRecipe> unknownSteps = addModdedBrewingRecipes(brewingRecipes, recipes);

		while (unknownSteps.size() > 0) {
			brewingRecipes = unknownSteps;
			unknownSteps = addModdedBrewingRecipes(brewingRecipes, recipes);
			if (unknownSteps.size() == brewingRecipes.size()) {
				return;
			}
		}
	}

	private Collection<IBrewingRecipe> addModdedBrewingRecipes(Collection<IBrewingRecipe> brewingRecipes, Collection<BrewingRecipeWrapper> recipes) {
		Collection<IBrewingRecipe> unknownSteps = new ArrayList<IBrewingRecipe>();

		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			if (iBrewingRecipe instanceof AbstractBrewingRecipe) {
				AbstractBrewingRecipe brewingRecipe = (AbstractBrewingRecipe) iBrewingRecipe;
				List<ItemStack> ingredientList = Internal.getStackHelper().toItemStackList(brewingRecipe.getIngredient());

				if (!ingredientList.isEmpty()) {
					ItemStack input = brewingRecipe.getInput();
					ItemStack output = brewingRecipe.getOutput();
					String potionInputUid = Internal.getStackHelper().getUniqueIdentifierForStack(input);
					String potionOutputUid = Internal.getStackHelper().getUniqueIdentifierForStack(output);

					Integer steps = brewingSteps.get(potionInputUid);
					if (steps == null) {
						unknownSteps.add(brewingRecipe);
					} else {
						int outputBrewingStep = steps + 1;
						brewingSteps.put(potionOutputUid, outputBrewingStep);

						BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(ingredientList, input, output, outputBrewingStep);
						recipes.add(recipe);
					}
				}
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

		return unknownSteps;
	}
}
