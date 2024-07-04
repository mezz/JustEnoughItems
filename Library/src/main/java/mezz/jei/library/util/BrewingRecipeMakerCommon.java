package mezz.jei.library.util;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.library.ingredients.IngredientSet;
import mezz.jei.common.platform.IPlatformIngredientHelper;
import mezz.jei.common.platform.IPlatformRegistry;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ErrorUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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

public class BrewingRecipeMakerCommon {
	private static final Logger LOGGER = LogManager.getLogger();

	public static Set<IJeiBrewingRecipe> getVanillaBrewingRecipes(
		IVanillaRecipeFactory recipeFactory,
		IIngredientManager ingredientManager,
		IVanillaPotionOutputSupplier vanillaOutputSupplier
	) {
		Set<IJeiBrewingRecipe> recipes = new HashSet<>();
		IPlatformRegistry<Potion> potionRegistry = Services.PLATFORM.getRegistry(Registry.POTION_REGISTRY);
		IngredientSet<ItemStack> knownPotions = getBaseKnownPotions(ingredientManager, potionRegistry);
		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);

		List<ItemStack> potionReagents = ingredientManager.getAllItemStacks().stream()
			.filter(BrewingRecipeMakerCommon::isIngredient)
			.toList();

		boolean foundNewPotions;
		do {
			List<ItemStack> newPotions = getNewPotions(
				recipeFactory,
				itemStackHelper,
				potionRegistry,
				knownPotions,
				potionReagents,
				vanillaOutputSupplier,
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
		List<ItemStack> potionContainers = ingredientHelper.getPotionContainers().stream()
			.flatMap(potionItem -> Arrays.stream(potionItem.getItems()))
			.toList();

		IIngredientHelper<ItemStack> itemStackHelper = ingredientManager.getIngredientHelper(VanillaTypes.ITEM_STACK);
		IngredientSet<ItemStack> knownPotions = IngredientSet.create(itemStackHelper, UidContext.Ingredient);

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
		IPlatformRegistry<Potion> potionRegistry,
		Collection<ItemStack> knownPotions,
		List<ItemStack> potionReagents,
		IVanillaPotionOutputSupplier vanillaOutputSupplier,
		Collection<IJeiBrewingRecipe> recipes
	) {
		List<ItemStack> newPotions = new ArrayList<>();
		for (ItemStack potionInput : knownPotions) {
			String inputId = itemStackHelper.getUniqueId(potionInput, UidContext.Recipe);
			for (ItemStack potionReagent : potionReagents) {
				ItemStack potionInputCopy = potionInput.copy();
				ItemStack potionOutput = getOutput(vanillaOutputSupplier, potionInputCopy, potionReagent);
				if (potionOutput.isEmpty()) {
					continue;
				}

				if (potionInput.getItem() == potionOutput.getItem()) {
					Potion potionOutputType = PotionUtils.getPotion(potionOutput);
					if (potionOutputType == Potions.WATER) {
						continue;
					}

					Potion potionInputType = PotionUtils.getPotion(potionInput);
					ResourceLocation registryInputId = potionRegistry.getRegistryName(potionInputType).orElse(null);
					ResourceLocation registryOutputId = potionRegistry.getRegistryName(potionOutputType).orElse(null);
					if (Objects.equals(registryInputId, registryOutputId)) {
						continue;
					}
				}

				String outputId = itemStackHelper.getUniqueId(potionOutput, UidContext.Recipe);
				if (Objects.equals(inputId, outputId)) {
					continue;
				}

				ResourceLocation uid = createBrewingRecipeUid(itemStackHelper, inputId, outputId, potionOutput);
				IJeiBrewingRecipe recipe = recipeFactory.createBrewingRecipe(
					List.of(potionReagent),
					potionInputCopy,
					potionOutput,
					uid
				);
				IJeiBrewingRecipe existingRecipe = recipes.stream()
					.filter(existing -> hasSameInputAndOutput(itemStackHelper, existing, inputId, outputId))
					.findFirst()
					.orElse(null);
				if (existingRecipe == null) {
					recipes.add(recipe);
					newPotions.add(potionOutput);
				} else {
					// This is a recipe with the same uid and output as an existing recipe,
					// but it has a different reagent.
					// Create a recipe that combines the two.
					IngredientSet<ItemStack> reagents = IngredientSet.create(itemStackHelper, UidContext.Recipe);
					reagents.addAll(existingRecipe.getIngredients());
					reagents.add(potionReagent);
					if (reagents.size() != existingRecipe.getIngredients().size()) {
						IJeiBrewingRecipe replacementRecipe = recipeFactory.createBrewingRecipe(
							List.copyOf(reagents),
							existingRecipe.getPotionInputs(),
							existingRecipe.getPotionOutput(),
							Objects.requireNonNullElse(existingRecipe.getUid(), uid)
						);
						recipes.remove(existingRecipe);
						recipes.add(replacementRecipe);
					}
				}
			}
		}
		return newPotions;
	}

	private static ResourceLocation createBrewingRecipeUid(
		IIngredientHelper<ItemStack> itemStackHelper,
		String inputId,
		String outputId,
		ItemStack potionOutput
	) {
		String outputModId = itemStackHelper.getResourceLocation(potionOutput).getNamespace();
		String uidPath = ResourceLocationUtil.sanitizePath(inputId) + ".to." + ResourceLocationUtil.sanitizePath(outputId);
		return new ResourceLocation(outputModId, uidPath);
	}

	private static boolean hasSameInputAndOutput(
		IIngredientHelper<ItemStack> itemStackHelper,
		IJeiBrewingRecipe recipe,
		String inputId,
		String outputId
	) {
		boolean hasInput = recipe.getPotionInputs()
			.stream()
			.map(input -> itemStackHelper.getUniqueId(input, UidContext.Recipe))
			.anyMatch(inputId::equals);
		if (!hasInput) {
			return false;
		}

		String recipeOutputId = itemStackHelper.getUniqueId(recipe.getPotionOutput(), UidContext.Recipe);
		return Objects.equals(recipeOutputId, outputId);
	}

	@FunctionalInterface
	public interface IVanillaPotionOutputSupplier {
		ItemStack getOutput(ItemStack input, ItemStack ingredient);
	}

	private static ItemStack getOutput(IVanillaPotionOutputSupplier vanillaOutputSupplier, ItemStack potion, ItemStack itemStack) {
		try {
			return vanillaOutputSupplier.getOutput(potion, itemStack);
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
