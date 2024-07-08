package mezz.jei.library.util;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.library.ingredients.IngredientSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BrewingRecipeMakerCommon {
	private static final Logger LOGGER = LogManager.getLogger();

	public static Set<IJeiBrewingRecipe> getVanillaBrewingRecipes(
		IVanillaRecipeFactory recipeFactory,
		IIngredientManager ingredientManager
	) {
		Set<IJeiBrewingRecipe> recipes = new HashSet<>();
		IPlatformRegistry<Potion> potionRegistry = Services.PLATFORM.getRegistry(Registries.POTION);
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		IngredientSet<ItemStack> knownPotions = getBaseKnownPotions(ingredientManager, potionRegistry);

		IngredientSet<ItemStack> potionReagents = ingredientManager.getAllItemStacks().stream()
			.filter(BrewingRecipeMakerCommon::isIngredient)
			.collect(Collectors.toCollection(() -> new IngredientSet<>(itemStackHelper, UidContext.Ingredient)));

		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(
				recipeFactory,
				itemStackHelper,
				knownPotions,
				potionReagents,
				recipes
			);
			foundNewPotions = !newPotions.isEmpty();
			knownPotions.addAll(newPotions);
		} while (foundNewPotions);

		return recipes;
	}

	private static boolean isIngredient(ItemStack itemStack) {
		try {
			return PotionBrewing.isIngredient(itemStack);
		} catch (RuntimeException | LinkageError e) {
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Failed to check if item is a potion reagent {}.", itemStackInfo, e);
			return false;
		}
	}


	private static IngredientSet<ItemStack> getBaseKnownPotions(IIngredientManager ingredientManager, IPlatformRegistry<Potion> potionRegistry) {
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		IngredientSet<ItemStack> potionContainers = ingredientHelper.getPotionContainers().stream()
			.flatMap(potionItem -> Arrays.stream(potionItem.getItems()))
			.collect(Collectors.toCollection(() -> new IngredientSet<>(itemStackHelper, UidContext.Ingredient)));

		IngredientSet<ItemStack> knownPotions = new IngredientSet<>(itemStackHelper, UidContext.Ingredient);
		knownPotions.addAll(potionContainers);

		potionRegistry.getValues()
			.filter(potion -> potion != Potions.EMPTY) // skip the "un-craft-able" vanilla potions
			.forEach(potion -> {
				for (ItemStack potionContainer : potionContainers) {
					ItemStack result = PotionUtils.setPotion(potionContainer.copy(), potion);
					knownPotions.add(result);
				}
			});
		return knownPotions;
	}

	private static List<ItemStack> getNewPotions(
		IVanillaRecipeFactory recipeFactory,
		IIngredientHelper<ItemStack> itemStackHelper,
		Collection<ItemStack> knownPotions,
		Collection<ItemStack> potionReagents,
		Collection<IJeiBrewingRecipe> recipes
	) {
		List<ItemStack> newPotions = new ArrayList<>();
		for (ItemStack potionInput : knownPotions) {
			String inputId = itemStackHelper.getUniqueId(potionInput, UidContext.Recipe);
			String inputPathId = ResourceLocationUtil.sanitizePath(inputId);

			for (ItemStack potionReagent : potionReagents) {
				ItemStack potionOutput = getOutput(potionInput.copy(), potionReagent);
				if (potionOutput.isEmpty()) {
					continue;
				}

				if (potionInput.getItem() instanceof PotionItem && potionOutput.getItem() instanceof PotionItem) {
					Potion potionOutputType = PotionUtils.getPotion(potionOutput);
					if (potionOutputType == Potions.WATER) {
						continue;
					}
				}

				String outputId = itemStackHelper.getUniqueId(potionOutput, UidContext.Recipe);
				if (Objects.equals(inputId, outputId)) {
					continue;
				}

				String outputModId = itemStackHelper.getResourceLocation(potionOutput).getNamespace();
				String uidPath = inputPathId + ".to." + ResourceLocationUtil.sanitizePath(outputId);
				IJeiBrewingRecipe recipe = recipeFactory.createBrewingRecipe(
					List.of(potionReagent),
					potionInput.copy(),
					potionOutput,
					new ResourceLocation(outputModId, uidPath)
				);
				if (!recipes.contains(recipe)) {
					recipes.add(recipe);
					newPotions.add(potionOutput);
				}
			}
		}
		return newPotions;
	}

	private static ItemStack getOutput(ItemStack potion, ItemStack itemStack) {
		ItemStack result = PotionBrewing.mix(itemStack, potion);
		if (result != itemStack) {
			return result;
		}
		return ItemStack.EMPTY;
	}
}
