package mezz.jei.gui.config.screen;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.elements.ScalableDrawable;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class ConfigScreenView {
	private static final int VALUE_SELECTOR_Z_OFFSET = 350;
	private static final int INFO_PADDING = 5;
	private static final int INFO_BACKGROUND_COLOR = 0xE0101218;
	private static final int INFO_BORDER_COLOR = 0x70FFFFFF;
	private static final int INFO_TITLE_COLOR = 0xFFF3F6FF;
	private static final int INFO_TEXT_COLOR = 0xFFC9D3E2;

	private final EditBox searchBox;
	private final ConfigScreenModel model;
	private final ConfigScreenLayout layout;
	private final ConfigScreenController controller;
	private final ScalableDrawable background;
	private final ScalableDrawable scrollbarMarker;
	private final ScalableDrawable scrollbarBackground;

	ConfigScreenView(
		EditBox searchBox,
		ConfigScreenModel model,
		ConfigScreenLayout layout,
		ConfigScreenController controller
	) {
		Textures textures = Internal.getTextures();
		this.searchBox = searchBox;
		this.model = model;
		this.layout = layout;
		this.controller = controller;
		this.background = textures.getRecipeGuiBackground();
		this.scrollbarMarker = textures.getScrollbarMarker();
		this.scrollbarBackground = textures.getScrollbarBackground();
	}

	void render(
		GuiGraphics guiGraphics,
		int mouseX,
		int mouseY,
		float partialTick,
		@Nullable ConfigValueSelector<?> valueSelector
	) {
		Font font = Minecraft.getInstance().font;
		Textures textures = Internal.getTextures();
		ImmutableRect2i area = layout.getArea();
		ImmutableRect2i navArea = layout.getNavArea();
		ImmutableRect2i contentArea = layout.getContentArea();
		ImmutableRect2i searchBackgroundArea = layout.getSearchBackgroundArea();
		ImmutableRect2i resetCategoryButtonArea = layout.getResetCategoryButtonArea();
		ImmutableRect2i applyButtonArea = layout.getApplyButtonArea();

		guiGraphics.pose().pushPose();
		background.draw(guiGraphics, area);
		drawNavBackground(guiGraphics, navArea);
		@Nullable ConfigNavItem hoveredNavItem = drawNavItems(guiGraphics, navArea, mouseX, mouseY);
		drawSearch(guiGraphics, textures, searchBackgroundArea, mouseX, mouseY, partialTick);
		drawResetButton(guiGraphics, font, textures, resetCategoryButtonArea, mouseX, mouseY);
		drawApplyButton(guiGraphics, font, textures, applyButtonArea, mouseX, mouseY);
		@Nullable ConfigEntryWidget<?> hoveredEntryWidget = drawEntries(guiGraphics, contentArea, mouseX, mouseY);
		drawInfoPanel(guiGraphics, font, getInfo(hoveredNavItem, hoveredEntryWidget));
		guiGraphics.pose().popPose();

		drawScrollBar(guiGraphics);
		drawValueSelector(guiGraphics, valueSelector, mouseX, mouseY);
	}

	private static void drawNavBackground(GuiGraphics guiGraphics, ImmutableRect2i navArea) {
		guiGraphics.fill(
			navArea.getX(),
			navArea.getY(),
			navArea.getX() + navArea.getWidth(),
			navArea.getY() + navArea.getHeight(),
			0x18000000
		);
	}

	@Nullable
	private ConfigNavItem drawNavItems(GuiGraphics guiGraphics, ImmutableRect2i navArea, int mouseX, int mouseY) {
		@Nullable ConfigNavItem hoveredNavItem = null;
		guiGraphics.enableScissor(
			navArea.getX(),
			navArea.getY(),
			navArea.getX() + navArea.getWidth(),
			navArea.getY() + navArea.getHeight()
		);
		List<ConfigNavItem> navItems = model.getNavItems();
		for (int i = 0; i < navItems.size(); i++) {
			ConfigNavItem navItem = navItems.get(i);
			navItem.draw(guiGraphics, mouseX, mouseY, !model.isSearching() && i == model.getActiveCategoryIndex());
			if (navArea.contains(mouseX, mouseY) && navItem.isMouseOver(mouseX, mouseY)) {
				hoveredNavItem = navItem;
			}
		}
		guiGraphics.disableScissor();
		return hoveredNavItem;
	}

	private void drawSearch(
		GuiGraphics guiGraphics,
		Textures textures,
		ImmutableRect2i searchBackgroundArea,
		int mouseX,
		int mouseY,
		float partialTick
	) {
		textures.getSearchBackground()
			.draw(guiGraphics, searchBackgroundArea);
		searchBox.render(guiGraphics, mouseX, mouseY, partialTick);
	}

	private void drawResetButton(
		GuiGraphics guiGraphics,
		Font font,
		Textures textures,
		ImmutableRect2i resetCategoryButtonArea,
		int mouseX,
		int mouseY
	) {
		Component resetLabel = Component.translatable("jei.config.screen.reset");
		drawButton(guiGraphics, font, textures, resetCategoryButtonArea, mouseX, mouseY, controller.hasResettableEntries(), resetLabel);
	}

	private void drawApplyButton(
		GuiGraphics guiGraphics,
		Font font,
		Textures textures,
		ImmutableRect2i applyButtonArea,
		int mouseX,
		int mouseY
	) {
		Component applyLabel = Component.translatable("jei.config.screen.apply");
		drawButton(guiGraphics, font, textures, applyButtonArea, mouseX, mouseY, controller.hasPendingChanges(), applyLabel);
	}

	private static void drawButton(
		GuiGraphics guiGraphics,
		Font font,
		Textures textures,
		ImmutableRect2i buttonArea,
		int mouseX,
		int mouseY,
		boolean active,
		Component label
	) {
		boolean hovered = active && buttonArea.contains(mouseX, mouseY);
		textures.getButtonForState(false, active, hovered).draw(guiGraphics, buttonArea);
		ConfigEntryWidget.drawCenteredButtonText(guiGraphics, font, label, buttonArea, hovered ? ConfigEntryWidget.HOVER_TEXT_COLOR : ConfigEntryWidget.TEXT_COLOR);
	}

	@Nullable
	private ConfigEntryWidget<?> drawEntries(GuiGraphics guiGraphics, ImmutableRect2i contentArea, int mouseX, int mouseY) {
		@Nullable ConfigEntryWidget<?> hoveredEntryWidget = null;
		guiGraphics.enableScissor(
			contentArea.getX(),
			contentArea.getY(),
			contentArea.getX() + contentArea.getWidth(),
			contentArea.getY() + contentArea.getHeight()
		);
		for (ConfigEntryWidget<?> entryWidget : controller.getVisibleEntryWidgets()) {
			if (entryWidget.area.equals(ImmutableRect2i.EMPTY)) {
				continue;
			}
			entryWidget.draw(guiGraphics, mouseX, mouseY);
			if (contentArea.contains(mouseX, mouseY) && entryWidget.isMouseOver(mouseX, mouseY)) {
				hoveredEntryWidget = entryWidget;
			}
		}
		guiGraphics.disableScissor();
		return hoveredEntryWidget;
	}

	private void drawScrollBar(GuiGraphics guiGraphics) {
		ImmutableRect2i scrollMarkerArea = layout.getScrollMarkerArea();
		if (scrollMarkerArea.isEmpty()) {
			return;
		}
		ImmutableRect2i scrollBarArea = layout.getScrollBarArea();
		scrollbarBackground.draw(guiGraphics, scrollBarArea);
		scrollbarMarker.draw(guiGraphics, scrollMarkerArea);
	}

	@Nullable
	private ConfigInfo getInfo(
		@Nullable ConfigNavItem hoveredNavItem,
		@Nullable ConfigEntryWidget<?> hoveredEntryWidget
	) {
		if (hoveredEntryWidget != null) {
			return hoveredEntryWidget.getInfo();
		}
		if (hoveredNavItem != null) {
			return hoveredNavItem.getInfo();
		}
		if (model.hasActiveCategory()) {
			return model.getActiveCategoryWidget().getInfo();
		}
		return null;
	}

	private void drawInfoPanel(GuiGraphics guiGraphics, Font font, @Nullable ConfigInfo info) {
		ImmutableRect2i infoArea = layout.getInfoArea();
		guiGraphics.fill(
			infoArea.getX(),
			infoArea.getY(),
			infoArea.getX() + infoArea.getWidth(),
			infoArea.getY() + infoArea.getHeight(),
			INFO_BACKGROUND_COLOR
		);
		guiGraphics.fill(
			infoArea.getX(),
			infoArea.getY(),
			infoArea.getX() + infoArea.getWidth(),
			infoArea.getY() + 1,
			INFO_BORDER_COLOR
		);
		if (info == null) {
			return;
		}

		int textX = infoArea.getX() + INFO_PADDING;
		int textY = infoArea.getY() + INFO_PADDING;
		int maxTextY = infoArea.getY() + infoArea.getHeight() - INFO_PADDING;
		int textWidth = infoArea.getWidth() - INFO_PADDING * 2;

		guiGraphics.enableScissor(
			infoArea.getX(),
			infoArea.getY(),
			infoArea.getX() + infoArea.getWidth(),
			infoArea.getY() + infoArea.getHeight()
		);

		List<FormattedCharSequence> titleLines = font.split(info.title(), textWidth);
		if (!titleLines.isEmpty()) {
			guiGraphics.drawString(font, titleLines.getFirst(), textX, textY, INFO_TITLE_COLOR, false);
			textY += font.lineHeight + 2;
		}

		for (Component line : info.lines()) {
			for (FormattedCharSequence wrappedLine : font.split(line, textWidth)) {
				if (textY + font.lineHeight > maxTextY) {
					guiGraphics.disableScissor();
					return;
				}
				guiGraphics.drawString(font, wrappedLine, textX, textY, INFO_TEXT_COLOR, false);
				textY += font.lineHeight;
			}
		}

		guiGraphics.disableScissor();
	}

	private static void drawValueSelector(
		GuiGraphics guiGraphics,
		@Nullable ConfigValueSelector<?> valueSelector,
		int mouseX,
		int mouseY
	) {
		if (valueSelector == null) {
			return;
		}
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, VALUE_SELECTOR_Z_OFFSET);
		valueSelector.draw(guiGraphics, mouseX, mouseY);
		guiGraphics.pose().popPose();
	}
}
