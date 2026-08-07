package mezz.jei.library.util;

import mezz.jei.api.recipe.vanilla.IJeiBrewingRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.runtime.IIngredientManager;
import net.minecraft.core.Holder;
import net.minecraft.core.component.predicates.PotionsPredicate;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.BrewingRecipe;
import net.minecraft.world.item.crafting.PotionIngredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class BrewingRecipeMakerCommon {
	private BrewingRecipeMakerCommon() {
	}

	public static List<IJeiBrewingRecipe> getVanillaBrewingRecipes(
		IVanillaRecipeFactory recipeFactory,
		IIngredientManager ingredientManager,
		Collection<RecipeHolder<BrewingRecipe>> brewingRecipes,
		ContextMap contextMap
	) {
		return brewingRecipes.stream()
			.<IJeiBrewingRecipe>mapMulti((recipeHolder, consumer) -> {
				BrewingRecipe recipe = recipeHolder.value();
				List<ItemStack> potionInputs = getItemStacks(recipe.getInput(), ingredientManager, contextMap);
				List<ItemStack> reagents = getItemStacks(recipe.getReagent(), ingredientManager, contextMap);
				if (!potionInputs.isEmpty() && !reagents.isEmpty()) {
					consumer.accept(recipeFactory.createBrewingRecipe(
						reagents,
						potionInputs,
						recipe.getOutput().create(),
						recipeHolder.id().identifier()
					));
				}
			})
			.toList();
	}

	private static List<ItemStack> getItemStacks(
		PotionIngredient potionIngredient,
		IIngredientManager ingredientManager,
		ContextMap contextMap
	) {
		List<ItemStack> baseStacks = potionIngredient.ingredient()
			.display()
			.resolve(contextMap, SlotDisplay.ItemStackContentsFactory.INSTANCE)
			.toList();

		return potionIngredient.potions()
			.flatMap(PotionsPredicate::potions)
			.map(potions -> createPotionStacks(potionIngredient, baseStacks, potions.stream().toList()))
			.orElseGet(() -> {
				if (potionIngredient.potions().isPresent()) {
					return ingredientManager.getAllItemStacks().stream()
						.filter(potionIngredient)
						.toList();
				}
				return baseStacks;
			});
	}

	private static List<ItemStack> createPotionStacks(
		PotionIngredient potionIngredient,
		List<ItemStack> baseStacks,
		List<Holder<Potion>> potions
	) {
		List<ItemStack> potionStacks = new ArrayList<>(baseStacks.size() * potions.size());
		for (ItemStack baseStack : baseStacks) {
			for (Holder<Potion> potion : potions) {
				ItemStack potionStack = PotionContents.createItemStack(baseStack.getItem(), potion);
				if (potionIngredient.test(potionStack)) {
					potionStacks.add(potionStack);
				}
			}
		}
		return potionStacks;
	}
}
