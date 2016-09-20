package mezz.jei.gui.ingredients;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import mezz.jei.api.gui.IGuiIngredientGroup;
import mezz.jei.api.gui.ITooltipCallback;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.gui.Focus;
import net.minecraft.client.Minecraft;

public abstract class GuiIngredientGroup<T, V extends GuiIngredient<T>> implements IGuiIngredientGroup<T> {
	protected final int itemCycleOffset = (int) (Math.random() * 1000);
	protected final Map<Integer, V> guiIngredients = new HashMap<Integer, V>();
	/**
	 * If focus is set and any of the guiIngredients contains focus
	 * they will only display focus instead of rotating through all their values.
	 */
	protected final IFocus<T> focus;
	@Nullable
	private ITooltipCallback<T> tooltipCallback;

	public GuiIngredientGroup(IFocus<T> focus) {
		this.focus = focus;
	}

	@Override
	public IFocus<T> getFocus() {
		return focus;
	}

	@Override
	public void set(int slotIndex, Collection<T> values) {
		guiIngredients.get(slotIndex).set(values, focus);
	}

	@Override
	public void set(int slotIndex, T value) {
		guiIngredients.get(slotIndex).set(value, focus);
	}

	@Override
	public void addTooltipCallback(ITooltipCallback<T> tooltipCallback) {
		this.tooltipCallback = tooltipCallback;
	}

	@Override
	public Map<Integer, V> getGuiIngredients() {
		return guiIngredients;
	}

	@Nullable
	public Focus<T> getFocusUnderMouse(int xOffset, int yOffset, int mouseX, int mouseY) {
		for (V widget : guiIngredients.values()) {
			if (widget != null && widget.isMouseOver(xOffset, yOffset, mouseX, mouseY)) {
				return widget.getCurrentlyDisplayed();
			}
		}
		return null;
	}

	@Nullable
	public V draw(Minecraft minecraft, int xOffset, int yOffset, int mouseX, int mouseY) {
		V hovered = null;
		for (V ingredient : guiIngredients.values()) {
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
