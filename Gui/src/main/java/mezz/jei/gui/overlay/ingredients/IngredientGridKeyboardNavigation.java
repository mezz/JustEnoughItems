package mezz.jei.gui.overlay.ingredients;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;

import java.util.function.BooleanSupplier;

/** Handles ingredient selection, including page and scroll boundaries. */
final class IngredientGridKeyboardNavigation implements GuiEventListener {
	private final IngredientGrid grid;
	private final IngredientGridWithNavigationController controller;
	private final BooleanSupplier active;

	IngredientGridKeyboardNavigation(IngredientGrid grid, IngredientGridWithNavigationController controller, BooleanSupplier active) {
		this.grid = grid;
		this.controller = controller;
		this.active = active;
	}

	@Override
	public void setFocused(boolean focused) {
		if (!focused) {
			grid.select(null);
		} else if (active.getAsBoolean()) {
			if (grid.getFocusedSlot().isEmpty()) {
				grid.getSlots().flatMap(slot -> slot.getOptionalElement().stream()).findFirst().ifPresent(grid::select);
			}
			ensureVisible();
		}
	}

	@Override
	public boolean isFocused() {
		return active.getAsBoolean() && grid.getFocusedSlot().isPresent();
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!isFocused()) {
			return false;
		}
		switch (keyCode) {
			case InputConstants.KEY_UP -> navigate(-grid.getColumnCount(), true);
			case InputConstants.KEY_DOWN -> navigate(grid.getColumnCount(), true);
			case InputConstants.KEY_LEFT -> navigate(-1, false);
			case InputConstants.KEY_RIGHT -> navigate(1, false);
			case InputConstants.KEY_TAB -> {
				if ((modifiers & GLFW.GLFW_MOD_SHIFT) != 0) {
					return navigate(-1, true);
				}
				return navigate(1, true);
			}
			default -> {
				return false;
			}
		}
		return true;
	}

	private boolean navigate(int step, boolean crossRows) {
		int current = grid.getAllSlots().indexOf(grid.getFocusedSlot().orElseThrow());
		boolean moved = move(current, step, crossRows);
		if (!moved && crossRows && changePage(Integer.signum(step))) {
			int edge = -step;
			if (step < 0) {
				edge = grid.getAllSlots().size();
			}
			if (Math.abs(step) > 1) {
				edge += current % grid.getColumnCount();
			}
			current = grid.getFocusedSlot().map(grid.getAllSlots()::indexOf).orElse(edge);
			moved = move(current, step, crossRows);
		}
		ensureVisible();
		return moved;
	}

	private boolean move(int current, int step, boolean crossRows) {
		var slots = grid.getAllSlots();
		var occupied = slots.stream().map(slot -> !slot.isBlocked() && slot.getOptionalElement().isPresent()).toList();
		int next = IngredientGridLayout.findNextSlot(occupied, grid.getColumnCount(), current, step, crossRows);
		if (next < 0) {
			return false;
		}
		grid.select(slots.get(next).getOptionalElement().orElseThrow());
		return true;
	}

	private boolean changePage(int direction) {
		if (controller.usesScrollbar()) {
			return controller.scrollByRows(direction);
		}
		if (direction > 0) {
			return controller.getPageNumber() + 1 < controller.getPageCount() && controller.nextPage();
		}
		return controller.getPageNumber() > 0 && controller.previousPage();
	}

	private void ensureVisible() {
		grid.getFocusedSlot().ifPresent(slot -> {
			var slotArea = slot.getArea();
			var area = grid.getArea();
			if (slotArea.y() < area.y()) {
				controller.scrollByPixels(slotArea.y() - area.y());
			} else if (slotArea.y() + slotArea.height() > area.y() + area.height()) {
				controller.scrollByPixels(slotArea.y() + slotArea.height() - area.y() - area.height());
			}
		});
	}
}
