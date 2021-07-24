package mezz.jei.input;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mezz.jei.input.click.MouseClickState;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;
import java.util.Arrays;

public class CombinedMouseHandler implements IMouseHandler {
	private final Int2ObjectMap<IMouseHandler> mousedDown = new Int2ObjectArrayMap<>();
	private final Iterable<IMouseHandler> mouseHandlers;

	public CombinedMouseHandler(IMouseHandler... mouseHandlers) {
		this.mouseHandlers = Arrays.asList(mouseHandlers);
	}

	public CombinedMouseHandler(Iterable<IMouseHandler> mouseHandlers) {
		this.mouseHandlers = mouseHandlers;
	}

	@Nullable
	@Override
	public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
		if (clickState.isSimulate() || clickState.isVanilla()) {
			this.mousedDown.remove(mouseButton);
			IMouseHandler handled = null;
			for (IMouseHandler mouseHandler : this.mouseHandlers) {
				if (handled == null) {
					handled = mouseHandler.handleClick(screen, mouseX, mouseY, mouseButton, clickState);
				} else {
					mouseHandler.handleMouseClickedOut(mouseButton);
				}
			}
			if (handled != null) {
				if (!clickState.isVanilla()) {
					this.mousedDown.put(mouseButton, handled);
				}
			} else {
				this.handleMouseClickedOut(mouseButton);
			}
			return handled;
		} else {
			IMouseHandler mouseHandler = this.mousedDown.remove(mouseButton);
			if (mouseHandler == null) {
				return null;
			}
			return mouseHandler.handleClick(screen, mouseX, mouseY, mouseButton, clickState);
		}
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
