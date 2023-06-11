package mezz.jei.gui.input;

import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.common.gui.elements.DrawableNineSliceTexture;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.core.util.TextHistory;
import mezz.jei.gui.input.handlers.TextFieldInputHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class GuiTextFieldFilter extends EditBox {
	private static final int maxSearchLength = 128;
	private static final TextHistory history = new TextHistory();
	private final BooleanSupplier filterEmpty;

	private ImmutableRect2i area;
	private final DrawableNineSliceTexture background;
	private ImmutableRect2i backgroundBounds;

	private @Nullable AbstractWidget previouslyFocusedWidget;

	public GuiTextFieldFilter(Textures textures, BooleanSupplier filterEmpty) {
		// TODO narrator string
		super(Minecraft.getInstance().font, 0, 0, 0, 0, CommonComponents.EMPTY);
		this.filterEmpty = filterEmpty;

		setMaxLength(maxSearchLength);
		this.area = ImmutableRect2i.EMPTY;
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
					if (screen.getFocused() instanceof AbstractWidget widget) {
						IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
						screenHelper.setFocused(widget, false);
						previouslyFocusedWidget = widget;
					}
					screen.setFocused(null);
				}
			} else {
				if (previouslyFocusedWidget != null) {
					Screen screen = minecraft.screen;
					if (screen != null) {
						IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
						screenHelper.setFocused(previouslyFocusedWidget, true);
						screen.setFocused(previouslyFocusedWidget);
					}
					previouslyFocusedWidget = null;
				}
			}

			String text = getValue();
			history.add(text);
		}
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		if (this.isVisible()) {
			RenderSystem.setShaderColor(1, 1, 1, 1);
			background.draw(guiGraphics, this.backgroundBounds);
		}
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
	}
}
