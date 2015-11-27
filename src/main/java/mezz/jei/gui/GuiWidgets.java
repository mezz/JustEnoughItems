package mezz.jei.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;

public abstract class GuiWidgets<V, T extends GuiWidget<V>> {
	@Nonnull
	protected final Map<Integer, T> guiWidgets = new HashMap<>();
	@Nonnull
	protected Focus focus = new Focus();

	/**
	 * If focus is set and any of the guiWidgets contains focus
	 * they will only display focus instead of rotating through all their values.
	 */
	public void setFocus(@Nonnull Focus focus) {
		this.focus = focus;
	}

	public void set(int index, @Nonnull Collection<V> values) {
		guiWidgets.get(index).set(values, focus);
	}

	public void set(int index, @Nonnull V value) {
		guiWidgets.get(index).set(value, focus);
	}

	public void clear() {
		for (T guiWidget : guiWidgets.values()) {
			guiWidget.clear();
		}
	}

	@Nullable
	public Focus getFocusUnderMouse(int mouseX, int mouseY) {
		for (T widget : guiWidgets.values()) {
			if (widget != null && widget.isMouseOver(mouseX, mouseY)) {
				return new Focus(widget.get());
			}
		}
		return null;
	}

	public void draw(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		T hovered = null;
		for (T widget : guiWidgets.values()) {
			if (hovered == null && widget.isMouseOver(mouseX, mouseY)) {
				hovered = widget;
			} else {
				widget.draw(minecraft);
			}
		}
		if (hovered != null) {
			hovered.drawHovered(minecraft, mouseX, mouseY);
		}
	}
}
