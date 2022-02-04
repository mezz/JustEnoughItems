package mezz.jei.gui.ingredients;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;

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

	public void draw(PoseStack poseStack, int posX, int posY, int highlightColor, int mouseX, int mouseY) {
		for (RecipeSlot slot : slots) {
			slot.draw(poseStack, posX, posY);
			if (slot.isMouseOver(mouseX - posX, mouseY - posY)) {
				slot.drawHighlight(poseStack, highlightColor, posX, posY);
			}
		}
	}

	public Optional<RecipeSlot> getHoveredSlot(int xOffset, int yOffset, double mouseX, double mouseY) {
		return slots.stream()
			.filter(ingredient -> ingredient.isMouseOver(mouseX - xOffset, mouseY - yOffset))
			.findFirst();
	}
}
