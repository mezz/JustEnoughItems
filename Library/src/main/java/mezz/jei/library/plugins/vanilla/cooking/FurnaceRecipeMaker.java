package mezz.jei.library.plugins.vanilla.cooking;

import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public final class FurnaceRecipeMaker {
	private static final ResourceKey<Recipe<?>> SPONGE_RECIPE = ResourceKey.create(
		Registries.RECIPE,
		Identifier.withDefaultNamespace("sponge")
	);
	private static final Identifier SPONGE_WITH_BUCKET = Identifier.withDefaultNamespace("sponge_with_bucket");

	private FurnaceRecipeMaker() {
	}

	public static List<RecipeHolder<SmeltingRecipe>> getRecipes(IVanillaRecipeFactory recipeFactory, RecipeMap recipeMap) {
		RecipeHolder<?> recipeHolder = recipeMap.byKey(SPONGE_RECIPE);
		if (recipeHolder == null || !(recipeHolder.value() instanceof SmeltingRecipe recipe)) {
			return List.of();
		}

		ItemStack wetSponge = new ItemStack(Items.WET_SPONGE);
		if (!recipe.input().test(wetSponge)) {
			return List.of();
		}

		ItemStack output = recipe.display()
			.stream()
			.filter(FurnaceRecipeDisplay.class::isInstance)
			.map(FurnaceRecipeDisplay.class::cast)
			.map(FurnaceRecipeDisplay::result)
			.filter(SlotDisplay.ItemStackSlotDisplay.class::isInstance)
			.map(SlotDisplay.ItemStackSlotDisplay.class::cast)
			.map(SlotDisplay.ItemStackSlotDisplay::stack)
			.map(ItemStack::copy)
			.findFirst()
			.orElse(ItemStack.EMPTY);
		if (output.isEmpty()) {
			return List.of();
		}

		SlotDisplay fuel = new SlotDisplay.WithRemainder(
			new SlotDisplay.ItemSlotDisplay(Items.BUCKET),
			new SlotDisplay.ItemSlotDisplay(Items.WATER_BUCKET)
		);
		RecipeHolder<SmeltingRecipe> jeiRecipe = recipeFactory.createSmeltingRecipe(
			Ingredient.of(Items.WET_SPONGE),
			fuel,
			output,
			recipe.cookingTime(),
			recipe.experience(),
			SPONGE_WITH_BUCKET
		);
		return List.of(jeiRecipe);
	}
}
