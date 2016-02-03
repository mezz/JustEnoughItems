package mezz.jei.input;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import mezz.jei.Internal;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.ItemListOverlay;
import mezz.jei.gui.RecipeClickableArea;
import mezz.jei.gui.RecipesGui;
import mezz.jei.util.Commands;
import mezz.jei.util.MouseHelper;

public class InputHandler {

	private final RecipesGui recipesGui;
	private final ItemListOverlay itemListOverlay;
	private final MouseHelper mouseHelper;

	private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();

	private boolean clickHandled = false;

	public InputHandler(RecipesGui recipesGui, ItemListOverlay itemListOverlay) {
		this.recipesGui = recipesGui;
		this.itemListOverlay = itemListOverlay;

		this.mouseHelper = new MouseHelper();

		showsRecipeFocuses.add(recipesGui);
		showsRecipeFocuses.add(itemListOverlay);
		showsRecipeFocuses.add(new GuiContainerWrapper());
	}

	public boolean handleMouseEvent(@Nonnull GuiContainer guiContainer, int mouseX, int mouseY) {
		boolean cancelEvent = false;
		if (Mouse.getEventButton() > -1) {
			if (Mouse.getEventButtonState()) {
				if (!clickHandled) {
					cancelEvent = handleMouseClick(guiContainer, Mouse.getEventButton(), mouseX, mouseY);
					clickHandled = cancelEvent;
				}
			} else if (clickHandled) {
				clickHandled = false;
				cancelEvent = true;
			}
		} else if (Mouse.getEventDWheel() != 0) {
			cancelEvent = handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
		}
		return cancelEvent;
	}

	private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
		return itemListOverlay.handleMouseScrolled(mouseX, mouseY, dWheel);
	}

	private boolean handleMouseClick(@Nonnull GuiContainer guiContainer, int mouseButton, int mouseX, int mouseY) {
		if (itemListOverlay.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}

		Focus focus = getFocusUnderMouseForClick(mouseX, mouseY);
		if (focus != null && handleMouseClickedFocus(mouseButton, focus)) {
			return true;
		}

		RecipeClickableArea clickableArea = Internal.getRuntime().getRecipeRegistry().getRecipeClickableArea(guiContainer);
		if (clickableArea != null && clickableArea.checkHover(mouseX - guiContainer.guiLeft, mouseY - guiContainer.guiTop)) {
			List<String> recipeCategoryUids = clickableArea.getRecipeCategoryUids();
			recipesGui.showCategories(recipeCategoryUids);
		}

		return false;
	}

	@Nullable
	private Focus getFocusUnderMouseForClick(int mouseX, int mouseY) {
		for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
			if (gui.canSetFocusWithMouse()) {
				Focus focus = gui.getFocusUnderMouse(mouseX, mouseY);
				if (focus != null) {
					return focus;
				}
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
		if (Config.isEditModeEnabled()) {
			if (handleClickEditStack(mouseButton, focus)) {
				return true;
			}
		}

		if (Config.isCheatItemsEnabled() && focus.getStack() != null) {
			if (mouseButton == 0) {
				Commands.giveFullStack(focus.getStack());
				return true;
			} else if (mouseButton == 1) {
				Commands.giveOneFromStack(focus.getStack());
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

	private boolean handleClickEditStack(int mouseButton, @Nonnull Focus focus) {
		ItemStack itemStack = focus.getStack();
		if (itemStack == null) {
			return false;
		}

		Config.ItemBlacklistType blacklistType = null;
		if (GuiScreen.isCtrlKeyDown()) {
			if (GuiScreen.isShiftKeyDown()) {
				if (mouseButton == 0) {
					blacklistType = Config.ItemBlacklistType.MOD_ID;
				}
			} else {
				if (mouseButton == 0) {
					blacklistType = Config.ItemBlacklistType.ITEM;
				} else if (mouseButton == 1) {
					blacklistType = Config.ItemBlacklistType.WILDCARD;
				}
			}
		}

		if (blacklistType == null) {
			return false;
		}

		if (Config.isItemOnConfigBlacklist(focus.getStack(), blacklistType)) {
			Config.removeItemFromConfigBlacklist(focus.getStack(), blacklistType);
		} else {
			Config.addItemToConfigBlacklist(focus.getStack(), blacklistType);
		}
		return true;
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
		if (itemListOverlay.isOpen() && itemListOverlay.hasKeyboardFocus()) {
			if (isInventoryCloseKey(eventKey) || isEnterKey(eventKey)) {
				itemListOverlay.setKeyboardFocus(false);
				return true;
			} else if (itemListOverlay.onKeyPressed(eventKey)) {
				return true;
			}
		}

		if (GuiScreen.isCtrlKeyDown()) {
			if (eventKey == KeyBindings.toggleOverlay.getKeyCode()) {
				Config.toggleOverlayEnabled();
				return false;
			} else if (eventKey == Keyboard.KEY_F) {
				itemListOverlay.setKeyboardFocus(true);
				return true;
			}
		}

		if (!isContainerTextFieldFocused()) {
			if (eventKey == KeyBindings.showRecipe.getKeyCode()) {
				Focus focus = getFocusUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
				if (focus != null) {
					if (!GuiScreen.isShiftKeyDown()) {
						recipesGui.showRecipes(focus);
					} else {
						recipesGui.showUses(focus);
					}
					return true;
				}
			} else if (eventKey == KeyBindings.showUses.getKeyCode()) {
				Focus focus = getFocusUnderMouseForKey(mouseHelper.getX(), mouseHelper.getY());
				if (focus != null) {
					recipesGui.showUses(focus);
					return true;
				}
			}

			if (itemListOverlay.isOpen() && itemListOverlay.onKeyPressed(eventKey)) {
				return true;
			}
		}

		return false;
	}

	private boolean isContainerTextFieldFocused() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		GuiTextField textField = null;

		if (gui instanceof GuiContainerCreative) {
			textField = ((GuiContainerCreative) gui).searchField;
		} else if (gui instanceof GuiRepair) {
			textField = ((GuiRepair) gui).nameField;
		}

		return textField != null && textField.getVisible() && textField.isEnabled && textField.isFocused();
	}

	public static boolean isInventoryToggleKey(int keyCode) {
		return keyCode == Minecraft.getMinecraft().gameSettings.keyBindInventory.getKeyCode();
	}

	public static boolean isInventoryCloseKey(int keyCode) {
		return keyCode == Keyboard.KEY_ESCAPE;
	}

	public static boolean isEnterKey(int keyCode) {
		return keyCode == Keyboard.KEY_RETURN;
	}
}
