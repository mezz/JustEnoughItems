package mezz.jei.input;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.config.Config;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ReflectionUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InputHandler {
	private final RecipeRegistry recipeRegistry;
	private final IIngredientRegistry ingredientRegistry;
	private final IngredientFilter ingredientFilter;
	private final RecipesGui recipesGui;
	private final IngredientListOverlay ingredientListOverlay;
	private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();
	private final IntSet clickHandled = new IntArraySet();

	public InputHandler(JeiRuntime runtime, IngredientListOverlay ingredientListOverlay) {
		this.recipeRegistry = runtime.getRecipeRegistry();
		this.ingredientRegistry = runtime.getIngredientRegistry();
		this.ingredientFilter = runtime.getIngredientFilter();
		this.recipesGui = runtime.getRecipesGui();
		this.ingredientListOverlay = ingredientListOverlay;

		this.showsRecipeFocuses.add(recipesGui);
		this.showsRecipeFocuses.add(ingredientListOverlay);
		this.showsRecipeFocuses.add(new GuiContainerWrapper());
	}

	public boolean handleMouseEvent(GuiScreen guiScreen, int mouseX, int mouseY) {
		boolean cancelEvent = false;
		final int eventButton = Mouse.getEventButton();
		if (eventButton > -1) {
			if (Mouse.getEventButtonState()) {
				if (!clickHandled.contains(eventButton)) {
					cancelEvent = handleMouseClick(guiScreen, eventButton, mouseX, mouseY);
					if (cancelEvent) {
						clickHandled.add(eventButton);
					}
				}
			} else if (clickHandled.contains(eventButton)) {
				clickHandled.remove(eventButton);
				cancelEvent = true;
			}
		} else if (Mouse.getEventDWheel() != 0) {
			cancelEvent = handleMouseScroll(Mouse.getEventDWheel(), mouseX, mouseY);
		}
		return cancelEvent;
	}

	private boolean handleMouseScroll(int dWheel, int mouseX, int mouseY) {
		return ingredientListOverlay.handleMouseScrolled(mouseX, mouseY, dWheel);
	}

	private boolean handleMouseClick(GuiScreen guiScreen, int mouseButton, int mouseX, int mouseY) {
		IClickedIngredient<?> clicked = getFocusUnderMouseForClick(mouseX, mouseY);
		if (Config.isEditModeEnabled() && clicked != null && handleClickEdit(mouseButton, clicked)) {
			return true;
		}
		if (ingredientListOverlay.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}

		if (clicked != null && handleMouseClickedFocus(mouseButton, clicked)) {
			return true;
		}
		if (handleFocusKeybinds(mouseButton - 100)) {
			return true;
		}

		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			RecipeClickableArea clickableArea = recipeRegistry.getRecipeClickableArea(guiContainer, mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop());
			if (clickableArea != null) {
				List<String> recipeCategoryUids = clickableArea.getRecipeCategoryUids();
				recipesGui.showCategories(recipeCategoryUids);
				return true;
			}
		}

		return handleGlobalKeybinds(mouseButton - 100);
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
		if (mouseButton == 0) {
			IFocus<?> focus = new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue());
			recipesGui.show(focus);
			clicked.onClickHandled();
			return true;
		} else if (mouseButton == 1) {
			IFocus<?> focus = new Focus<>(IFocus.Mode.INPUT, clicked.getValue());
			recipesGui.show(focus);
			clicked.onClickHandled();
			return true;
		}

		return false;
	}

	private <V> boolean handleClickEdit(int mouseButton, IClickedIngredient<V> clicked) {
		V ingredient = clicked.getValue();
		IngredientBlacklistType blacklistType = null;
		if (GuiScreen.isCtrlKeyDown()) {
			if (mouseButton == 0) {
				blacklistType = IngredientBlacklistType.ITEM;
			} else if (mouseButton == 1) {
				blacklistType = IngredientBlacklistType.WILDCARD;
			}
		}

		if (blacklistType == null) {
			return false;
		}

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		if (Config.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			Config.removeIngredientFromConfigBlacklist(ingredientFilter, ingredientRegistry, ingredient, blacklistType, ingredientHelper);
		} else {
			Config.addIngredientToConfigBlacklist(ingredientFilter, ingredientRegistry, ingredient, blacklistType, ingredientHelper);
		}
		clicked.onClickHandled();
		return true;
	}

	public boolean hasKeyboardFocus() {
		return ingredientListOverlay.isEnabled() && ingredientListOverlay.hasKeyboardFocus();
	}

	public boolean handleKeyEvent() {
		char typedChar = Keyboard.getEventCharacter();
		int eventKey = Keyboard.getEventKey();

		return ((eventKey == 0 && typedChar >= 32) || Keyboard.getEventKeyState()) &&
				handleKeyDown(typedChar, eventKey);
	}

	private boolean handleKeyDown(char typedChar, int eventKey) {
		if (ingredientListOverlay.isEnabled() && ingredientListOverlay.hasKeyboardFocus()) {
			if (isInventoryCloseKey(eventKey) || isEnterKey(eventKey)) {
				ingredientListOverlay.setKeyboardFocus(false);
				return true;
			} else if (ingredientListOverlay.onKeyPressed(typedChar, eventKey)) {
				return true;
			}
		}

		if (handleGlobalKeybinds(eventKey)) {
			return true;
		}

		if (!isContainerTextFieldFocused()) {
			if (handleFocusKeybinds(eventKey)) {
				return true;
			}
			if (ingredientListOverlay.isEnabled()) {
				return ingredientListOverlay.onKeyPressed(typedChar, eventKey);
			}
			return false;
		}

		return false;
	}

	private boolean handleGlobalKeybinds(int eventKey) {
		if (KeyBindings.toggleOverlay.isActiveAndMatches(eventKey)) {
			Config.toggleOverlayEnabled();
			return false;
		}

		if (ingredientListOverlay.isEnabled()) {
			if (KeyBindings.toggleCheatMode.isActiveAndMatches(eventKey)) {
				Config.toggleCheatItemsEnabled();
				return true;
			}

			if (KeyBindings.focusSearch.isActiveAndMatches(eventKey)) {
				ingredientListOverlay.setKeyboardFocus(true);
				return true;
			}
		}

		return false;
	}

	private boolean handleFocusKeybinds(int eventKey) {
		final boolean showRecipe = KeyBindings.showRecipe.isActiveAndMatches(eventKey);
		final boolean showUses = KeyBindings.showUses.isActiveAndMatches(eventKey);
		if (showRecipe || showUses) {
			IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseHelper.getX(), MouseHelper.getY());
			if (clicked != null) {
				IFocus.Mode mode = showRecipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT;
				recipesGui.show(new Focus<Object>(mode, clicked.getValue()));
				clicked.onClickHandled();
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
