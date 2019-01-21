package mezz.jei.input;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.ResourceLocation;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRegistry;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IHideModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.config.KeyBindings;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipeClickableArea;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientRegistry;
import mezz.jei.recipes.RecipeRegistry;
import mezz.jei.runtime.JeiRuntime;
import mezz.jei.util.ReflectionUtil;

public class InputHandler {
	private final RecipeRegistry recipeRegistry;
	private final IIngredientRegistry ingredientRegistry;
	private final IngredientFilter ingredientFilter;
	private final RecipesGui recipesGui;
	private final IngredientListOverlay ingredientListOverlay;
	private final IHideModeConfig hideModeConfig;
	private final IWorldConfig worldConfig;
	private final LeftAreaDispatcher leftAreaDispatcher;
	private final BookmarkList bookmarkList;
	private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();
	private final IntSet clickHandled = new IntArraySet();

	public InputHandler(
		JeiRuntime runtime,
		IngredientFilter ingredientFilter,
		IngredientRegistry ingredientRegistry,
		IngredientListOverlay ingredientListOverlay,
		IHideModeConfig hideModeConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		LeftAreaDispatcher leftAreaDispatcher,
		BookmarkList bookmarkList
	) {
		this.recipeRegistry = runtime.getRecipeRegistry();
		this.ingredientRegistry = ingredientRegistry;
		this.ingredientFilter = ingredientFilter;
		this.recipesGui = runtime.getRecipesGui();
		this.ingredientListOverlay = ingredientListOverlay;
		this.hideModeConfig = hideModeConfig;
		this.worldConfig = worldConfig;
		this.leftAreaDispatcher = leftAreaDispatcher;
		this.bookmarkList = bookmarkList;

		this.showsRecipeFocuses.add(recipesGui);
		this.showsRecipeFocuses.add(ingredientListOverlay);
		this.showsRecipeFocuses.add(leftAreaDispatcher);
		this.showsRecipeFocuses.add(new GuiContainerWrapper(guiScreenHelper));
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	@SubscribeEvent
	public void onGuiKeyPressedEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (hasKeyboardFocus() && handleKeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	@SubscribeEvent
	public void onGuiCharTypedEvent(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (hasKeyboardFocus() && handleCharTyped(event.getCodePoint(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	@SubscribeEvent
	public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
		if (!hasKeyboardFocus() && handleKeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	@SubscribeEvent
	public void onGuiCharTypedEvent(GuiScreenEvent.KeyboardCharTypedEvent.Post event) {
		if (!hasKeyboardFocus() && handleCharTyped(event.getCodePoint(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public void onGuiMouseEvent(GuiScreenEvent.MouseClickedEvent.Pre event) {
		int mouseButton = event.getButton();
		if (mouseButton > -1) {
			if (!clickHandled.contains(mouseButton)) {
				if (handleMouseClick(event.getGui(), mouseButton, event.getMouseX(), event.getMouseY())) {
					clickHandled.add(mouseButton);
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onGuiMouseEvent(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		int mouseButton = event.getButton();
		if (mouseButton > -1) {
			if (clickHandled.contains(mouseButton)) {
				clickHandled.remove(mouseButton);
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public void onGuiMouseEvent(GuiScreenEvent.MouseScrollEvent.Pre event) {
		double dWheel = event.getScrollDelta();
		double mouseX = event.getMouseX();
		double mouseY = event.getMouseY();
		if (ingredientListOverlay.handleMouseScrolled(mouseX, mouseY, dWheel) ||
			leftAreaDispatcher.handleMouseScrolled(mouseX, mouseY, dWheel)) {
			event.setCanceled(true);
		}
	}

	private boolean handleMouseClick(GuiScreen guiScreen, int mouseButton, double mouseX, double mouseY) {
		IClickedIngredient<?> clicked = getFocusUnderMouseForClick(mouseX, mouseY);
		if (worldConfig.isHideModeEnabled() && clicked != null && handleClickEdit(clicked)) {
			return true;
		}
		if (ingredientListOverlay.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}
		if (leftAreaDispatcher.handleMouseClicked(mouseX, mouseY, mouseButton)) {
			return true;
		}

		if (clicked != null && handleMouseClickedFocus(mouseButton, clicked)) {
			return true;
		}
		InputMappings.Input input = InputMappings.Type.MOUSE.getOrMakeInput(mouseButton);
		if (handleFocusKeybinds(input)) {
			return true;
		}

		if (guiScreen instanceof GuiContainer) {
			GuiContainer guiContainer = (GuiContainer) guiScreen;
			RecipeClickableArea clickableArea = recipeRegistry.getRecipeClickableArea(guiContainer, mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop());
			if (clickableArea != null) {
				List<ResourceLocation> recipeCategoryUids = clickableArea.getRecipeCategoryUids();
				recipesGui.showCategories(recipeCategoryUids);
				return true;
			}
		}

		return handleGlobalKeybinds(input);
	}

	@Nullable
	private IClickedIngredient<?> getFocusUnderMouseForClick(double mouseX, double mouseY) {
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
	private IClickedIngredient<?> getIngredientUnderMouseForKey(double mouseX, double mouseY) {
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
			Focus<?> focus = new Focus<>(IFocus.Mode.OUTPUT, clicked.getValue());
			recipesGui.show(focus);
			clicked.onClickHandled();
			return true;
		} else if (mouseButton == 1) {
			Focus<?> focus = new Focus<>(IFocus.Mode.INPUT, clicked.getValue());
			recipesGui.show(focus);
			clicked.onClickHandled();
			return true;
		}

		return false;
	}

	private <V> boolean handleClickEdit(IClickedIngredient<V> clicked) {
		V ingredient = clicked.getValue();
		IngredientBlacklistType blacklistType = GuiScreen.isCtrlKeyDown() ? IngredientBlacklistType.WILDCARD : IngredientBlacklistType.ITEM;

		IIngredientHelper<V> ingredientHelper = ingredientRegistry.getIngredientHelper(ingredient);

		if (hideModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
			hideModeConfig.removeIngredientFromConfigBlacklist(ingredientFilter, ingredientRegistry, ingredient, blacklistType, ingredientHelper);
		} else {
			hideModeConfig.addIngredientToConfigBlacklist(ingredientFilter, ingredientRegistry, ingredient, blacklistType, ingredientHelper);
		}
		clicked.onClickHandled();
		return true;
	}

	private boolean hasKeyboardFocus() {
		// TODO 1.13 when GuiContainer returns the right value so Post events get fired
		// https://github.com/MinecraftForge/MinecraftForge/pull/5367
		return true;
//		return ingredientListOverlay.hasKeyboardFocus();
	}

	private boolean handleCharTyped(char codePoint, int modifiers) {
		return ingredientListOverlay.onCharTyped(codePoint, modifiers);
	}

	private boolean handleKeyEvent(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getInputByCode(keyCode, scanCode);
		if (ingredientListOverlay.hasKeyboardFocus()) {
			if (KeyBindings.isInventoryCloseKey(input) || KeyBindings.isEnterKey(keyCode)) {
				ingredientListOverlay.setKeyboardFocus(false);
				return true;
			} else if (ingredientListOverlay.onKeyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}

		if (handleGlobalKeybinds(input)) {
			return true;
		}

		if (!isContainerTextFieldFocused()) {
			if (handleFocusKeybinds(input)) {
				return true;
			}
			return ingredientListOverlay.onKeyPressed(keyCode, scanCode, modifiers);
		}

		return false;
	}

	private boolean handleGlobalKeybinds(InputMappings.Input input) {
		if (KeyBindings.toggleOverlay.isActiveAndMatches(input)) {
			worldConfig.toggleOverlayEnabled();
			return false;
		}
		if (KeyBindings.toggleBookmarkOverlay.isActiveAndMatches(input)) {
			worldConfig.toggleBookmarkEnabled();
			return false;
		}
		return ingredientListOverlay.onGlobalKeyPressed(input);
	}

	private boolean handleFocusKeybinds(InputMappings.Input input) {
		final boolean showRecipe = KeyBindings.showRecipe.isActiveAndMatches(input);
		final boolean showUses = KeyBindings.showUses.isActiveAndMatches(input);
		final boolean bookmark = KeyBindings.bookmark.isActiveAndMatches(input);
		if (showRecipe || showUses || bookmark) {
			IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseUtil.getX(), MouseUtil.getY());
			if (clicked != null) {
				if (bookmark) {
					if (bookmarkList.remove(clicked.getValue())) {
						if (bookmarkList.isEmpty()) {
							worldConfig.setBookmarkEnabled(false);
						}
						return true;
					} else {
						worldConfig.setBookmarkEnabled(true);
						return bookmarkList.add(clicked.getValue());
					}
				} else {
					IFocus.Mode mode = showRecipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT;
					recipesGui.show(new Focus<Object>(mode, clicked.getValue()));
					clicked.onClickHandled();
					return true;
				}
			}
		}
		return false;
	}

	private boolean isContainerTextFieldFocused() {
		GuiScreen gui = Minecraft.getInstance().currentScreen;
		if (gui == null) {
			return false;
		}
		GuiTextField textField = ReflectionUtil.getFieldWithClass(gui, GuiTextField.class);
		return textField != null && textField.getVisible() && textField.isFocused();
	}

}
