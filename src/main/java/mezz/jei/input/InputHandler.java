package mezz.jei.input;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.config.Config;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.overlay.ItemListOverlay;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.CommandUtil;
import mezz.jei.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InputHandler {
	private final RecipeRegistry recipeRegistry;
	private final IIngredientRegistry ingredientRegistry;
	private final RecipesGui recipesGui;
	private final ItemListOverlay itemListOverlay;
	private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<IShowsRecipeFocuses>();

	private boolean clickHandled = false;

	public InputHandler(JeiRuntime runtime, ItemListOverlay itemListOverlay) {
		this.recipeRegistry = runtime.getRecipeRegistry();
		this.ingredientRegistry = runtime.getIngredientRegistry();
		this.recipesGui = runtime.getRecipesGui();
		this.itemListOverlay = itemListOverlay;

		this.showsRecipeFocuses.add(recipesGui);
		this.showsRecipeFocuses.add(itemListOverlay);
		this.showsRecipeFocuses.add(new GuiContainerWrapper());
	}

	public boolean handleMouseEvent(GuiScreen guiScreen, int mouseX, int mouseY) {
		boolean cancelEvent = false;
		if (Mouse.getEventButton() > -1) {
			if (Mouse.getEventButtonState()) {
				if (!clickHandled) {
					cancelEvent = handleMouseClick(guiScreen, Mouse.getEventButton(), mouseX, mouseY);
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
		return itemListOverlay.isEnabled() && itemListOverlay.handleMouseScrolled(mouseX, mouseY, dWheel);
	}

	private boolean handleMouseClick(GuiScreen guiScreen, int mouseButton, int mouseX, int mouseY) {
		if (itemListOverlay.isEnabled() && itemListOverlay.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}

		IClickedIngredient<?> clicked = getFocusUnderMouseForClick(mouseX, mouseY);
		if (clicked != null && handleMouseClickedFocus(mouseButton, clicked)) {
			return true;
		}

		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			RecipeClickableArea clickableArea = recipeRegistry.getRecipeClickableArea(guiContainer, mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop());
			if (clickableArea != null) {
				List<String> recipeCategoryUids = clickableArea.getRecipeCategoryUids();
				recipesGui.showCategories(recipeCategoryUids);
			}
		}

		return false;
	}

	@Nullable
	private IClickedIngredient<?> getFocusUnderMouseForClick(int mouseX, int mouseY) {
		for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
			if (gui.canSetFocusWithMouse()) {
				IClickedIngredient<?> clicked = gui.getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					return clicked;
				}
			}
		}
		return null;
	}

	@Nullable
	private IClickedIngredient<?> getIngredientUnderMouseForKey(int mouseX, int mouseY) {
		for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
			IClickedIngredient<?> clicked = gui.getIngredientUnderMouse(mouseX, mouseY);
			if (clicked != null) {
				return clicked;
			}
		}
		return null;
	}

	private <V> boolean handleMouseClickedFocus(int mouseButton, IClickedIngredient<V> clicked) {
		if (Config.isEditModeEnabled() &&
				handleClickEdit(mouseButton, clicked.getValue())) {
			return true;
		}

		if (Config.isCheatItemsEnabled() && clicked.allowsCheating() && !recipesGui.isOpen()) {
			final boolean fullStack = (mouseButton == 0);
			if (fullStack || mouseButton == 1) {
				V focusValue = clicked.getValue();
				IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(focusValue);
				ItemStack itemStack = ItemStack.EMPTY;
				try {
					itemStack = ingredientHelper.cheatIngredient(focusValue, fullStack);
				} catch (AbstractMethodError ignored) {
					// older ingredientHelpers do not have this method
				}
				if (!itemStack.isEmpty()) {
					CommandUtil.giveStack(itemStack, fullStack);
				}
				return true;
			}
		}

		if (mouseButton == 0) {
			IFocus focus = new Focus<Object>(IFocus.Mode.OUTPUT, clicked.getValue());
			recipesGui.show(focus);
			return true;
		} else if (mouseButton == 1) {
			IFocus focus = new Focus<Object>(IFocus.Mode.INPUT, clicked.getValue());
			recipesGui.show(focus);
			return true;
		}

		return false;
	}

	private <V> boolean handleClickEdit(int mouseButton, V ingredient) {
		Config.IngredientBlacklistType blacklistType = null;
		if (GuiScreen.isCtrlKeyDown()) {
			if (GuiScreen.isShiftKeyDown()) {
				if (mouseButton == 0) {
					blacklistType = Config.IngredientBlacklistType.MOD_ID;
				}
			} else {
				if (mouseButton == 0) {
					blacklistType = Config.IngredientBlacklistType.ITEM;
				} else if (mouseButton == 1) {
					blacklistType = Config.IngredientBlacklistType.WILDCARD;
				}
			}
		}

		if (blacklistType == null) {
			return false;
		}

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		if (Config.isIngredientOnConfigBlacklist(ingredient, blacklistType, ingredientHelper)) {
			Config.removeIngredientFromConfigBlacklist(ingredient, blacklistType, ingredientHelper);
		} else {
			Config.addIngredientToConfigBlacklist(ingredient, blacklistType, ingredientHelper);
		}
		return true;
	}

	public boolean hasKeyboardFocus() {
		return itemListOverlay.isEnabled() && itemListOverlay.hasKeyboardFocus();
	}

	public boolean handleKeyEvent() {
		char typedChar = Keyboard.getEventCharacter();
		int eventKey = Keyboard.getEventKey();

		return ((eventKey == 0 && typedChar >= 32) || Keyboard.getEventKeyState()) &&
				handleKeyDown(typedChar, eventKey);
	}

	private boolean handleKeyDown(char typedChar, int eventKey) {
		if (itemListOverlay.isEnabled() && itemListOverlay.hasKeyboardFocus()) {
			if (isInventoryCloseKey(eventKey) || isEnterKey(eventKey)) {
				itemListOverlay.setKeyboardFocus(false);
				return true;
			} else if (itemListOverlay.onKeyPressed(typedChar, eventKey)) {
				return true;
			}
		}

		if (KeyBindings.toggleOverlay.isActiveAndMatches(eventKey)) {
			Config.toggleOverlayEnabled();
			return false;
		}

		if (itemListOverlay.isEnabled()) {
			if (KeyBindings.toggleCheatMode.isActiveAndMatches(eventKey)) {
				Config.toggleCheatItemsEnabled();
				return true;
			}

			if (KeyBindings.focusSearch.isActiveAndMatches(eventKey)) {
				itemListOverlay.setKeyboardFocus(true);
				return true;
			}
		}

		if (!isContainerTextFieldFocused()) {
			final boolean showRecipe = KeyBindings.showRecipe.isActiveAndMatches(eventKey);
			final boolean showUses = KeyBindings.showUses.isActiveAndMatches(eventKey);
			if (showRecipe || showUses) {
				IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseHelper.getX(), MouseHelper.getY());
				if (clicked != null) {
					IFocus.Mode mode = showRecipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT;
					recipesGui.show(new Focus<Object>(mode, clicked.getValue()));
					return true;
				}
			}

			if (itemListOverlay.isEnabled() && itemListOverlay.onKeyPressed(typedChar, eventKey)) {
				return true;
			}
		}

		return false;
	}

	private boolean isContainerTextFieldFocused() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui == null) {
			return false;
		}
		GuiTextField textField = ReflectionUtil.getFieldWithClass(gui, GuiTextField.class);
		return textField != null && textField.getVisible() && textField.isFocused();
	}

	public static boolean isInventoryToggleKey(int keyCode) {
		return Minecraft.getMinecraft().gameSettings.keyBindInventory.isActiveAndMatches(keyCode);
	}

	public static boolean isInventoryCloseKey(int keyCode) {
		return keyCode == Keyboard.KEY_ESCAPE;
	}

	public static boolean isEnterKey(int keyCode) {
		return keyCode == Keyboard.KEY_RETURN;
	}
}
