package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.recipe.RecipeIngredientRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeSlots implements IRecipeSlotsView {
	private final List<RecipeSlot> slots = new ArrayList<>();
	private final List<IRecipeSlotTooltipCallback> tooltipCallbacks = new ArrayList<>();

	@Override
	public List<IRecipeSlotView> getSlotViews() {
		return Collections.unmodifiableList(this.slots);
	}

	@Override
	public List<IRecipeSlotView> getSlotViews(RecipeIngredientRole role, IIngredientType<?> ingredientType) {
		return this.slots.stream()
			.filter(slotView -> slotView.getRole() == role)
			.filter(slotView -> slotView.getAllIngredients(ingredientType).findAny().isPresent())
			.map(slotView -> (IRecipeSlotView) slotView)
			.toList();
	}

	public List<RecipeSlot> getSlots() {
		return Collections.unmodifiableList(this.slots);
	}

	public void addSlot(RecipeSlot slot) {
		this.slots.add(slot);
	}

	public void draw(PoseStack poseStack, int xOffset, int yOffset, int highlightColor, int mouseX, int mouseY) {
		for (RecipeSlot slot : this.slots) {
			slot.draw(poseStack, xOffset, yOffset);
			if (slot.isMouseOver(mouseX - xOffset, mouseY - yOffset)) {
				slot.setTooltipCallbacks(tooltipCallbacks);
				slot.drawHighlight(poseStack, highlightColor, xOffset, yOffset);
			}
		}
	}

	public Optional<RecipeSlot> getHoveredSlot(int xOffset, int yOffset, double mouseX, double mouseY) {
		return slots.stream()
			.filter(ingredient -> ingredient.isMouseOver(mouseX - xOffset, mouseY - yOffset))
			.findFirst();
	}

	public void addTooltipCallback(IRecipeSlotTooltipCallback tooltipCallback) {
		this.tooltipCallbacks.add(tooltipCallback);
	}
}
