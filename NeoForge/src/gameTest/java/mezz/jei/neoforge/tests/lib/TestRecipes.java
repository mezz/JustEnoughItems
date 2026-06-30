package mezz.jei.neoforge.tests.lib;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public final class TestRecipes {
	private TestRecipes() {
	}

	public static TransferRecipe<TestRecipe> basicRecipe(String id, Item... items) {
		return testRecipe(
			id,
			Stream.of(items)
				.map(TestRecipeSlotView::item)
				.toList()
		);
	}

	public static TransferRecipe<TestRecipe> testRecipe(String id, List<TestRecipeSlotView> inputSlots) {
		return new TransferRecipe<>(new TestRecipe(id), inputSlots);
	}

	public static TransferRecipe<RecipeHolder<CraftingRecipe>> craftingRecipe(
		String idPath,
		ItemStack result,
		List<TestRecipeSlotView> inputSlots
	) {
		Identifier id = Identifier.fromNamespaceAndPath("jeitests", "recipe_transfer/" + idPath);
		ResourceKey<Recipe<?>> resourceKey = ResourceKey.create(Registries.RECIPE, id);
		List<Optional<Ingredient>> ingredients = inputSlots.stream()
			.map(slot -> slot.isEmpty() ? Optional.<Ingredient>empty() : Optional.of(slot.ingredient()))
			.toList();
		CraftingRecipe recipe = new ShapedRecipe(
			new Recipe.CommonInfo(false),
			new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, ""),
			new ShapedRecipePattern(3, 3, ingredients, Optional.empty()),
			ItemStackTemplate.fromNonEmptyStack(result)
		);
		return new TransferRecipe<>(new RecipeHolder<>(resourceKey, recipe), inputSlots);
	}

	public static List<TestRecipeSlotView> grid(RecipeSlotPlacement... placements) {
		List<TestRecipeSlotView> slots = emptyGrid();
		for (RecipeSlotPlacement placement : placements) {
			slots.set(placement.index(), placement.slot());
		}
		return slots;
	}

	public static RecipeSlotPlacement ingredient(int index, Item item) {
		return ingredient(index, TestRecipeSlotView.item(item));
	}

	public static RecipeSlotPlacement ingredient(int index, TestRecipeSlotView slot) {
		return new RecipeSlotPlacement(index, slot);
	}

	private static List<TestRecipeSlotView> emptyGrid() {
		List<TestRecipeSlotView> slots = new ArrayList<>();
		for (int i = 0; i < 9; i++) {
			slots.add(TestRecipeSlotView.empty());
		}
		return slots;
	}

	public record TestRecipe(String id) {
	}

	public record RecipeSlotPlacement(int index, TestRecipeSlotView slot) {
	}
}
