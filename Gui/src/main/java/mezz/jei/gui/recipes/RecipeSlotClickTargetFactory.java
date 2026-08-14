package mezz.jei.gui.recipes;

import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.gui.input.ClickableIngredientInternal;
import mezz.jei.gui.input.IClickableIngredientInternal;
import mezz.jei.gui.overlay.elements.IElement;
import mezz.jei.gui.overlay.elements.IngredientElement;
import mezz.jei.gui.overlay.elements.TagIngredientElement;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.BooleanSupplier;

final class RecipeSlotClickTargetFactory {
	private final IRecipeManager recipeManager;
	private final BooleanSupplier isRecipeCyclingPaused;

	RecipeSlotClickTargetFactory(IRecipeManager recipeManager, BooleanSupplier isRecipeCyclingPaused) {
		this.recipeManager = recipeManager;
		this.isRecipeCyclingPaused = isRecipeCyclingPaused;
	}

	Optional<IClickableIngredientInternal<?>> create(RecipeSlotUnderMouse slotUnderMouse) {
		return slotUnderMouse.slot()
			.getDisplayedIngredient()
			.map(ingredient -> create(slotUnderMouse, ingredient));
	}

	private <T> IClickableIngredientInternal<T> create(
		RecipeSlotUnderMouse slotUnderMouse,
		ITypedIngredient<T> ingredient
	) {
		IElement<T> element = createElement(slotUnderMouse.slot(), ingredient);
		return new ClickableIngredientInternal<>(element, slotUnderMouse::isMouseOver, false, true);
	}

	private <T> IElement<T> createElement(IRecipeSlotView slot, ITypedIngredient<T> ingredient) {
		return getNavigableTag(slot)
			.<IElement<T>>map(tagKey -> new TagIngredientElement<>(
				ingredient,
				tagKey,
				this.recipeManager,
				this.isRecipeCyclingPaused
			))
			.orElseGet(() -> new IngredientElement<>(ingredient));
	}

	private Optional<TagKey<?>> getNavigableTag(IRecipeSlotView slot) {
		if (this.isRecipeCyclingPaused.getAsBoolean()) {
			return Optional.empty();
		}
		Optional<TagKey<?>> tagKey = slot.getTagKey();
		if (tagKey.isEmpty()) {
			return Optional.empty();
		}
		boolean hasMultipleIngredients = slot.getAllIngredients()
			.limit(2)
			.count() > 1;
		if (hasMultipleIngredients) {
			return tagKey;
		}
		return Optional.empty();
	}
}
