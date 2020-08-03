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
import java.util.stream.Collectors;

import net.minecraftforge.common.brewing.BrewingRecipe;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.common.brewing.VanillaBrewingRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ResourceLocation;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.collect.IngredientSet;
import mezz.jei.config.ClientConfig;
import mezz.jei.util.ErrorUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BrewingRecipeMaker {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Set<Class<?>> unhandledRecipeClasses = new HashSet<>();
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
		List<ItemStack> potionReagents = ingredientManager.getAllIngredients(VanillaTypes.ITEM).stream()
			.filter(itemStack -> {
				try {
					return PotionBrewing.isReagent(itemStack);
				} catch (RuntimeException | LinkageError e) {
					String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
					LOGGER.error("Failed to check if item is a potion reagent {}.", itemStackInfo, e);
					return false;
				}
			})
			.collect(Collectors.toList());

		List<ItemStack> basePotions = new ArrayList<>();
		for (Ingredient potionItem : PotionBrewing.POTION_ITEMS) {
			Collections.addAll(basePotions, potionItem.getMatchingStacks());
		}

		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM);
		Collection<ItemStack> knownPotions = IngredientSet.create(itemStackHelper);
		for (Potion potion : ForgeRegistries.POTION_TYPES.getValues()) {
			if (potion == Potions.EMPTY) {
				// skip the "uncraftable" vanilla potions
				continue;
			}
			for (ItemStack input : basePotions) {
				ItemStack result = PotionUtils.addPotionToItemStack(input.copy(), potion);
				knownPotions.add(result);
			}
		}

		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(knownPotions, potionReagents, recipes, vanillaBrewingRecipe);
			foundNewPotions = !newPotions.isEmpty();
			knownPotions.addAll(newPotions);
		} while (foundNewPotions);
	}

	private List<ItemStack> getNewPotions(Collection<ItemStack> knownPotions, List<ItemStack> potionReagents, Collection<IJeiBrewingRecipe> recipes, VanillaBrewingRecipe vanillaBrewingRecipe) {
		List<ItemStack> newPotions = new ArrayList<>();
		for (ItemStack potionInput : knownPotions) {
			for (ItemStack potionReagent : potionReagents) {
				ItemStack potionOutput = vanillaBrewingRecipe.getOutput(potionInput.copy(), potionReagent);
				if (potionOutput.isEmpty()) {
					continue;
				}

				if (potionInput.getItem() == potionOutput.getItem()) {
					Potion potionOutputType = PotionUtils.getPotionFromItem(potionOutput);
					if (potionOutputType == Potions.WATER) {
						continue;
					}

					Potion potionInputType = PotionUtils.getPotionFromItem(potionInput);
					ResourceLocation inputId = ForgeRegistries.POTION_TYPES.getKey(potionInputType);
					ResourceLocation outputId = ForgeRegistries.POTION_TYPES.getKey(potionOutputType);
					if (Objects.equals(inputId, outputId)) {
						continue;
					}
				}

				IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(Collections.singletonList(potionReagent), potionInput.copy(), potionOutput);
				if (!recipes.contains(recipe) && !disabledRecipes.contains(recipe)) {
					if (BrewingRecipeRegistry.hasOutput(potionInput, potionReagent)) {
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
				ItemStack[] ingredients = brewingRecipe.getIngredient().getMatchingStacks();
				if (ingredients.length > 0) {
					Ingredient inputIngredient = brewingRecipe.getInput();
					ItemStack output = brewingRecipe.getOutput();
					ItemStack[] inputs = inputIngredient.getMatchingStacks();
					IJeiBrewingRecipe recipe = vanillaRecipeFactory.createBrewingRecipe(Arrays.asList(ingredients), Arrays.asList(inputs), output);
					recipes.add(recipe);
				}
			} else if (!(iBrewingRecipe instanceof VanillaBrewingRecipe)) {
				Class<?> recipeClass = iBrewingRecipe.getClass();
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
