package mezz.jei.library.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.common.util.MathUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class RecipeSlots {
	private final List<RecipeSlot> slots;
	private final IRecipeSlotsView view;

	public RecipeSlots() {
		this.slots = new ArrayList<>();
		this.view = new RecipeSlotsView(this.slots);
	}

	public IRecipeSlotsView getView() {
		return this.view;
	}

	public List<RecipeSlot> getSlots() {
		return Collections.unmodifiableList(this.slots);
	}

	public void addSlot(RecipeSlot slot) {
		this.slots.add(slot);
	}

	public void draw(PoseStack poseStack) {
		for (IRecipeSlotDrawable slot : slots) {
			slot.draw(poseStack);
		}
	}

	public Optional<RecipeSlot> getHoveredSlot(double recipeMouseX, double recipeMouseY) {
		return slots.stream()
			.filter(ingredient -> MathUtil.contains(ingredient.getRect(), recipeMouseX, recipeMouseY))
			.findFirst();
	}
}
