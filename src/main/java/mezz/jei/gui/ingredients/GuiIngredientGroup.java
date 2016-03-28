package mezz.jei.gui.ingredients;

import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class GuiIngredientGroup<V, T extends GuiIngredient<V>> implements IGuiIngredientGroup<V> {
	protected final int itemCycleOffset = (int) (Math.random() * 1000);
	@Nonnull
	protected final Map<Integer, T> guiIngredients = new HashMap<>();
	@Nonnull
	protected Focus focus = new Focus();
	@Nullable
	private ITooltipCallback<V> tooltipCallback;

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

	@Override
	public void addTooltipCallback(@Nonnull ITooltipCallback<V> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	@Override
	@Nonnull
	public Map<Integer, T> getGuiIngredients() {
		return guiIngredients;
	}

	@Nullable
	public Focus getFocusUnderMouse(int xOffset, int yOffset, int mouseX, int mouseY) {
		for (T widget : guiIngredients.values()) {
			if (widget != null && widget.isMouseOver(xOffset, yOffset, mouseX, mouseY)) {
				return widget.getFocus();
			}
		}
		return null;
	}

	@Nullable
	public T draw(@Nonnull Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
		T hovered = null;
		for (T ingredient : guiIngredients.values()) {
			if (hovered == null && ingredient.isMouseOver(xOffset, yOffset, mouseX, mouseY)) {
				hovered = ingredient;
				hovered.setTooltipCallback(tooltipCallback);
			} else {
				ingredient.draw(minecraft, xOffset, yOffset);
			}
		}
		return hovered;
	}
}
