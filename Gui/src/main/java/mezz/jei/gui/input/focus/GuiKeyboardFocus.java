package mezz.jei.gui.input.focus;

import mezz.jei.common.platform.Services;
import mezz.jei.gui.util.GuiEventListenerUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;

/** Transfers focus between JEI controls and restores the screen's previous widget when JEI releases it. */
public final class GuiKeyboardFocus {
	private @Nullable Screen screen;
	private @Nullable GuiEventListener focused;
	private Runnable restorePreviousFocus = () -> {};

	/** Selects a control, releasing JEI's focus if that control is unavailable. */
	public void focus(GuiEventListener target) {
		update();
		if (isFocused(target)) {
			return;
		}
		Screen currentScreen = Minecraft.getInstance().screen;
		if (currentScreen == null) {
			return;
		}
		if (focused == null) {
			screen = currentScreen;
			restorePreviousFocus = suspendScreenFocus(currentScreen);
		}
		focused = target;
		currentScreen.setFocused(target);
		if (!target.isFocused()) {
			clear();
		}
	}

	public boolean isFocused(GuiEventListener target) {
		return focused == target && screen == Minecraft.getInstance().screen &&
			screen != null && screen.getFocused() == target && target.isFocused();
	}

	/** Releases this control without disturbing focus owned by another control. */
	public void clear(GuiEventListener target) {
		if (focused == target) {
			clear();
		}
	}

	/** Releases JEI focus, restoring the previous widget only while JEI still owns the screen focus. */
	public void clear() {
		Screen oldScreen = screen;
		GuiEventListener oldFocus = focused;
		Runnable restoreFocus = restorePreviousFocus;
		screen = null;
		focused = null;
		restorePreviousFocus = () -> {};
		if (oldFocus != null) {
			oldFocus.setFocused(false);
			if (oldScreen != null && oldScreen.getFocused() == oldFocus) {
				oldScreen.setFocused(null);
				if (oldScreen == Minecraft.getInstance().screen) {
					restoreFocus.run();
				}
			}
		}
	}

	/** Releases focus after a screen change, an unavailable control, or Minecraft selecting another widget. */
	public void update() {
		if (focused != null && !isFocused(focused)) {
			clear();
		}
	}

	/** Releases the current widget and returns an action that restores its focus. */
	static Runnable suspendScreenFocus(Screen screen) {
		GuiEventListener screenFocus = screen.getFocused();
		GuiEventListener widget = screenFocus;
		if (widget == null) {
			// The creative inventory can focus its search box without setting the screen's focus.
			widget = GuiEventListenerUtil.getEditBoxes(screen).filter(EditBox::isFocused).findFirst().orElse(null);
		}
		if (widget == null) {
			return () -> {};
		}
		if (widget instanceof EditBox editBox) {
			boolean canLoseFocus = Services.PLATFORM.getScreenHelper().canLoseFocus(editBox);
			editBox.setCanLoseFocus(true);
			editBox.setFocused(false);
			editBox.setCanLoseFocus(canLoseFocus);
		} else {
			widget.setFocused(false);
		}
		screen.setFocused(null);
		GuiEventListener previousWidget = widget;
		return () -> {
			previousWidget.setFocused(true);
			screen.setFocused(screenFocus);
		};
	}
}
