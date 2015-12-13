package mezz.jei.plugins.vanilla.brewing;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.brewing.BrewingOreRecipe;
import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;

import mezz.jei.api.JEIManager;
import mezz.jei.util.Log;

public class BrewingRecipeMaker {
	private static final Set<Class> unhandledRecipeClasses = new HashSet<>();

	@Nonnull
	public static List<BrewingRecipeWrapper> getBrewingRecipes() {
		Set<BrewingRecipeWrapper> recipes = new HashSet<>();

		addVanillaBrewingRecipes(recipes);
		addModdedBrewingRecipes(recipes);

		List<BrewingRecipeWrapper> recipeList = new ArrayList<>(recipes);
		Collections.sort(recipeList, new Comparator<BrewingRecipeWrapper>() {
			@Override
			public int compare(BrewingRecipeWrapper o1, BrewingRecipeWrapper o2) {
				return Integer.compare(o1.getBrewingSteps(), o2.getBrewingSteps());
			}
		});

		return recipeList;
	}

	private static void addVanillaBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
		ImmutableList<ItemStack> potionIngredients = JEIManager.itemRegistry.getPotionIngredients();
		VanillaBrewingRecipe vanillaBrewingRecipe = new VanillaBrewingRecipe();
		Set<Integer> potionMetas = new HashSet<>();
		potionMetas.add(0);

		int brewingSteps = 1;
		ItemStack potionInput = new ItemStack(Items.potionitem);
		Set<Integer> newPotionMetas = new HashSet<>();
		do {
			newPotionMetas.clear();
			for (Integer potionInputMeta : potionMetas) {
				potionInput.setItemDamage(potionInputMeta);
				for (ItemStack potionIngredient : potionIngredients) {
					ItemStack potionOutput = vanillaBrewingRecipe.getOutput(potionInput, potionIngredient);
					if (potionOutput != null) {
						int potionOutputMeta = potionOutput.getMetadata();
						if (potionInputMeta != potionOutputMeta) {
							BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(potionIngredient, potionInput.copy(), potionOutput, brewingSteps);
							if (!recipes.contains(recipe)) {
								recipes.add(recipe);
								newPotionMetas.add(potionOutputMeta);
							}
						}
					}
				}
			}
			potionMetas.addAll(newPotionMetas);
			brewingSteps++;
			if (brewingSteps > 100) {
				Log.error("Calculation of vanilla brewing recipes is broken, aborting after 100 brewing steps.");
				return;
			}
		} while (newPotionMetas.size() > 0);
	}

	private static void addModdedBrewingRecipes(Collection<BrewingRecipeWrapper> recipes) {
		List<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			if (iBrewingRecipe instanceof BrewingRecipe) {
				BrewingRecipe brewingRecipe = (BrewingRecipe) iBrewingRecipe;
				BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(brewingRecipe.ingredient, brewingRecipe.input, brewingRecipe.output, 0);
				recipes.add(recipe);
			} else if (iBrewingRecipe instanceof BrewingOreRecipe) {
				BrewingOreRecipe brewingRecipe = (BrewingOreRecipe) iBrewingRecipe;
				BrewingRecipeWrapper recipe = new BrewingRecipeWrapper(brewingRecipe.ingredient, brewingRecipe.input, brewingRecipe.output, 0);
				recipes.add(recipe);
			} else if (!(iBrewingRecipe instanceof VanillaBrewingRecipe)) {
				Class recipeClass = iBrewingRecipe.getClass();
				if (!unhandledRecipeClasses.contains(recipeClass)) {
					unhandledRecipeClasses.add(recipeClass);
					Log.debug("Can't handle brewing recipe class: {}", recipeClass);
				}
			}
		}
	}
}
