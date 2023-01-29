package mezz.jei.input;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import mezz.jei.input.click.ClickFocusHandler;
import mezz.jei.input.click.GuiAreaClickHandler;
import mezz.jei.input.click.MouseClickState;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.InputMappings;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.bookmarks.BookmarkList;
import mezz.jei.config.IEditModeConfig;
import mezz.jei.config.IWorldConfig;
import mezz.jei.config.IngredientBlacklistType;
import mezz.jei.config.KeyBindings;
import mezz.jei.events.EventBusHelper;
import mezz.jei.gui.Focus;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.ingredients.IngredientFilter;
import mezz.jei.ingredients.IngredientManager;
import mezz.jei.util.ReflectionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InputHandler {
	private static final Logger LOGGER = LogManager.getLogger();

	private final IIngredientManager ingredientManager;
	private final WeakReference<IngredientFilter> weakIngredientFilter;
	private final RecipesGui recipesGui;
	private final IngredientListOverlay ingredientListOverlay;
	private final IEditModeConfig editModeConfig;
	private final IWorldConfig worldConfig;
	private final GuiScreenHelper guiScreenHelper;
	private final LeftAreaDispatcher leftAreaDispatcher;
	private final BookmarkList bookmarkList;
	private final List<IShowsRecipeFocuses> showsRecipeFocuses = new ArrayList<>();
	private final IMouseHandler mouseClickHandler;
	private final IMouseDragHandler mouseDragHandler;

	public InputHandler(
		RecipesGui recipesGui,
		IngredientFilter ingredientFilter,
		IngredientManager ingredientManager,
		IngredientListOverlay ingredientListOverlay,
		IEditModeConfig editModeConfig,
		IWorldConfig worldConfig,
		GuiScreenHelper guiScreenHelper,
		LeftAreaDispatcher leftAreaDispatcher,
		BookmarkList bookmarkList
	) {
		this.ingredientManager = ingredientManager;
		this.weakIngredientFilter = new WeakReference<>(ingredientFilter);
		this.recipesGui = recipesGui;
		this.ingredientListOverlay = ingredientListOverlay;
		this.editModeConfig = editModeConfig;
		this.worldConfig = worldConfig;
		this.guiScreenHelper = guiScreenHelper;
		this.leftAreaDispatcher = leftAreaDispatcher;
		this.bookmarkList = bookmarkList;

		this.showsRecipeFocuses.add(recipesGui);
		this.showsRecipeFocuses.add(ingredientListOverlay);
		this.showsRecipeFocuses.add(leftAreaDispatcher);
		this.showsRecipeFocuses.add(new GuiContainerWrapper(guiScreenHelper));

		this.mouseClickHandler = new CombinedMouseHandler(
			new ClickEditHandler(),
			ingredientListOverlay.getMouseHandler(),
			leftAreaDispatcher.getMouseHandler(),
			new ClickFocusHandler(this, recipesGui),
			new ClickGlobalHandler(),
			new GuiAreaClickHandlerGenerator()
		);
		this.mouseDragHandler = ingredientListOverlay.getMouseDragHandler();
	}

	public void registerToEventBus() {
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.InitGuiEvent.class, InputHandler::onInitGuiEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardKeyPressedEvent.Pre.class, InputHandler::onGuiKeyPressedEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardCharTypedEvent.Pre.class, InputHandler::onGuiCharTypedEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardKeyPressedEvent.Post.class, InputHandler::onGuiKeyboardEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.KeyboardCharTypedEvent.Post.class, InputHandler::onGuiCharTypedEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.MouseClickedEvent.Pre.class, InputHandler::onGuiMouseEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.MouseReleasedEvent.Pre.class, InputHandler::onGuiMouseEvent);
		EventBusHelper.registerWeakListener(this, GuiScreenEvent.MouseScrollEvent.Pre.class, InputHandler::onGuiMouseEvent);
	}

	public void onInitGuiEvent(GuiScreenEvent.InitGuiEvent event) {
		this.mouseClickHandler.handleMouseClickedOut(0);
		this.mouseClickHandler.handleMouseClickedOut(1);
		this.mouseClickHandler.handleMouseClickedOut(2);
		this.mouseDragHandler.handleDragCanceled();
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public void onGuiKeyPressedEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
		if (hasKeyboardFocus()) {
			handleKeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers());
			event.setCanceled(true);
		}
	}

	/**
	 * When we have keyboard focus, use Pre
	 */
	public void onGuiCharTypedEvent(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
		if (hasKeyboardFocus() && handleCharTyped(event.getCodePoint(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onGuiKeyboardEvent(GuiScreenEvent.KeyboardKeyPressedEvent.Post event) {
		if (!hasKeyboardFocus() && handleKeyEvent(event.getKeyCode(), event.getScanCode(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	/**
	 * Without keyboard focus, use Post
	 */
	public void onGuiCharTypedEvent(GuiScreenEvent.KeyboardCharTypedEvent.Post event) {
		if (!hasKeyboardFocus() && handleCharTyped(event.getCodePoint(), event.getModifiers())) {
			event.setCanceled(true);
		}
	}

	public void onGuiMouseEvent(GuiScreenEvent.MouseClickedEvent.Pre event) {
		int mouseButton = event.getButton();
		if (mouseButton > -1) {
			Screen screen = event.getGui();
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			IMouseHandler handler = this.mouseClickHandler.handleClick(screen, mouseX, mouseY, mouseButton, MouseClickState.SIMULATE);
			IMouseDragHandler dragHandler = null;
			if (mouseButton == 0) {
				dragHandler = this.mouseDragHandler.handleDragStart(screen, mouseX, mouseY);
			}
			if (handler != null || dragHandler != null) {
				event.setCanceled(true);
			}
		}
	}

	public void onGuiMouseEvent(GuiScreenEvent.MouseReleasedEvent.Pre event) {
		int mouseButton = event.getButton();
		if (mouseButton > -1) {
			Screen screen = event.getGui();
			double mouseX = event.getMouseX();
			double mouseY = event.getMouseY();
			IMouseHandler handled = this.mouseClickHandler.handleClick(screen, mouseX, mouseY, mouseButton, MouseClickState.EXECUTE);
			IMouseDragHandler dragHandled = null;
			if (mouseButton == 0) {
				dragHandled = this.mouseDragHandler.handleDragComplete(screen, mouseX, mouseY);
			}
			if (handled != null || dragHandled != null) {
				event.setCanceled(true);
			}
		}
	}

	public void onGuiMouseEvent(GuiScreenEvent.MouseScrollEvent.Pre event) {
		double dWheel = event.getScrollDelta();
		double mouseX = event.getMouseX();
		double mouseY = event.getMouseY();
		IMouseHandler overlayMouseHandler = ingredientListOverlay.getMouseHandler();
		if (overlayMouseHandler.handleMouseScrolled(mouseX, mouseY, dWheel) ||
			leftAreaDispatcher.handleMouseScrolled(mouseX, mouseY, dWheel)) {
			event.setCanceled(true);
		}
	}

	@Nullable
	public IClickedIngredient<?> getFocusUnderMouseForClick(double mouseX, double mouseY, int mouseButton) {
		for (IShowsRecipeFocuses gui : showsRecipeFocuses) {
			if (!isConflictingVanillaMouseButton(mouseButton) || gui.canSetFocusWithMouse()) {
				IClickedIngredient<?> clicked = gui.getIngredientUnderMouse(mouseX, mouseY);
				if (clicked != null) {
					return clicked;
				}
			}
		}
		return null;
	}

	private static boolean isConflictingVanillaMouseButton(int mouseButton) {
		return mouseButton <= 2;
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

	public class ClickEditHandler implements IMouseHandler {
		@Override
		public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
			if (!worldConfig.isEditModeEnabled()) {
				return null;
			}
			IClickedIngredient<?> clicked = getFocusUnderMouseForClick(mouseX, mouseY, mouseButton);
			if (clicked == null) {
				return null;
			}
			if (!clickState.isSimulate()) {
				handler(clicked);
			}
			return LimitedAreaMouseHandler.create(this, clicked.getArea());
		}

		private <V> void handler(IClickedIngredient<V> clicked) {
			V ingredient = clicked.getValue();
			IngredientBlacklistType blacklistType = Screen.hasControlDown() ? IngredientBlacklistType.WILDCARD : IngredientBlacklistType.ITEM;

			IIngredientHelper<V> ingredientHelper = ingredientManager.getIngredientHelper(ingredient);

			IngredientFilter ingredientFilter = weakIngredientFilter.get();
			if (ingredientFilter == null) {
				LOGGER.error("Can't edit the config blacklist, the ingredient filter is null");
			} else {
				if (editModeConfig.isIngredientOnConfigBlacklist(ingredient, ingredientHelper)) {
					editModeConfig.removeIngredientFromConfigBlacklist(ingredient, blacklistType, ingredientHelper);
				} else {
					editModeConfig.addIngredientToConfigBlacklist(ingredient, blacklistType, ingredientHelper);
				}
			}
		}
	}

	public class ClickGlobalHandler implements IMouseHandler {
		@Override
		public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
			InputMappings.Input input = InputMappings.Type.MOUSE.getOrCreate(mouseButton);
			if (handleGlobalKeybinds(input, clickState)) {
				return this;
			}
			return null;
		}
	}

	public class GuiAreaClickHandlerGenerator implements IMouseHandler {
		@Nullable
		@Override
		public IMouseHandler handleClick(Screen screen, double mouseX, double mouseY, int mouseButton, MouseClickState clickState) {
			if (screen instanceof ContainerScreen) {
				ContainerScreen<?> guiContainer = (ContainerScreen<?>) screen;
				IGuiClickableArea clickableArea = guiScreenHelper.getGuiClickableArea(guiContainer, mouseX - guiContainer.getGuiLeft(), mouseY - guiContainer.getGuiTop());
				if (clickableArea != null) {
					return new GuiAreaClickHandler(recipesGui, clickableArea, guiContainer);
				}
			}
			return null;
		}
	}

	private boolean hasKeyboardFocus() {
		return ingredientListOverlay.hasKeyboardFocus();
	}

	private boolean handleCharTyped(char codePoint, int modifiers) {
		return ingredientListOverlay.onCharTyped(codePoint, modifiers);
	}

	private boolean handleKeyEvent(int keyCode, int scanCode, int modifiers) {
		InputMappings.Input input = InputMappings.getKey(keyCode, scanCode);
		if (ingredientListOverlay.hasKeyboardFocus()) {
			if (KeyBindings.isInventoryCloseKey(input) || KeyBindings.isEnterKey(keyCode)) {
				ingredientListOverlay.clearKeyboardFocus();
				return true;
			} else if (ingredientListOverlay.onKeyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}

		if (handleGlobalKeybinds(input, MouseClickState.VANILLA)) {
			return true;
		}

		if (!isContainerTextFieldFocused() && !ingredientListOverlay.hasKeyboardFocus()) {
			IClickedIngredient<?> clicked = getIngredientUnderMouseForKey(MouseUtil.getX(), MouseUtil.getY());
			if (clicked != null && handleFocusKeybinds(clicked, input, MouseClickState.VANILLA)) {
				ingredientListOverlay.clearKeyboardFocus();
				return true;
			}
			return ingredientListOverlay.onKeyPressed(keyCode, scanCode, modifiers);
		}

		return false;
	}

	private boolean handleGlobalKeybinds(InputMappings.Input input, MouseClickState clickState) {
		if (KeyBindings.toggleOverlay.isActiveAndMatches(input)) {
			if (!clickState.isSimulate()) {
				worldConfig.toggleOverlayEnabled();
			}
			return true;
		}
		if (KeyBindings.toggleBookmarkOverlay.isActiveAndMatches(input)) {
			if (!clickState.isSimulate()) {
				worldConfig.toggleBookmarkEnabled();
			}
			return true;
		}
		return ingredientListOverlay.onGlobalKeyPressed(input, clickState);
	}

	public boolean handleFocusKeybinds(IClickedIngredient<?> clicked, InputMappings.Input input, MouseClickState clickState) {
		final boolean showRecipe = KeyBindings.showRecipe.isActiveAndMatches(input);
		final boolean showUses = KeyBindings.showUses.isActiveAndMatches(input);
		if (showRecipe || showUses) {
			if (!clickState.isSimulate()) {
				IFocus.Mode mode = showRecipe ? IFocus.Mode.OUTPUT : IFocus.Mode.INPUT;
				recipesGui.show(new Focus<Object>(mode, clicked.getValue()));
			}
			this.ingredientListOverlay.clearKeyboardFocus();
			return true;
		} else if (KeyBindings.bookmark.isActiveAndMatches(input)) {
			if (!clickState.isSimulate()) {
				if (bookmarkList.remove(clicked.getValue())) {
					if (bookmarkList.isEmpty()) {
						worldConfig.setBookmarkEnabled(false);
					}
					return true;
				} else {
					worldConfig.setBookmarkEnabled(true);
					return bookmarkList.add(clicked.getValue());
				}
			}
			this.ingredientListOverlay.clearKeyboardFocus();
			return true;
		}
		return false;
	}

	private boolean isContainerTextFieldFocused() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft == null) {
			return false;
		}
		Screen screen = minecraft.screen;
		if (screen == null) {
			return false;
		}
		TextFieldWidget textField = ReflectionUtil.getFieldWithClass(screen, TextFieldWidget.class);
		return textField != null && textField.isVisible() && textField.isFocused();
	}

}
