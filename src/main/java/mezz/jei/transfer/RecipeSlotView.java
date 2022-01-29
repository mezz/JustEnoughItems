package mezz.jei.transfer;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.gui.ingredients.GuiIngredient;
import mezz.jei.ingredients.IngredientTypeHelper;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RecipeSlotView implements IRecipeSlotView {
	private final RecipeIngredientRole role;
	private final int slotIndex;
	private final List<? extends GuiIngredient<?>> ingredients;

	public RecipeSlotView(RecipeIngredientRole role, int slotIndex, List<? extends GuiIngredient<?>> ingredients) {
		this.role = role;
		this.slotIndex = slotIndex;
		this.ingredients = ingredients;
	}

	@Override
	public List<?> getAllIngredients() {
		return this.ingredients.stream()
			.flatMap(guiIngredient -> guiIngredient.getAllIngredients().stream())
			.toList();
	}

	@Override
	public <T> Stream<T> getAllIngredients(IIngredientType<T> ingredientType) {
		return this.ingredients.stream()
			.map(guiIngredient -> IngredientTypeHelper.checkedCast(guiIngredient, ingredientType))
			.filter(Objects::nonNull)
			.flatMap(guiIngredient -> guiIngredient.getAllIngredients().stream());
	}

	@Override
	public int getSlotIndex() {
		return slotIndex;
	}

	@Override
	public RecipeIngredientRole getRole() {
		return role;
	}

	@Override
	public void drawHighlight(PoseStack stack, int color, int xOffset, int yOffset) {
		this.ingredients.stream()
			.findFirst()
			.ifPresent(ingredient -> {
				ingredient.drawHighlight(stack, color, xOffset, yOffset);
			});
	}

	@Override
	public String toString() {
		return "RecipeSlotView{" +
			"role=" + role +
			", slotIndex=" + slotIndex +
			", ingredients=" + ingredients +
			'}';
	}
}
