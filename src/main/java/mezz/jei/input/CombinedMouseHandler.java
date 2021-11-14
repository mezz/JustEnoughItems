package mezz.jei.input;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class CombinedMouseHandler implements IMouseHandler {
	private final Int2ObjectMap<IMouseHandler> mousedDown = new Int2ObjectArrayMap<>();
	private final List<IMouseHandler> mouseHandlers;

	public CombinedMouseHandler(IMouseHandler... mouseHandlers) {
		this.mouseHandlers = Arrays.asList(mouseHandlers);
	}

	public CombinedMouseHandler(List<IMouseHandler> mouseHandlers) {
		this.mouseHandlers = mouseHandlers;
	}

	@Override
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		switch (clickState) {
			case VANILLA: return handleVanillaClick(screen, mouseX, mouseY, mouseButton);
			case SIMULATE: return handleSimulateClick(screen, mouseX, mouseY, mouseButton);
			case EXECUTE: return handleExecuteClick(screen, mouseX, mouseY, mouseButton);
		}
		return null;
	}

	/*
	 * A vanilla click or key-down will be handled immediately.
	 * We do not track the mousedDown for it,
	 * the first handler to use it will be the "winner", the rest will get a clicked-out.
	 */
	@Nullable
	private IMouseHandler handleVanillaClick(Screen screen, double mouseX, double mouseY, int mouseButton) {
		this.mousedDown.remove(mouseButton);

		IMouseHandler handled = handleClickInternal(screen, mouseX, mouseY, mouseButton, MouseClickState.VANILLA);
		if (handled == null) {
			return null;
		}
		return this;
	}

	/*
	 * For JEI-controlled clicks.
	 * JEI activates clicks when the player clicks down on it and releases the mouse on the same element.
	 *
	 * In the first click pass, it is a "simulate" to check if the handler can handle the click,
	 * and it will be added to mousedDown.
	 * In the second pass, all handlers that were in mousedDown will be sent the real click.
	 */
	@Nullable
	private IMouseHandler handleSimulateClick(Screen screen, double mouseX, double mouseY, int mouseButton) {
		this.mousedDown.remove(mouseButton);

		IMouseHandler clickHandled = handleClickInternal(screen, mouseX, mouseY, mouseButton, MouseClickState.SIMULATE);
		if (clickHandled == null) {
			return null;
		}
		this.mousedDown.put(mouseButton, clickHandled);
		return this;
	}

	@Nullable
	private IMouseHandler handleExecuteClick(Screen screen, double mouseX, double mouseY, int mouseButton) {
		IMouseHandler mouseHandler = this.mousedDown.remove(mouseButton);
		if (mouseHandler == null) {
			return null;
		}
		IMouseHandler handled = mouseHandler.handleClick(screen, mouseX, mouseY, mouseButton, MouseClickState.EXECUTE);
		if (handled == null) {
			return null;
		}
		return this;
	}

	/**
	 * Calls handleClick on each mouse handler until one handles the click (returns non-null).
	 *
	 * handleMouseClickedOut will be called on:
	 * 1. every mouse handler that fails to handleClick (returned null).
	 * 2. every mouse handler that never got a chance to handleClick because something else handled it first.
	 */
	@Nullable
	private IMouseHandler handleClickInternal(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState mouseClickState) {
		IMouseHandler firstHandled = null;
		for (IMouseHandler mouseHandler : this.mouseHandlers) {
			if (firstHandled == null) {
				firstHandled = mouseHandler.handleClick(screen, mouseX, mouseY, mouseButton, mouseClickState);
				if (firstHandled == null) {
					mouseHandler.handleMouseClickedOut(mouseButton);
				}
			} else {
				mouseHandler.handleMouseClickedOut(mouseButton);
			}
		}
		return firstHandled;
	}

	@Override
	public void handleMouseClickedOut(int mouseButton) {
		for (IMouseHandler mouseHandler : this.mouseHandlers) {
			mouseHandler.handleMouseClickedOut(mouseButton);
		}
	}

	@Override
	public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDelta) {
		for (IMouseHandler mouseHandler : this.mouseHandlers) {
			if (mouseHandler.handleMouseScrolled(mouseX, mouseY, scrollDelta)) {
				return true;
			}
		}
		return false;
	}
}
