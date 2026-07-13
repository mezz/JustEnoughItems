package mezz.jei.gui.config.screen;

import mezz.jei.common.Internal;
import mezz.jei.common.gui.JeiTooltip;
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
	private static final int VALUE_AREA_BACKGROUND_COLOR = 0x82000000;
	private static final int VALUE_AREA_BORDER_COLOR = 0x35FFFFFF;

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
		@Nullable ConfigInfo hoveredValueSelectorInfo = getValueSelectorInfo(valueSelector, mouseX, mouseY);
		@Nullable ConfigInfo tooltipInfo = getTooltipInfo(valueSelector, contentArea, mouseX, mouseY);
		@Nullable ConfigInfo hoveredControlInfo = getControlInfo(searchBackgroundArea, resetCategoryButtonArea, applyButtonArea, mouseX, mouseY);

		guiGraphics.pose().pushPose();
		background.draw(guiGraphics, area);
		drawNavBackground(guiGraphics, navArea);
		@Nullable ConfigNavItem hoveredNavItem = drawNavItems(guiGraphics, navArea, mouseX, mouseY);
		drawNavScrollBar(guiGraphics);
		drawSearch(guiGraphics, textures, searchBackgroundArea, mouseX, mouseY, partialTick);
		drawResetButton(guiGraphics, font, textures, resetCategoryButtonArea, mouseX, mouseY);
		drawApplyButton(guiGraphics, font, textures, applyButtonArea, mouseX, mouseY);
		drawValueAreaBackground(guiGraphics, contentArea);
		@Nullable ConfigInfo hoveredEntryInfo = drawEntries(guiGraphics, contentArea, mouseX, mouseY, valueSelector == null);
		drawInfoPanel(guiGraphics, font, getInfo(hoveredValueSelectorInfo, hoveredControlInfo, hoveredNavItem, hoveredEntryInfo));
		guiGraphics.pose().popPose();

		drawContentScrollBar(guiGraphics);
		drawValueSelector(guiGraphics, valueSelector, mouseX, mouseY);
		drawTooltip(guiGraphics, mouseX, mouseY, tooltipInfo);
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

	private static void drawValueAreaBackground(GuiGraphics guiGraphics, ImmutableRect2i contentArea) {
		guiGraphics.fill(
			contentArea.getX(),
			contentArea.getY(),
			contentArea.getX() + contentArea.getWidth(),
			contentArea.getY() + contentArea.getHeight(),
			VALUE_AREA_BACKGROUND_COLOR
		);
		guiGraphics.fill(
			contentArea.getX(),
			contentArea.getY(),
			contentArea.getX() + contentArea.getWidth(),
			contentArea.getY() + 1,
			VALUE_AREA_BORDER_COLOR
		);
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
		ConfigEntryWidget.drawButtonBackground(guiGraphics, textures, buttonArea, active, hovered);
		int textColor = active ? (hovered ? ConfigEntryWidget.HOVER_TEXT_COLOR : ConfigEntryWidget.TEXT_COLOR) : ConfigEntryWidget.DISABLED_TEXT_COLOR;
		ConfigEntryWidget.drawCenteredButtonText(guiGraphics, font, label, buttonArea, textColor);
	}

	@Nullable
	private ConfigInfo drawEntries(
		GuiGraphics guiGraphics,
		ImmutableRect2i contentArea,
		int mouseX,
		int mouseY,
		boolean allowEntryHover
	) {
		@Nullable ConfigInfo hoveredEntryInfo = null;
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
			entryWidget.draw(guiGraphics, mouseX, mouseY, allowEntryHover);
			if (allowEntryHover && contentArea.contains(mouseX, mouseY) && entryWidget.isMouseOver(mouseX, mouseY)) {
				hoveredEntryInfo = entryWidget.getInfo(mouseX, mouseY);
			}
		}
		guiGraphics.disableScissor();
		return hoveredEntryInfo;
	}

	@Nullable
	private ConfigInfo getValueSelectorInfo(@Nullable ConfigValueSelector<?> valueSelector, int mouseX, int mouseY) {
		if (valueSelector != null && valueSelector.isMouseOver(mouseX, mouseY)) {
			return valueSelector.getInfo();
		}
		return null;
	}

	@Nullable
	private ConfigInfo getTooltipInfo(
		@Nullable ConfigValueSelector<?> valueSelector,
		ImmutableRect2i contentArea,
		int mouseX,
		int mouseY
	) {
		if (valueSelector != null) {
			return valueSelector.getTooltipInfo(mouseX, mouseY);
		}
		if (!contentArea.contains(mouseX, mouseY)) {
			return null;
		}
		for (ConfigEntryWidget<?> entryWidget : controller.getVisibleEntryWidgets()) {
			if (entryWidget.area.equals(ImmutableRect2i.EMPTY) || !entryWidget.isMouseOver(mouseX, mouseY)) {
				continue;
			}
			@Nullable ConfigInfo info = entryWidget.getTooltipInfo(mouseX, mouseY);
			if (info != null) {
				return info;
			}
		}
		return null;
	}

	private void drawNavScrollBar(GuiGraphics guiGraphics) {
		drawScrollBar(guiGraphics, layout.getNavScrollBarArea(), layout.getNavScrollMarkerArea());
	}

	private void drawContentScrollBar(GuiGraphics guiGraphics) {
		drawScrollBar(guiGraphics, layout.getScrollBarArea(), layout.getScrollMarkerArea());
	}

	private void drawScrollBar(GuiGraphics guiGraphics, ImmutableRect2i scrollBarArea, ImmutableRect2i scrollMarkerArea) {
		if (!scrollMarkerArea.isEmpty()) {
			scrollbarBackground.draw(guiGraphics, scrollBarArea);
			scrollbarMarker.draw(guiGraphics, scrollMarkerArea);
		}
	}

	@Nullable
	private ConfigInfo getInfo(
		@Nullable ConfigInfo hoveredValueSelectorInfo,
		@Nullable ConfigInfo hoveredControlInfo,
		@Nullable ConfigNavItem hoveredNavItem,
		@Nullable ConfigInfo hoveredEntryInfo
	) {
		if (hoveredValueSelectorInfo != null) {
			return hoveredValueSelectorInfo;
		}
		if (hoveredControlInfo != null) {
			return hoveredControlInfo;
		}
		if (hoveredEntryInfo != null) {
			return hoveredEntryInfo;
		}
		if (hoveredNavItem != null) {
			return hoveredNavItem.getInfo();
		}
		if (model.hasActiveCategory()) {
			return model.getActiveCategoryWidget().getInfo();
		}
		return null;
	}

	@Nullable
	private ConfigInfo getControlInfo(
		ImmutableRect2i searchBackgroundArea,
		ImmutableRect2i resetCategoryButtonArea,
		ImmutableRect2i applyButtonArea,
		int mouseX,
		int mouseY
	) {
		if (searchBackgroundArea.contains(mouseX, mouseY)) {
			return new ConfigInfo(
				Component.translatable("jei.config.screen.search.info.title"),
				Component.translatable("jei.config.screen.search.info")
			);
		}
		if (resetCategoryButtonArea.contains(mouseX, mouseY)) {
			return new ConfigInfo(
				Component.translatable("jei.config.screen.reset"),
				Component.translatable("jei.config.screen.reset.visible.info")
			);
		}
		if (applyButtonArea.contains(mouseX, mouseY)) {
			return new ConfigInfo(
				Component.translatable("jei.config.screen.apply"),
				Component.translatable("jei.config.screen.apply.info")
			);
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

	private static void drawTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, @Nullable ConfigInfo info) {
		if (info == null) {
			return;
		}
		JeiTooltip tooltip = new JeiTooltip();
		tooltip.add(info.title());
		for (Component line : info.lines()) {
			tooltip.add(line);
		}
		tooltip.draw(guiGraphics, mouseX, mouseY);
	}
}
