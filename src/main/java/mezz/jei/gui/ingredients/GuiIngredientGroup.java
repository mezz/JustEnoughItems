package mezz.jei.gui.ingredients;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;

import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.gui.Focus;

public abstract class GuiIngredientGroup<V, T extends GuiIngredient<V>> implements IGuiIngredientGroup<V> {
	@Nonnull
	protected final Map<Integer, T> guiIngredients = new HashMap<>();
	@Nonnull
	protected Focus focus = new Focus();

	/**
	 * If focus is set and any of the guiIngredients contains focus
	 * they will only display focus instead of rotating through all their values.
	 */
	public void setFocus(@Nonnull Focus focus) {
		this.focus = focus;
	}

	@Override
	public void set(int slotIndex, @Nonnull Collection<V> values) {
		guiIngredients.get(slotIndex).set(values, focus);
	}

	@Override
	public void set(int slotIndex, @Nonnull V value) {
		guiIngredients.get(slotIndex).set(value, focus);
	}

	@Nonnull
	public Map<Integer, T> getGuiIngredients() {
		return guiIngredients;
	}

	@Nullable
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		for (T widget : guiIngredients.values()) {
			if (widget != null && widget.isMouseOver(mouseX, mouseY)) {
				return new Focus(widget.get());
			}
		}
		return null;
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		T hovered = null;
		for (T ingredient : guiIngredients.values()) {
			if (hovered == null && ingredient.isMouseOver(mouseX, mouseY)) {
				hovered = ingredient;
			} else {
				ingredient.draw(minecraft);
			}
		}
		if (hovered != null) {
			hovered.drawHovered(minecraft, mouseX, mouseY);
		}
	}
}
