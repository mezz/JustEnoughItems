package mezz.jei.gui.config.screen;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.StringUtil;
import mezz.jei.gui.input.IUserInputHandler;
import mezz.jei.gui.input.UserInput;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

final class ConfigNavItem implements IUserInputHandler {
	private static final int TEXT_LEFT_PADDING = 6;
	private static final int ACTIVE_TEXT_LEFT_PADDING = 8;
	private static final int TEXT_RIGHT_PADDING = 6;

	private final Component fullName;
	private final int categoryIndex;
	private final ConfigCategoryWidget categoryWidget;
	private final Supplier<ImmutableRect2i> navAreaSupplier;
	private final IntConsumer categorySelector;

	private ImmutableRect2i area = ImmutableRect2i.EMPTY;
	private FormattedCharSequence visibleName = FormattedCharSequence.EMPTY;
	private int cachedHeight = ConfigScreenLayout.NAV_ITEM_HEIGHT;

	public ConfigNavItem(
		Component displayName,
		int categoryIndex,
		ConfigCategoryWidget categoryWidget,
		Supplier<ImmutableRect2i> navAreaSupplier,
		IntConsumer categorySelector
	) {
		this.fullName = StringUtil.stripStyling(displayName);
		this.categoryIndex = categoryIndex;
		this.categoryWidget = categoryWidget;
		this.navAreaSupplier = navAreaSupplier;
		this.categorySelector = categorySelector;
	}

	public int calculateHeight(int availableWidth) {
		Font font = Minecraft.getInstance().font;
		int textWidth = Math.max(0, availableWidth - ACTIVE_TEXT_LEFT_PADDING - TEXT_RIGHT_PADDING);
		if (font.width(fullName) > textWidth) {
			FormattedText formattedText = StringUtil.truncateStringToWidth(fullName, textWidth, font);
			visibleName = Language.getInstance().getVisualOrder(formattedText);
		} else {
			visibleName = fullName.getVisualOrderText();
		}
		cachedHeight = ConfigScreenLayout.NAV_ITEM_HEIGHT;
		return cachedHeight;
	}

	public int getCachedHeight() {
		return cachedHeight;
	}

	public void updateBounds(ImmutableRect2i area) {
		this.area = area;
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return area.contains(mouseX, mouseY);
	}

	public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, boolean active) {
		Font font = Minecraft.getInstance().font;
		ImmutableRect2i navArea = navAreaSupplier.get();
		boolean hovered = isMouseOver(mouseX, mouseY) && navArea.contains(mouseX, mouseY);

		Textures textures = Internal.getTextures();
		textures.getConfigCategoryButton().draw(guiGraphics, area);
		if (active || hovered) {
			textures.getConfigCategoryHighlight().draw(guiGraphics, area);
		}
		if (active) {
			guiGraphics.fill(area.getX(), area.getY(), area.getX() + 2, area.getY() + area.getHeight(), 0xCC2B3442);
		}

		int textColor = active || hovered ? ConfigEntryWidget.HOVER_TEXT_COLOR : ConfigEntryWidget.TEXT_COLOR;
		int textX = area.getX() + (active ? ACTIVE_TEXT_LEFT_PADDING : TEXT_LEFT_PADDING);
		int textY = ConfigEntryWidget.getCenteredTextY(font, area);
		guiGraphics.drawString(font, visibleName, textX, textY, textColor, false);
	}

	public ConfigInfo getInfo() {
		return categoryWidget.getInfo();
	}

	@Override
	public Optional<IUserInputHandler> handleUserInput(Screen screen, UserInput input, IInternalKeyMappings keyBindings) {
		ImmutableRect2i navArea = navAreaSupplier.get();
		if (navArea.contains(input.getMouseX(), input.getMouseY())
			&& area.contains(input.getMouseX(), input.getMouseY())
			&& input.is(keyBindings.getLeftClick())) {
			if (!input.isSimulate()) {
				categorySelector.accept(categoryIndex);
			}
			return Optional.of(this);
		}
		return Optional.empty();
	}
}
