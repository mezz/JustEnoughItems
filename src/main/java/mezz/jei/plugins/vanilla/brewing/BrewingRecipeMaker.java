package mezz.jei.plugins.vanilla.brewing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.config.ClientConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrewingRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<Class> unhandledRecipeClasses = new HashSet<>();
	private final Set<IJeiBrewingRecipe> disabledRecipes = new HashSet<>();
	private final IIngredientManager ingredientManager;
	private final IVanillaRecipeFactory vanillaRecipeFactory;

	public static List<IJeiBrewingRecipe> getBrewingRecipes(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
		BrewingRecipeMaker brewingRecipeMaker = new BrewingRecipeMaker(ingredientManager, vanillaRecipeFactory);
		return brewingRecipeMaker.getBrewingRecipes();
	}

	private BrewingRecipeMaker(IIngredientManager ingredientManager, IVanillaRecipeFactory vanillaRecipeFactory) {
		this.ingredientManager = ingredientManager;
		this.vanillaRecipeFactory = vanillaRecipeFactory;
	}

	private List<IJeiBrewingRecipe> getBrewingRecipes() {
		unhandledRecipeClasses.clear();

		Set<IJeiBrewingRecipe> recipes = new HashSet<>();

		Collection<IBrewingRecipe> brewingRecipes = BrewingRecipeRegistry.getRecipes();
		brewingRecipes.stream()
			.filter(r -> r instanceof VanillaBrewingRecipe)
			.map(r -> (VanillaBrewingRecipe) r)
			.findFirst()
			.ifPresent(vanillaBrewingRecipe -> addVanillaBrewingRecipes(recipes, vanillaBrewingRecipe));
		addModdedBrewingRecipes(brewingRecipes, recipes);

		List<IJeiBrewingRecipe> recipeList = new ArrayList<>(recipes);
		recipeList.sort(Comparator.comparingInt(IJeiBrewingRecipe::getBrewingSteps));

		return recipeList;
	}

	private void addVanillaBrewingRecipes(Collection<IJeiBrewingRecipe> recipes, VanillaBrewingRecipe vanillaBrewingRecipe) {
		List<ItemStack> potionIngredients = ingredientManager.getPotionIngredients();
		List<ItemStack> knownPotions = new ArrayList<>();

		knownPotions.add(BrewingRecipeUtil.WATER_BOTTLE);

		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(knownPotions, potionIngredients, recipes, vanillaBrewingRecipe);
			foundNewPotions = !newPotions.isEmpty();
			knownPotions.addAll(newPotions);
		} while (foundNewPotions);
	}

	private List<ItemStack> getNewPotions(List<ItemStack> knownPotions, List<ItemStack> potionIngredients, Collection<IJeiBrewingRecipe> recipes, VanillaBrewingRecipe vanillaBrewingRecipe) {
		List<ItemStack> newPotions = new ArrayList<>();
		for (ItemStack potionInput : knownPotions) {
			for (ItemStack potionIngredient : potionIngredients) {
				ItemStack potionOutput = vanillaBrewingRecipe.getOutput(potionInput.copy(), potionIngredient);
				if (potionOutput.isEmpty()) {
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

				IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(Collections.singletonList(potionIngredient), potionInput.copy(), potionOutput);
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

	private void addModdedBrewingRecipes(Collection<IBrewingRecipe> brewingRecipes, Collection<IJeiBrewingRecipe> recipes) {
		for (IBrewingRecipe iBrewingRecipe : brewingRecipes) {
			if (iBrewingRecipe instanceof BrewingRecipe) {
				BrewingRecipe brewingRecipe = (BrewingRecipe) iBrewingRecipe;
				ItemStack[] stacks = brewingRecipe.getIngredient().getMatchingStacks();
				if (stacks.length > 0) {
					ItemStack input = brewingRecipe.getInput();
					// AbstractBrewingRecipe.isInput treats any uncraftable potion here as a water bottle in the brewing stand
					if (ItemStack.areItemStacksEqual(input, BrewingRecipeUtil.POTION)) {
						input = BrewingRecipeUtil.WATER_BOTTLE;
					}
					ItemStack output = brewingRecipe.getOutput();
					IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(Arrays.asList(stacks), input, output);
					recipes.add(recipe);
				}
			} else if (!(iBrewingRecipe instanceof VanillaBrewingRecipe)) {
				Class recipeClass = iBrewingRecipe.getClass();
				if (!unhandledRecipeClasses.contains(recipeClass)) {
					unhandledRecipeClasses.add(recipeClass);
					if (ClientConfig.getInstance().isDebugModeEnabled()) {
						LOGGER.debug("Can't handle brewing recipe class: {}", recipeClass);
					}
				}
			}
		}
	}
}
