package mezz.jei.forge.tests.lib;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.ArrayList;
import java.util.List;
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

	public static TransferRecipe<CraftingRecipe> craftingRecipe(
		String idPath,
		ItemStack result,
		List<TestRecipeSlotView> inputSlots
	) {
		ResourceLocation id = new ResourceLocation("jeitests", "recipe_transfer/" + idPath);
		NonNullList<Ingredient> ingredients = NonNullList.withSize(inputSlots.size(), Ingredient.EMPTY);
		for (int i = 0; i < inputSlots.size(); i++) {
			TestRecipeSlotView slot = inputSlots.get(i);
			if (!slot.isEmpty()) {
				ingredients.set(i, slot.ingredient());
			}
		}
		CraftingRecipe recipe = new ShapedRecipe(
			id,
			"",
			3,
			3,
			ingredients,
			result
		);
		return new TransferRecipe<>(recipe, inputSlots);
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
