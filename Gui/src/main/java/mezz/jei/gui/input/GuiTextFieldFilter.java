package mezz.jei.gui.input;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.util.TextHistory;
import mezz.jei.gui.input.focus.ScreenFocusHandler;
import mezz.jei.gui.input.handlers.TextFieldInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class GuiTextFieldFilter extends EditBox {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();
	private final BooleanSupplier filterEmpty;

	private ImmutableRect2i area;
	private final ScalableDrawable background;
	private ImmutableRect2i backgroundBounds;

	private @Nullable ScreenFocusHandler screenUnfocusHandler;

	public GuiTextFieldFilter(BooleanSupplier filterEmpty) {
		// TODO narrator string
		super(Minecraft.getInstance().font, 0, 0, 0, 0, CommonComponents.EMPTY);
		this.filterEmpty = filterEmpty;

		setMaxLength(maxSearchLength);
		this.area = ImmutableRect2i.EMPTY;
		Textures textures = Internal.getTextures();
		this.background = textures.getSearchBackground();
		this.backgroundBounds = ImmutableRect2i.EMPTY;
		setBordered(false);
	}

	public void updateBounds(ImmutableRect2i area) {
		this.backgroundBounds = area;
		setX(area.getX() + 4);
		setY(area.getY() + (area.getHeight() - 8) / 2);
		this.width = area.getWidth() - 12;
		this.height = area.getHeight();
		this.area = area;
	}

	@Override
	public void setValue(String filterText) {
		if (!filterText.equals(getValue())) {
			super.setValue(filterText);
		}
		int color = filterEmpty.getAsBoolean() ? 0xFFFF0000 : 0xFFFFFFFF;
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
				Screen screen = minecraft.screen;
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
		if (this.isVisible()) {
			background.draw(guiGraphics, this.backgroundBounds);
		}
		super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}
}
