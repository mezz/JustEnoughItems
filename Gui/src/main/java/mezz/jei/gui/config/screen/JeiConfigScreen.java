package mezz.jei.gui.config.screen;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.config.IJeiConfigCategory;
import mezz.jei.common.Internal;
import mezz.jei.common.config.file.IConfigSchema;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.gui.GuiProperties;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.InputType;
import mezz.jei.gui.input.UserInput;
import mezz.jei.gui.input.handlers.UserInputRouter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JeiConfigScreen extends Screen {

    private static final int MIN_GUI_WIDTH = 200;
    private static final int MIN_HEIGHT = 210;
    private static final int NAV_WIDTH = 56;
    private static final int ENTRY_HEIGHT = 20;
    private static final int NAV_ITEM_HEIGHT = 14;
    private static final int NAV_ITEM_GAP = 2;
    private static final int SEARCH_HEIGHT = 18;
    private static final double SCROLL_SPEED = 10.0;
    private static final double SCROLL_LERP = 0.35;
    private static final int RESET_BTN_W = 40;

	public static Screen create() {
		return new JeiConfigScreen(Internal.getClientConfigSchema());
	}

	private final DrawableNineSliceTexture background;
	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
    private ImmutableRect2i navArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i contentArea = ImmutableRect2i.EMPTY;
    private ImmutableRect2i resetCategoryBtn = ImmutableRect2i.EMPTY;

	private final UserInputRouter inputHandler;
	private final IInternalKeyMappings keyBindings;

    private final EditBox searchBox;
    private String searchText = "";
	private ImmutableRect2i scrollBarArea = ImmutableRect2i.EMPTY;
	private final DrawableNineSliceTexture scrollbarMarker;
	private final DrawableNineSliceTexture scrollbarBackground;
    private double targetScrollY = 0;
    private double currentScrollY = 0;
    private int totalContentHeight = 0;

    private int totalNavHeight = 0;
    private double navTargetScrollY = 0;
    private double navCurrentScrollY = 0;

    private final List<NavItem> navItems = new ArrayList<>();
    private int activeNavIndex = 0;
    private final List<ConfigCategoryWidget> categoryWidgets = new ArrayList<>();

	@Nullable
    private ConfigValueSelector<?> valueSelector;

	private JeiConfigScreen(IConfigSchema clientSchema) {
        super(Component.literal("JEI Configuration"));

		Textures textures = Internal.getTextures();
        this.background = textures.getRecipeGuiBackground();
        this.scrollbarMarker = textures.getScrollbarMarker();
		this.scrollbarBackground = textures.getScrollbarBackground();
		this.keyBindings = Internal.getKeyMappings();

        List<? extends IJeiConfigCategory> allCategories = clientSchema.getCategories();

        List<IUserInputHandler> allInputHandlers = new ArrayList<>();
        for (int i = 0; i < allCategories.size(); i++) {
            IJeiConfigCategory category = allCategories.get(i);
            ConfigCategoryWidget widget = new ConfigCategoryWidget(
                    category,
                    this::getContentArea,
                    this::updateLayout,
                    this::openValueSelector
            );
            categoryWidgets.add(widget);
            allInputHandlers.add(widget.createInputHandler());
            navItems.add(new NavItem(category.getLocalizedName(), i));
        }

        allInputHandlers.addAll(navItems);

        allInputHandlers.addFirst(new ValueSelectorInputHandler());
        this.inputHandler = new UserInputRouter("JeiConfigScreen", allInputHandlers);

        Font font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, 0, SEARCH_HEIGHT, Component.literal("Search..."));
        this.searchBox.setMaxLength(64);
        this.searchBox.setBordered(false);
        this.searchBox.setTextColor(0xFFFFFFFF);
        this.searchBox.setResponder(text -> {
            this.searchText = text.toLowerCase();
            targetScrollY = 0;
            currentScrollY = 0;
            updateLayout();
        });
    }

    private void openValueSelector(ConfigValueSelector<?> selector) {
        this.valueSelector = selector;
    }

    private ImmutableRect2i getContentArea() {
        return contentArea;
	}

	@Nullable
	public IGuiProperties getGuiProperties() {
		if (width <= 0 || height <= 0) {
			return null;
		}
		return new GuiProperties(
			getClass(),
			area.getX(),
			area.getY(),
			area.getWidth(),
			area.getHeight(),
			width,
			height
		);
	}

	public static IGlobalGuiHandler createGuiHandler() {
		return new IGlobalGuiHandler() {
			@Override
			public Collection<Rect2i> getGuiExtraAreas() {
				Minecraft minecraft = Minecraft.getInstance();
				if (minecraft.screen instanceof JeiConfigScreen screen) {
					if (screen.valueSelector != null) {
                        ImmutableRect2i selectorArea = screen.valueSelector.area;
                        return Collections.singleton(new Rect2i(
                                selectorArea.getX(), selectorArea.getY(),
                                selectorArea.getWidth(), selectorArea.getHeight()
                        ));
					}
				}
				return Collections.emptyList();
			}
		};
	}

	@Override
	protected void init() {
		super.init();
        int guiWidth = Math.max(MIN_GUI_WIDTH, Math.min(width - 40, 280));
        int guiHeight = Math.max(MIN_HEIGHT, Math.min(height - 40, 300));
        int guiLeft = (width - guiWidth) / 2;
        int guiTop = (height - guiHeight) / 2;
        area = new ImmutableRect2i(guiLeft, guiTop, guiWidth, guiHeight);

        int innerTop = area.getY() + 6;
        int innerBottom = area.getY() + area.getHeight() - 6;
        int innerHeight = innerBottom - innerTop;

        navArea = new ImmutableRect2i(
                area.getX() + 4,
                innerTop,
                NAV_WIDTH,
                innerHeight
        );

        int contentLeft = navArea.getX() + navArea.getWidth() + 4;
        int contentWidth = area.getWidth() - NAV_WIDTH - 20;

        resetCategoryBtn = new ImmutableRect2i(
                contentLeft + contentWidth - RESET_BTN_W + 1,
                innerTop,
                RESET_BTN_W - 3,
                SEARCH_HEIGHT
        );
        searchBox.setX(contentLeft + 2);
        searchBox.setY(innerTop + 5);
        searchBox.setWidth(contentWidth - RESET_BTN_W - 4);
        searchBox.setHeight(SEARCH_HEIGHT);
        addWidget(searchBox);

        int contentTop = innerTop + SEARCH_HEIGHT + 2;
        contentArea = new ImmutableRect2i(
                contentLeft,
                contentTop,
                contentWidth,
                innerBottom - contentTop
        );

        scrollBarArea = new ImmutableRect2i(
                area.getX() + area.getWidth() - 14,
                contentArea.getY(),
                10,
                contentArea.getHeight()
        );

        for (NavItem navItem : navItems) {
            navItem.calculateHeight(navArea.getWidth());
        }
        navCurrentScrollY = 0;
        navTargetScrollY = 0;
        updateNavLayout();

        setActiveCategory(activeNavIndex);
    }

    private void setActiveCategory(int index) {
        if (index < 0 || index >= categoryWidgets.size()) {
            return;
        }
        activeNavIndex = index;
        targetScrollY = 0;
        currentScrollY = 0;

        for (int i = 0; i < categoryWidgets.size(); i++) {
            ConfigCategoryWidget w = categoryWidgets.get(i);
            if (i == index) {
                w.expanded = true;
            } else {
                w.expanded = false;
                w.resetBounds();
            }
        }

        searchBox.setValue("");
        searchText = "";
		updateLayout();
	}

    private boolean matchesSearch(ConfigEntryWidget<?> entry) {
        if (searchText.isEmpty()) {
            return true;
        }
        String name = entry.fullName.getString().toLowerCase();
        return name.contains(searchText);
    }

    private boolean isSearching() {
        return !searchText.isEmpty();
    }

	private void updateLayout() {
        int currentY = contentArea.getY() - (int) currentScrollY;
        totalContentHeight = 0;

        if (isSearching()) {
            for (ConfigCategoryWidget widget : categoryWidgets) {
                widget.area = ImmutableRect2i.EMPTY;
                for (ConfigEntryWidget<?> entryWidget : widget.getEntryWidgets()) {
                    if (!matchesSearch(entryWidget)) {
                        entryWidget.area = ImmutableRect2i.EMPTY;
                        entryWidget.nameArea = ImmutableRect2i.EMPTY;
                        continue;
                    }
                    int ew = contentArea.getWidth() - 4;
                    entryWidget.updateBounds(new ImmutableRect2i(contentArea.getX() + 2, currentY, ew, ENTRY_HEIGHT));
                    int h = entryWidget.getHeight();
                    entryWidget.updateBounds(new ImmutableRect2i(contentArea.getX() + 2, currentY, ew, h));
                    currentY += h;
                    totalContentHeight += h;
                }
            }
        } else {
            ConfigCategoryWidget activeWidget = categoryWidgets.get(activeNavIndex);
            activeWidget.area = ImmutableRect2i.EMPTY;

            for (ConfigEntryWidget<?> entryWidget : activeWidget.getEntryWidgets()) {
                int ew = contentArea.getWidth() - 4;
                entryWidget.updateBounds(new ImmutableRect2i(contentArea.getX() + 2, currentY, ew, ENTRY_HEIGHT));
                int h = entryWidget.getHeight();
                entryWidget.updateBounds(new ImmutableRect2i(contentArea.getX() + 2, currentY, ew, h));
                currentY += h;
                totalContentHeight += h;
            }
        }
    }

    private void updateNavLayout() {
        int navY = navArea.getY() - (int) navCurrentScrollY;
        totalNavHeight = 0;
        for (NavItem navItem : navItems) {
            navItem.updateBounds(new ImmutableRect2i(
                    navArea.getX(), navY, navArea.getWidth(), navItem.cachedHeight
            ));
            navY += navItem.cachedHeight + NAV_ITEM_GAP;
            totalNavHeight += navItem.cachedHeight + NAV_ITEM_GAP;
		}
        if (!navItems.isEmpty()) totalNavHeight -= NAV_ITEM_GAP;
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
        if (forwardKeyPressedToEntries(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (searchBox.isFocused() && searchBox.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private boolean forwardCharTypedToEntries(char codePoint, int modifiers) {
        for (ConfigCategoryWidget widget : categoryWidgets) {
            for (ConfigEntryWidget<?> entry : widget.getEntryWidgets()) {
                if (entry.charTyped(codePoint, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean forwardKeyPressedToEntries(int keyCode, int scanCode, int modifiers) {
        for (ConfigCategoryWidget widget : categoryWidgets) {
            for (ConfigEntryWidget<?> entry : widget.getEntryWidgets()) {
                if (entry.keyPressed(keyCode, scanCode, modifiers)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isCategoryModified() {
        ConfigCategoryWidget activeWidget = categoryWidgets.get(activeNavIndex);
        return activeWidget.getEntryWidgets().stream().anyMatch(ConfigEntryWidget::isModified);
    }

    private void resetCurrentCategory() {
        ConfigCategoryWidget activeWidget = categoryWidgets.get(activeNavIndex);
        for (ConfigEntryWidget<?> entry : activeWidget.getEntryWidgets()) {
            entry.resetToDefault();
        }
        updateLayout();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && resetCategoryBtn.contains(mouseX, mouseY)) {
            if (!isCategoryModified()) {
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
        if (button == 0 && resetCategoryBtn.contains(mouseX, mouseY) && isCategoryModified()) {
            resetCurrentCategory();
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

	private boolean handleInput(UserInput input) {
		return this.inputHandler.handleUserInput(this, input, keyBindings);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (navArea.contains(mouseX, mouseY)) {
            int maxNavScroll = Math.max(0, totalNavHeight - navArea.getHeight());
            navTargetScrollY = Mth.clamp(navTargetScrollY - scrollY * SCROLL_SPEED, 0, maxNavScroll);
        } else if (contentArea.contains(mouseX, mouseY)) {
            int maxScroll = Math.max(0, totalContentHeight - contentArea.getHeight());
            targetScrollY = Mth.clamp(targetScrollY - scrollY * SCROLL_SPEED, 0, maxScroll);
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		if (minecraft == null) {
			return;
		}
		renderTransparentBackground(guiGraphics);

        if (Math.abs(targetScrollY - currentScrollY) > 0.5) {
            currentScrollY += (targetScrollY - currentScrollY) * SCROLL_LERP;
            updateLayout();
        } else if (currentScrollY != targetScrollY) {
            currentScrollY = targetScrollY;
            updateLayout();
        }
        if (Math.abs(navTargetScrollY - navCurrentScrollY) > 0.5) {
            navCurrentScrollY += (navTargetScrollY - navCurrentScrollY) * SCROLL_LERP;
            updateNavLayout();
        } else if (navCurrentScrollY != navTargetScrollY) {
            navCurrentScrollY = navTargetScrollY;
            updateNavLayout();
        }

        ConfigEntryWidget<?> hoveredEntryWidget = null;
        NavItem hoveredNavItem = null;

		guiGraphics.pose().pushPose();
		background.draw(guiGraphics, area);
        Font font = minecraft.font;

        Textures textures = Internal.getTextures();
        guiGraphics.fill(navArea.getX(), navArea.getY(), navArea.getX() + navArea.getWidth(), navArea.getY() + navArea.getHeight(), 0x18000000);

        guiGraphics.enableScissor(
                navArea.getX(), navArea.getY(),
                navArea.getX() + navArea.getWidth(),
                navArea.getY() + navArea.getHeight()
        );
        for (int i = 0; i < navItems.size(); i++) {
            NavItem navItem = navItems.get(i);
            navItem.draw(guiGraphics, mouseX, mouseY, !isSearching() && i == activeNavIndex);
            if (navArea.contains(mouseX, mouseY) && navItem.isMouseOver(mouseX, mouseY)) {
                hoveredNavItem = navItem;
            }
        }
        guiGraphics.disableScissor();

        int searchY = navArea.getY();
        int searchBgX = contentArea.getX();
        int searchBgW = contentArea.getWidth() - resetCategoryBtn.getWidth() - 3;
        textures.getSearchBackground().draw(guiGraphics, new ImmutableRect2i(searchBgX, searchY, searchBgW, SEARCH_HEIGHT));
        searchBox.render(guiGraphics, mouseX, mouseY, partialTick);

        boolean categoryModified = isCategoryModified();
        boolean resetHov = categoryModified && resetCategoryBtn.contains(mouseX, mouseY);
        textures.getButtonForState(false, categoryModified, resetHov).draw(guiGraphics, resetCategoryBtn);
        String resetLbl = "Reset";
        int rtx = resetCategoryBtn.getX() + (resetCategoryBtn.getWidth() - font.width(resetLbl)) / 2;
        int rty = resetCategoryBtn.getY() + (resetCategoryBtn.getHeight() - font.lineHeight) / 2;
        guiGraphics.drawString(font, resetLbl, rtx, rty, resetHov ? 0xFFFFFF55 : 0xFFFFFFFF, false);

        guiGraphics.enableScissor(
                contentArea.getX(), contentArea.getY(),
                contentArea.getX() + contentArea.getWidth(),
                contentArea.getY() + contentArea.getHeight()
        );

        if (isSearching()) {
            for (ConfigCategoryWidget widget : categoryWidgets) {
                for (ConfigEntryWidget<?> entryWidget : widget.getEntryWidgets()) {
                    if (!matchesSearch(entryWidget)) {
                        continue;
                    }
					entryWidget.draw(guiGraphics, mouseX, mouseY);
                    if (contentArea.contains(mouseX, mouseY) && entryWidget.nameArea.contains(mouseX, mouseY)) {
                        hoveredEntryWidget = entryWidget;
                    }
                }
            }
        } else {
            ConfigCategoryWidget activeWidget = categoryWidgets.get(activeNavIndex);
            for (ConfigEntryWidget<?> entryWidget : activeWidget.getEntryWidgets()) {
                entryWidget.draw(guiGraphics, mouseX, mouseY);
                if (contentArea.contains(mouseX, mouseY) && entryWidget.nameArea.contains(mouseX, mouseY)) {
                    hoveredEntryWidget = entryWidget;
				}
			}
		}
		guiGraphics.disableScissor();
		guiGraphics.pose().popPose();

		renderScrollBar(guiGraphics);

		if (valueSelector != null) {
			guiGraphics.pose().pushPose();
			guiGraphics.pose().translate(0, 0, 350);
			valueSelector.draw(guiGraphics, mouseX, mouseY);
			guiGraphics.pose().popPose();
		}

        if (hoveredNavItem != null) {
            hoveredNavItem.drawTooltip(guiGraphics, mouseX, mouseY);
		}
		if (hoveredEntryWidget != null) {
			hoveredEntryWidget.drawTooltip(guiGraphics, mouseX, mouseY);
		}
	}

	private void renderScrollBar(GuiGraphics guiGraphics) {
        int maxScroll = Math.max(0, totalContentHeight - contentArea.getHeight());
        if (maxScroll <= 0) {
            return;
        }
		scrollbarBackground.draw(guiGraphics, scrollBarArea);
        int trackHeight = scrollBarArea.getHeight();
        int markerHeight = Math.max(10, trackHeight * contentArea.getHeight() / totalContentHeight);
        int markerY = scrollBarArea.getY() + (int) ((trackHeight - markerHeight) * currentScrollY / maxScroll);
        scrollbarMarker.draw(guiGraphics, new ImmutableRect2i(
                scrollBarArea.getX() + 1,
                markerY,
                scrollBarArea.getWidth() - 2,
                markerHeight
        ));
	}

    private class NavItem implements IUserInputHandler {
        private final Component displayName;
        private final int categoryIndex;
        private ImmutableRect2i area = ImmutableRect2i.EMPTY;
        private List<FormattedCharSequence> wrappedLines = List.of();
        private int cachedHeight = NAV_ITEM_HEIGHT;

        NavItem(Component displayName, int categoryIndex) {
            this.displayName = displayName;
            this.categoryIndex = categoryIndex;
        }

        int calculateHeight(int availableWidth) {
            Font font = Minecraft.getInstance().font;
            int textWidth = (int) ((availableWidth - 6) / ConfigEntryWidget.TEXT_SCALE);
            wrappedLines = font.split(displayName, textWidth);
            int scaledLineHeight = (int) (font.lineHeight * ConfigEntryWidget.TEXT_SCALE);
            int textHeight = wrappedLines.size() * scaledLineHeight;
            cachedHeight = Math.max(NAV_ITEM_HEIGHT, textHeight + 4);
            return cachedHeight;
		}

		void updateBounds(ImmutableRect2i area) {
			this.area = area;
		}

		boolean isMouseOver(double mouseX, double mouseY) {
			return area.contains(mouseX, mouseY);
		}

        void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean active) {
            Font font = Minecraft.getInstance().font;
            boolean hovered = isMouseOver(mouseX, mouseY) && navArea.contains(mouseX, mouseY);

            Textures textures = Internal.getTextures();
            textures.getConfigCategoryButton().draw(guiGraphics, area);
            if (active || hovered) {
                textures.getConfigCategoryHighlight().draw(guiGraphics, area);
            }
            if (active) {
                guiGraphics.fill(area.getX(), area.getY(), area.getX() + 2, area.getY() + area.getHeight(), 0xCCFFFFFF);
            }

            int textColor = 0xFFFFFFFF;
            int textX = area.getX() + (active ? 8 : 6);
            int scaledLineHeight = (int) (font.lineHeight * ConfigEntryWidget.TEXT_SCALE);
            int totalTextHeight = wrappedLines.size() * scaledLineHeight;
            int textY = area.getY() + (area.getHeight() - totalTextHeight) / 2 + 2;
            for (FormattedCharSequence line : wrappedLines) {
                ConfigEntryWidget.drawScaledString(guiGraphics, font, line, textX, textY, textColor, false);
                textY += scaledLineHeight;
            }
        }

        void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            categoryWidgets.get(categoryIndex).drawTooltip(guiGraphics, mouseX, mouseY);
        }

        @Override
        public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
            if (navArea.contains(input.getMouseX(), input.getMouseY())
                    && area.contains(input.getMouseX(), input.getMouseY())
                    && input.is(keyBindings.getLeftClick())) {
                if (!input.isSimulate()) {
                    setActiveCategory(categoryIndex);
                }
                return Optional.of(this);
            }
            return Optional.empty();
        }
    }

    private class ValueSelectorInputHandler implements IUserInputHandler {
        @Override
        public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
            if (valueSelector != null) {
                if (valueSelector.isMouseOver(input.getMouseX(), input.getMouseY()) && input.is(keyBindings.getLeftClick())) {
                    if (valueSelector.onMouseClicked(input)) {
						if (!input.isSimulate()) {
                            valueSelector = null;
                            updateLayout();
						}
						return Optional.of(this);
					}
                } else if (input.is(keyBindings.getLeftClick())) {
                    if (!input.isSimulate()) {
						valueSelector = null;
					}
                    return Optional.of(this);
                }
			}
            return Optional.empty();
		}
	}
}
