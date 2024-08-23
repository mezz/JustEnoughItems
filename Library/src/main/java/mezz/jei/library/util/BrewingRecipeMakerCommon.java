package mezz.jei.library.util;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import mezz.jei.common.util.RegistryUtil;
import mezz.jei.library.ingredients.IngredientSet;
import mezz.jei.library.plugins.vanilla.ingredients.subtypes.PotionSubtypeInterpreter;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BrewingRecipeMakerCommon {
	private static final Logger LOGGER = LogManager.getLogger();

	public static Set<IJeiBrewingRecipe> getVanillaBrewingRecipes(
		IVanillaRecipeFactory recipeFactory,
		IIngredientManager ingredientManager,
		PotionBrewing potionBrewing
	) {
		Set<IJeiBrewingRecipe> recipes = new HashSet<>();
		Registry<Potion> potionRegistry = RegistryUtil.getRegistry(Registries.POTION);
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		IngredientSet<ItemStack> knownPotions = getBaseKnownPotions(ingredientManager, potionRegistry, potionBrewing);

		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		IngredientSet<ItemStack> potionReagents = ingredientHelper.getPotionIngredients(potionBrewing)
			.flatMap(i -> Arrays.stream(i.getItems()))
			.collect(Collectors.toCollection(() -> new IngredientSet<>(itemStackHelper, UidContext.Ingredient)));

		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(
				potionBrewing,
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

	private static IngredientSet<ItemStack> getBaseKnownPotions(IIngredientManager ingredientManager, Registry<Potion> potionRegistry, PotionBrewing potionBrewing) {
		IPlatformIngredientHelper ingredientHelper = Services.PLATFORM.getIngredientHelper();
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		IngredientSet<ItemStack> potionContainers = ingredientHelper.getPotionContainers(potionBrewing).stream()
			.flatMap(potionItem -> Arrays.stream(potionItem.getItems()))
			.collect(Collectors.toCollection(() -> new IngredientSet<>(itemStackHelper, UidContext.Ingredient)));

		IngredientSet<ItemStack> knownPotions = new IngredientSet<>(itemStackHelper, UidContext.Ingredient);
		knownPotions.addAll(potionContainers);

		potionRegistry.holders()
			.forEach(potion -> {
				for (ItemStack potionContainer : potionContainers) {
					ItemStack result = PotionContents.createItemStack(potionContainer.getItem(), potion);
					knownPotions.add(result);
				}
			});
		return knownPotions;
	}

	private static List<ItemStack> getNewPotions(
		PotionBrewing potionBrewing,
		IVanillaRecipeFactory recipeFactory,
		IIngredientHelper<ItemStack> itemStackHelper,
		Collection<ItemStack> knownPotions,
		Collection<ItemStack> potionReagents,
		Collection<IJeiBrewingRecipe> recipes
	) {
		List<ItemStack> newPotions = new ArrayList<>();
		for (ItemStack potionInput : knownPotions) {
			Object inputId = itemStackHelper.getUid(potionInput, UidContext.Recipe);
			String inputPathId = PotionSubtypeInterpreter.INSTANCE.getStringName(potionInput);

			for (ItemStack potionReagent : potionReagents) {
				ItemStack potionOutput = getOutput(potionBrewing, potionInput.copy(), potionReagent);
				if (potionOutput.isEmpty()) {
					continue;
				}

				if (potionInput.getItem() instanceof PotionItem && potionOutput.getItem() instanceof PotionItem) {
					Optional<Holder<Potion>> potionOutputType = potionOutput.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion();
					if (potionOutputType.isEmpty()) {
						continue;
					}
				}

				Object outputId = itemStackHelper.getUid(potionOutput, UidContext.Recipe);
				if (Objects.equals(inputId, outputId)) {
					continue;
				}

				ResourceLocation outputResourceLocation = itemStackHelper.getResourceLocation(potionOutput);
				String outputPathId = PotionSubtypeInterpreter.INSTANCE.getStringName(potionOutput);
				String outputModId = outputResourceLocation.getNamespace();
				String uidPath = ResourceLocationUtil.sanitizePath(inputPathId + ".to." + outputPathId);
				IJeiBrewingRecipe recipe = recipeFactory.createBrewingRecipe(
					List.of(potionReagent),
					potionInput.copy(),
					potionOutput,
					ResourceLocation.fromNamespaceAndPath(outputModId, uidPath)
				);
				if (!recipes.contains(recipe)) {
					recipes.add(recipe);
					newPotions.add(potionOutput);
				}
			}
		}
		return newPotions;
	}

	private static ItemStack getOutput(PotionBrewing potionBrewing, ItemStack potion, ItemStack itemStack) {
		try {
			ItemStack result = potionBrewing.mix(itemStack, potion);
			if (result != itemStack) {
				return result;
			}
		} catch (RuntimeException e) {
			String potionInfo = ErrorUtil.getItemStackInfo(potion);
			String itemStackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error(
				"A modded potion mix crashed: \nPotion: {}\nItemStack: {}",
				potionInfo,
				itemStackInfo,
				e
			);
		}
		return ItemStack.EMPTY;
	}
}
