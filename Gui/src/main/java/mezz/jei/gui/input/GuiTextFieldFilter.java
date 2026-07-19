package mezz.jei.gui.input;

import mezz.jei.common.Internal;
import mezz.jei.common.config.IClientConfig;
import mezz.jei.common.gui.JeiGuiColors;
import mezz.jei.common.gui.JeiGuiColors.GuiColor;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.TextHistory;
import mezz.jei.gui.input.focus.ScreenFocusHandler;
import mezz.jei.gui.input.handlers.TextFieldInputHandler;
import mezz.jei.gui.overlay.ISearchField;
import mezz.jei.gui.search.ISearchCompletionProvider;
import mezz.jei.gui.search.SearchCompletionPopup;
import mezz.jei.gui.search.SearchSyntaxHighlighter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class GuiTextFieldFilter extends EditBox implements ISearchField {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();

	private final BooleanSupplier filterEmpty;
	private final IClientConfig clientConfig;

	private ImmutableRect2i area;
	private final ScalableDrawable background;
	private ImmutableRect2i backgroundBounds;

	private @Nullable ScreenFocusHandler screenUnfocusHandler;

	private final SearchCompletionPopup completionPopup;

	public GuiTextFieldFilter(BooleanSupplier filterEmpty, IClientConfig clientConfig, ISearchCompletionProvider searchCompletionProvider) {
		super(Minecraft.getInstance().font, 0, 0, 0, 0, Component.translatable("gui.jei.search"));
		this.filterEmpty = filterEmpty;
		this.clientConfig = clientConfig;

		setMaxLength(maxSearchLength);
		this.area = ImmutableRect2i.EMPTY;
		Textures textures = Internal.getTextures();
		this.background = textures.getSearchBackground();
		this.completionPopup = new SearchCompletionPopup(searchCompletionProvider, textures.getCompletionPopupBackground());
		this.backgroundBounds = ImmutableRect2i.EMPTY;
		setBordered(false);
		addFormatter(new SearchSyntaxHighlighter(filterEmpty, this::getValue, searchCompletionProvider)::format);
	}

	@Override
	public void updateBounds(ImmutableRect2i area) {
		this.backgroundBounds = area;
		setX(area.getX() + 4);
		setY(area.getY() + (area.getHeight() - 8) / 2);
		this.width = area.getWidth() - 12;
		this.height = area.getHeight();
		this.area = area;
		this.completionPopup.updateBounds(area);
	}

	@Override
	public void setValue(String filterText) {
		if (!filterText.equals(getValue())) {
			super.setValue(filterText);
		}
		int color = JeiGuiColors.getColor(GuiColor.SEARCH_FIELD_TEXT);
		if (filterEmpty.getAsBoolean()) {
			color = JeiGuiColors.getColor(GuiColor.SEARCH_FIELD_ERROR_TEXT);
		}
		setTextColor(color);
	}

	public Optional<String> getHistory(TextHistory.Direction direction) {
		String currentText = getValue();
		return history.get(direction, currentText);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public IUserInputHandler createInputHandler() {
		return new TextFieldInputHandler(this);
	}

	@Override
	public void setFocused(boolean keyboardFocus) {
		final boolean previousFocus = isFocused();
		super.setFocused(keyboardFocus);

		if (previousFocus != keyboardFocus) {
			Minecraft minecraft = Minecraft.getInstance();
			if (keyboardFocus) {
				Screen screen = minecraft.gui.screen();
				if (screen != null) {
					screenUnfocusHandler = ScreenFocusHandler.create(screen);
					if (screenUnfocusHandler != null) {
						screenUnfocusHandler.unFocus();
					}
				}
			} else {
				if (screenUnfocusHandler != null) {
					screenUnfocusHandler.focus();
					screenUnfocusHandler = null;
				}
			}

			String text = getValue();
			history.add(text);
		}
	}

	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		extractBackgroundRenderState(guiGraphics);
		extractForegroundRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	public void extractBackgroundRenderState(GuiGraphicsExtractor guiGraphics) {
		if (this.isVisible()) {
			background.draw(guiGraphics, this.backgroundBounds);
		}
	}

	public void extractForegroundRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
		if (clientConfig.isSearchCompletionEnabled() && isFocused()) {
			completionPopup.render(guiGraphics, getValue(), getCursorPosition(), mouseX, mouseY);
		} else {
			completionPopup.close();
		}
	}

	public boolean isCompletionVisible() {
		return isFocused() && completionPopup.isVisible();
	}

	public void moveCompletion(int delta) {
		completionPopup.moveSelection(delta);
	}

	public void acceptCompletion() {
		completionPopup.accept(this);
	}

	public void closeCompletion() {
		completionPopup.close();
	}

	public boolean handleCompletionClick(double mouseX, double mouseY) {
		return completionPopup.handleMouseClicked(mouseX, mouseY, this);
	}

	public boolean isCompletionMouseOver(double mouseX, double mouseY) {
		return completionPopup.isMouseOver(mouseX, mouseY);
	}
}
