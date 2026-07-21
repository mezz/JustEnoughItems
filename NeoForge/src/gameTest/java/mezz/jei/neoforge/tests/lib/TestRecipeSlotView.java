package mezz.jei.neoforge.tests.lib;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public record TestRecipeSlotView(RecipeIngredientRole role, List<@Nullable ITypedIngredient<?>> ingredients, List<ItemStack> itemStacks) implements IRecipeSlotView {
	public static TestRecipeSlotView empty() {
		return new TestRecipeSlotView(RecipeIngredientRole.INPUT, List.of(), List.of());
	}

	public static TestRecipeSlotView item(Item item) {
		return item(new ItemStack(item));
	}

	public static TestRecipeSlotView item(ItemStack stack) {
		return items(stack);
	}

	public static TestRecipeSlotView items(ItemStack... stacks) {
		List<ItemStack> itemStacks = Stream.of(stacks)
			.map(ItemStack::copy)
			.toList();
		List<@Nullable ITypedIngredient<?>> ingredients = new ArrayList<>(itemStacks.size());
		for (ItemStack itemStack : itemStacks) {
			ingredients.add(new TestTypedIngredient<>(VanillaTypes.ITEM_STACK, itemStack.copy()));
		}
		return new TestRecipeSlotView(RecipeIngredientRole.INPUT, ingredients, itemStacks);
	}

	public boolean matches(ItemStack stack) {
		return itemStacks.stream()
			.anyMatch(itemStack -> ItemStack.isSameItemSameComponents(itemStack, stack));
	}

	public Ingredient ingredient() {
		Item[] items = itemStacks.stream()
			.map(ItemStack::getItem)
			.toArray(Item[]::new);
		return Ingredient.of(items);
	}

	public String describeItems() {
		return itemStacks.toString();
	}

	@Override
	public Stream<ITypedIngredient<?>> getAllIngredients() {
		return ingredients.stream()
			.filter(Objects::nonNull);
	}

	@Override
	public List<@Nullable ITypedIngredient<?>> getAllIngredientsList() {
		return ingredients;
	}

	@Override
	public Optional<ITypedIngredient<?>> getDisplayedIngredient() {
		return getAllIngredients().findFirst();
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(GuiGraphicsExtractor guiGraphics, int color) {
	}

	@Override
	public Optional<String> getSlotName() {
		return Optional.empty();
	}

	private record TestTypedIngredient<T>(IIngredientType<T> type, T ingredient) implements ITypedIngredient<T> {
		@Override
		public IIngredientType<T> getType() {
			return type;
		}

		@Override
		public T getIngredient() {
			return ingredient;
		}
	}
}
