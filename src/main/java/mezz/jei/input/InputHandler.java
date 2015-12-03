package mezz.jei.input;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import net.minecraftforge.fml.client.FMLClientHandler;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipesGui;
import mezz.jei.util.Commands;
import mezz.jei.util.MouseHelper;
import mezz.jei.util.Permissions;

public class InputHandler {

	private final RecipesGui recipesGui;
	private final ItemListOverlay itemListOverlay;
	private final MouseHelper mouseHelper;

	private final List<IMouseHandler> mouseHandlers = new ArrayList<>();
	private final List<IKeyable> keyables = new ArrayList<>();
	private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();

	private boolean clickHandled = false;

	public InputHandler(RecipesGui recipesGui, ItemListOverlay itemListOverlay, GuiContainer guiContainer) {
		this.recipesGui = recipesGui;
		this.itemListOverlay = itemListOverlay;

		this.mouseHelper = new MouseHelper();

		List<ICloseable> objects = new ArrayList<>();
		objects.add(recipesGui);
		objects.add(itemListOverlay);
		objects.add(new GuiContainerWrapper(guiContainer, recipesGui));

		for (Object gui : objects) {
			if (gui instanceof IMouseHandler) {
				mouseHandlers.add((IMouseHandler) gui);
			}
			if (gui instanceof IKeyable) {
				keyables.add((IKeyable) gui);
			}
			if (gui instanceof IShowsRecipeFocuses) {
				showsRecipeFocuses.add((IShowsRecipeFocuses) gui);
			}
		}
	}

	public boolean handleMouseEvent(int mouseX, int mouseY) {
		boolean cancelEvent = false;
		if (Mouse.getEventButton() > -1) {
			if (Mouse.getEventButtonState()) {
				if (!clickHandled) {
					cancelEvent = handleMouseClick(Mouse.getEventButton(), mouseX, mouseY);
					clickHandled = true;
				}
			} else {
				clickHandled = false;
			}
		} else if (Mouse.getEventDWheel() != 0) {
			cancelEvent = handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
		}
		return cancelEvent;
	}

	private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
		for (IMouseHandler scrollable : mouseHandlers) {
			if (scrollable.handleMouseScrolled(mouseX, mouseY, dWheel)) {
				return true;
			}
		}
		return false;
	}

	private boolean handleMouseClick(int mouseButton, int mouseX, int mouseY) {
		Focus focus = getFocusUnderMouseForClick(mouseX, mouseY);
		if (focus != null) {
			if (handleMouseClickedFocus(mouseButton, focus)) {
				return true;
			}
		}

		for (IMouseHandler clickable : mouseHandlers) {
			if (clickable.handleMouseClicked(mouseX, mouseY, mouseButton)) {
				return true;
			}
		}

		return false;
	}

	@Nullable
	private Focus getFocusUnderMouseForClick(int mouseX, int mouseY) {
		for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
			if (!(gui instanceof IMouseHandler)) {
				continue;
			}

			Focus focus = gui.getFocusUnderMouse(mouseX, mouseY);
			if (focus != null) {
				return focus;
			}
		}
		return null;
	}

	@Nullable
	private Focus getFocusUnderMouseForKey(int mouseX, int mouseY) {
		for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
			Focus focus = gui.getFocusUnderMouse(mouseX, mouseY);
			if (focus != null) {
				return focus;
			}
		}
		return null;
	}

	private boolean handleMouseClickedFocus(int mouseButton, @Nonnull Focus focus) {
		if (Config.editModeEnabled && GuiScreen.isCtrlKeyDown()) {
			Boolean wildcard = null;
			if (mouseButton == 0) {
				wildcard = false;
			} else if (mouseButton == 1) {
				wildcard = true;
			}

			if (wildcard != null) {
				if (Config.isItemOnConfigBlacklist(focus.getStack(), wildcard)) {
					Config.removeItemFromConfigBlacklist(focus.getStack(), wildcard);
				} else {
					Config.addItemToConfigBlacklist(focus.getStack(), wildcard);
				}
				return true;
			}
		}

		EntityPlayerSP player = FMLClientHandler.instance().getClientPlayerEntity();
		if (Config.cheatItemsEnabled && Permissions.canPlayerSpawnItems(player)) {
			if (mouseButton == 0) {
				if (focus.getStack() != null) {
					Commands.giveFullStack(focus.getStack());
				}
				return true;
			} else if (mouseButton == 1) {
				if (focus.getStack() != null) {
					Commands.giveOneFromStack(focus.getStack());
				}
				return true;
			}
		}

		if (mouseButton == 0) {
			recipesGui.showRecipes(focus);
			return true;
		} else if (mouseButton == 1) {
			recipesGui.showUses(focus);
			return true;
		}

		return false;
	}

	public boolean handleKeyEvent() {
		boolean cancelEvent = false;
		if (Keyboard.getEventKeyState()) {
			int eventKey = Keyboard.getEventKey();
			cancelEvent = handleKeyDown(eventKey);
		}
		return cancelEvent;
	}

	private boolean handleKeyDown(int eventKey) {
		for (IKeyable keyable : keyables) {
			if (keyable.isOpen() && keyable.hasKeyboardFocus()) {
				if (isInventoryCloseKey(eventKey)) {
					keyable.setKeyboardFocus(false);
					return true;
				} else if (keyable.onKeyPressed(eventKey)) {
					return true;
				}
			}
		}

		if (isInventoryCloseKey(eventKey) || isInventoryToggleKey(eventKey)) {
			if (recipesGui.isOpen()) {
				recipesGui.close();
				return true;
			}
		}

		if (eventKey == KeyBindings.showRecipe.getKeyCode()) {
			Focus focus = getFocusUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
			if (focus != null) {
				recipesGui.showRecipes(focus);
				return true;
			}
		} else if (eventKey == KeyBindings.showUses.getKeyCode()) {
			Focus focus = getFocusUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
			if (focus != null) {
				recipesGui.showUses(focus);
				return true;
			}
		} else if (eventKey == KeyBindings.toggleOverlay.getKeyCode() && GuiScreen.isCtrlKeyDown()) {
			itemListOverlay.toggleEnabled();
			return false;
		}

		for (IKeyable keyable : keyables) {
			if (keyable.isOpen() && keyable.onKeyPressed(eventKey)) {
				return true;
			}
		}

		return false;
	}

	private boolean isInventoryToggleKey(int keyCode) {
		return keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();
	}

	private boolean isInventoryCloseKey(int keyCode) {
		return keyCode == Keyboard.KEY_ESCAPE;
	}

}
