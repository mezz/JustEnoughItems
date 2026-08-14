package mezz.jei.gui.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.common.gui.IngredientGridTooltipComponent;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class InteractiveIngredientGridTooltipComponent extends IngredientGridTooltipComponent<ITypedIngredient<?>> {
	private final List<IRecipeSlotDrawable> slots;

	public InteractiveIngredientGridTooltipComponent(IRecipeManager recipeManager, List<ITypedIngredient<?>> ingredients) {
		super(ingredients);
		this.slots = new ArrayList<>(ingredients.size());
		for (ITypedIngredient<?> ingredient : ingredients) {
			IRecipeSlotDrawable slot = recipeManager.createRecipeSlotDrawable(
				RecipeIngredientRole.OUTPUT,
				List.of(Optional.of(ingredient)),
				Set.of(0),
				0
			);
			this.slots.add(slot);
		}
	}

	@Override
	protected void drawIngredient(
		GuiGraphics guiGraphics,
		ITypedIngredient<?> ingredient,
		int index,
		int x,
		int y,
		boolean hovered
	) {
		IRecipeSlotDrawable slot = this.slots.get(index);
		slot.setPosition(x, y);
		slot.draw(guiGraphics, hovered);
	}

	public Stream<IClickableIngredientInternal<?>> getIngredientUnderMouse(double mouseX, double mouseY) {
		int index = getIngredientIndexUnderMouse(mouseX, mouseY);
		if (index < 0) {
			return Stream.empty();
		}
		ITypedIngredient<?> ingredient = getIngredient(index);
		return Stream.of(createCandidateIngredient(ingredient, index));
	}

	public Optional<ITypedIngredient<?>> getTypedIngredientUnderMouse(double mouseX, double mouseY) {
		int index = getIngredientIndexUnderMouse(mouseX, mouseY);
		if (index < 0) {
			return Optional.empty();
		}
		return Optional.of(getIngredient(index));
	}

	private <T> IClickableIngredientInternal<T> createCandidateIngredient(ITypedIngredient<T> ingredient, int index) {
		IElement<T> element = new IngredientElement<>(ingredient);
		return new ClickableIngredientInternal<>(element, (mouseX, mouseY) -> getIngredientIndexUnderMouse(mouseX, mouseY) == index, false, true);
	}
}
