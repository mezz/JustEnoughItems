package mezz.jei.gui.config.screen;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.UserInputRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class JeiConfigScreen extends Screen {
	private static final int SEARCH_TEXT_COLOR = 0xFFE8EEF7;
	private static final int SEARCH_HINT_COLOR = 0xFF8F98A6;

	public static Screen create(@Nullable Screen parent) {
		return new JeiConfigScreen(parent, Internal.getClientConfigSchema());
	}

	private final UserInputRouter inputHandler;
	private final IInternalKeyMappings keyBindings;
	private final EditBox searchBox;
	private final ConfigScreenLayout layout = new ConfigScreenLayout();
	private final ConfigScreenModel model;
	private final ConfigScreenController controller;
	private final ConfigScreenView view;

	@Nullable
	private final Screen parent;

	@Nullable
	private ConfigValueSelector<?> valueSelector;

	private JeiConfigScreen(@Nullable Screen parent, IConfigSchema clientSchema) {
		super(Component.translatable("jei.config.screen.title"));
		this.parent = parent;

		this.keyBindings = Internal.getKeyMappings();

		Font font = Minecraft.getInstance().font;
		this.searchBox = new EditBox(font, 0, 0, 0, ConfigScreenLayout.SEARCH_HEIGHT, Component.translatable("jei.config.screen.search"));
		this.searchBox.setMaxLength(64);
		this.searchBox.setBordered(false);
		this.searchBox.setHint(Component.translatable("jei.config.screen.search"));
		updateSearchTextColor("");

		this.model = new ConfigScreenModel(clientSchema);
		this.controller = new ConfigScreenController(model, layout, () -> {
			if (!searchBox.getValue().isEmpty()) {
				searchBox.setValue("");
			}
		});
		this.view = new ConfigScreenView(searchBox, model, layout, controller);
		this.searchBox.setResponder(searchText -> {
			updateSearchTextColor(searchText);
			controller.setSearchText(searchText);
		});

		List<IUserInputHandler> allInputHandlers = new ArrayList<>();
		List<IJeiConfigCategory> categories = model.getCategories();
		ConfigEntryWidgetFactory entryWidgetFactory = new ConfigEntryWidgetFactory(this::openValueSelector, controller::updateContentLayout);
		for (int i = 0; i < categories.size(); i++) {
			IJeiConfigCategory category = categories.get(i);
			ConfigCategoryWidget widget = new ConfigCategoryWidget(
				category,
				layout::getContentArea,
				controller::updateContentLayout,
				entryWidgetFactory
			);
			model.addCategoryWidget(widget);
			allInputHandlers.add(widget.createInputHandler());

			ConfigNavItem navItem = new ConfigNavItem(
				category.getLocalizedName(),
				i,
				widget,
				layout::getNavArea,
				controller::setActiveCategory
			);
			model.addNavItem(navItem);
		}
		allInputHandlers.addAll(model.getNavItems());

		allInputHandlers.addFirst(new ConfigValueSelectorInputHandler(
			() -> valueSelector,
			this::closeValueSelector,
			controller::updateContentLayout
		));
		this.inputHandler = new UserInputRouter("JeiConfigScreen", allInputHandlers);
	}

	private void updateSearchTextColor(String searchText) {
		this.searchBox.setTextColor(searchText.isEmpty() ? SEARCH_HINT_COLOR : SEARCH_TEXT_COLOR);
	}

	private void openValueSelector(ConfigValueSelector<?> selector) {
		this.valueSelector = selector;
	}

	private void closeValueSelector() {
		this.valueSelector = null;
	}

	private void flushPendingInput() {
		inputHandler.handleGuiChange();
		closeValueSelector();
	}

	@Nullable
	ImmutableRect2i getValueSelectorArea() {
		ConfigValueSelector<?> valueSelector = this.valueSelector;
		return valueSelector == null ? null : valueSelector.area;
	}

	public static IGlobalGuiHandler createGuiHandler() {
		return new ConfigScreenGuiHandler();
	}

	@Override
	protected void init() {
		super.init();
		layout.updateScreenBounds(width, height, searchBox);
		addWidget(searchBox);

		controller.calculateNavItemHeights();
		layout.resetNavScroll();
		controller.updateNavLayout();
		if (model.hasActiveCategory()) {
			controller.setActiveCategory(model.getActiveCategoryIndex());
		} else {
			controller.updateContentLayout();
		}
	}

	@Override
	public void onClose() {
		requestClose();
	}

	private void requestClose() {
		flushPendingInput();
		if (controller.hasPendingChanges()) {
			openPendingChangesConfirmation();
			return;
		}
		closeWithoutPrompt();
	}

	private void closeWithoutPrompt() {
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	private void openPendingChangesConfirmation() {
		if (minecraft == null) {
			return;
		}
		minecraft.setScreen(new ConfirmScreen(applyChanges -> {
			if (applyChanges) {
				controller.applyPendingChanges();
			} else {
				controller.discardPendingChanges();
			}
			closeWithoutPrompt();
		},
			Component.translatable("jei.config.screen.pendingChanges.title"),
			Component.translatable("jei.config.screen.pendingChanges.message"),
			Component.translatable("jei.config.screen.apply"),
			Component.translatable("jei.config.screen.discard")
		));
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (forwardCharTypedToEntries(codePoint, modifiers)) {
			return true;
		}
		if (searchBox.isFocused() && searchBox.charTyped(codePoint, modifiers)) {
			return true;
		}
		return super.charTyped(codePoint, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		UserInput input = UserInput.fromVanilla(keyCode, scanCode, modifiers, InputType.IMMEDIATE);
		if (input.is(Minecraft.getInstance().options.keyInventory)) {
			requestClose();
			return true;
		}
		if (forwardKeyPressedToEntries(keyCode, scanCode, modifiers)) {
			return true;
		}
		if (searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	private boolean forwardCharTypedToEntries(char codePoint, int modifiers) {
		for (ConfigEntryWidget<?> entry : controller.getVisibleEntryWidgets()) {
			if (entry.charTyped(codePoint, modifiers)) {
				return true;
			}
		}
		return false;
	}

	private boolean forwardKeyPressedToEntries(int keyCode, int scanCode, int modifiers) {
		for (ConfigEntryWidget<?> entry : controller.getVisibleEntryWidgets()) {
			if (entry.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && controller.startContentScrollDrag(mouseX, mouseY)) {
			return true;
		}
		if (button == 0 && controller.startNavScrollDrag(mouseX, mouseY)) {
			return true;
		}
		ImmutableRect2i resetCategoryButtonArea = layout.getResetCategoryButtonArea();
		ImmutableRect2i applyButtonArea = layout.getApplyButtonArea();
		if (button == 0 && (resetCategoryButtonArea.contains(mouseX, mouseY) || applyButtonArea.contains(mouseX, mouseY))) {
			flushPendingInput();
			if (resetCategoryButtonArea.contains(mouseX, mouseY) && !controller.hasResettableEntries()) {
				return true;
			}
			if (applyButtonArea.contains(mouseX, mouseY) && !controller.hasPendingChanges()) {
				return true;
			}
		}
		if (searchBox.isFocused() && !searchBox.isMouseOver(mouseX, mouseY)) {
			searchBox.setFocused(false);
		}
		boolean ret = UserInput.fromVanilla(mouseX, mouseY, button, InputType.SIMULATE)
			.map(this::handleInput)
			.orElse(false);
		return ret || super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (button == 0 && (controller.stopContentScrollDrag() || controller.stopNavScrollDrag())) {
			return true;
		}
		ImmutableRect2i resetCategoryButtonArea = layout.getResetCategoryButtonArea();
		if (button == 0 && resetCategoryButtonArea.contains(mouseX, mouseY) && controller.hasResettableEntries()) {
			flushPendingInput();
			controller.resetTargetEntries();
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			return true;
		}
		ImmutableRect2i applyButtonArea = layout.getApplyButtonArea();
		if (button == 0 && applyButtonArea.contains(mouseX, mouseY) && controller.hasPendingChanges()) {
			flushPendingInput();
			controller.applyPendingChanges();
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			return true;
		}
		boolean ret = UserInput.fromVanilla(mouseX, mouseY, button, InputType.EXECUTE)
			.map(this::handleInput)
			.orElse(false);
		if (ret) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
		}
		return ret || super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (button == 0 && controller.dragContentScroll(mouseY)) {
			return true;
		}
		if (button == 0 && controller.dragNavScroll(mouseY)) {
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	private boolean handleInput(UserInput input) {
		return this.inputHandler.handleUserInput(this, input, keyBindings);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (inputHandler.handleMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			return true;
		}
		if (controller.scroll(mouseX, mouseY, scrollY)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (minecraft == null) {
			return;
		}
		renderTransparentBackground(guiGraphics);
		controller.stepScrollPositions();
		view.render(guiGraphics, mouseX, mouseY, partialTick, valueSelector);
	}
}
