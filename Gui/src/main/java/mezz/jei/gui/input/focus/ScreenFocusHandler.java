package mezz.jei.gui.input.focus;

import mezz.jei.core.util.ReflectionUtil;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

public class ScreenFocusHandler implements IFocusHandler {
	private static final ReflectionUtil reflectionUtil = new ReflectionUtil();

	private final Screen screen;
	private final @Nullable IFocusHandler focusedElement;
	private final @Nullable GuiEventListener storedInScreenFocus;

	public static @Nullable ScreenFocusHandler create(Screen screen) {
		GuiEventListener focused = screen.getFocused();
		final IFocusHandler focusedElement;
		final GuiEventListener storedInScreenFocus;
		if (focused != null) {
			focusedElement = GuiEventListenerFocusHandler.create(focused);
			storedInScreenFocus = focused;
		} else {
			EditBox editBox = reflectionUtil.getFieldWithClass(screen, EditBox.class)
				.findFirst()
				.orElse(null);
			if (editBox != null) {
				focusedElement = GuiEventListenerFocusHandler.create(editBox);
			} else {
				return null;
			}
			storedInScreenFocus = null;
		}
		return new ScreenFocusHandler(screen, focusedElement, storedInScreenFocus);
	}

	public ScreenFocusHandler(Screen screen,
							@Nullable IFocusHandler focusedElement,
							@Nullable GuiEventListener storedInScreenFocus
	) {
		this.screen = screen;
		this.focusedElement = focusedElement;
		this.storedInScreenFocus = storedInScreenFocus;
	}

	@Override
	public void unFocus() {
		if (focusedElement != null) {
			focusedElement.unFocus();
			if (storedInScreenFocus != null) {
				screen.setFocused(null);
			}
		}
	}

	@Override
	public void focus() {
		if (focusedElement != null) {
			focusedElement.focus();
			if (storedInScreenFocus != null) {
				screen.setFocused(storedInScreenFocus);
			}
		}
	}
}
