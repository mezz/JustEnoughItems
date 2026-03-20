package mezz.jei.gui.events;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiProperties;
import mezz.jei.api.runtime.IScreenHelper;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.util.ImmutableRect2i;
import mezz.jei.common.util.RectDebugger;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.BookmarkOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.stream.Collectors;

public class GuiEventHandler {
	private final IngredientListOverlay ingredientListOverlay;
	private final IScreenHelper screenHelper;
	private final BookmarkOverlay bookmarkOverlay;

	public GuiEventHandler(
		IScreenHelper screenHelper,
		BookmarkOverlay bookmarkOverlay,
		IngredientListOverlay ingredientListOverlay
	) {
		this.screenHelper = screenHelper;
		this.bookmarkOverlay = bookmarkOverlay;
		this.ingredientListOverlay = ingredientListOverlay;
	}

	public void onGuiInit(Screen screen) {
		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
			.map(ImmutableRect2i::new)
			.collect(Collectors.toUnmodifiableSet());
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
	}

	public void onGuiOpen(Screen screen) {
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(screen)
			.update();
	}

	/**
	 * Draws above most ContainerScreen elements, but below the tooltips.
	 */
	public void drawForContainerScreen(AbstractContainerScreen<?> screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		var poseStack = guiGraphics.pose();
		poseStack.pushMatrix();
		{
			@Nullable IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
			if (guiProperties != null) {
				poseStack.translate(-guiProperties.guiLeft(), -guiProperties.guiTop());
				bookmarkOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
				ingredientListOverlay.drawOnForeground(guiGraphics, mouseX, mouseY);
			}

			drawMainContents(screen, guiProperties, guiGraphics, mouseX, mouseY);
		}
		poseStack.popMatrix();
	}

	public void drawForScreen(Screen screen, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (screen instanceof AbstractContainerScreen<?>) {
			// for container screens, drawing the main contents is handled in drawForContainerScreen
			return;
		}
		@Nullable IGuiProperties guiProperties = screenHelper.getGuiProperties(screen).orElse(null);
		drawMainContents(screen, guiProperties, guiGraphics, mouseX, mouseY);
	}

	private void drawMainContents(Screen screen, @Nullable IGuiProperties guiProperties, GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		Minecraft minecraft = Minecraft.getInstance();

		Set<ImmutableRect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
			.map(ImmutableRect2i::new)
			.collect(Collectors.toUnmodifiableSet());
		ingredientListOverlay.getScreenPropertiesUpdater()
			.updateScreen(guiProperties)
			.updateExclusionAreas(guiExclusionAreas)
			.update();
		bookmarkOverlay.getScreenPropertiesUpdater()
			.updateScreen(guiProperties)
			.updateExclusionAreas(guiExclusionAreas)
			.update();

		DeltaTracker deltaTracker = minecraft.getDeltaTracker();
		float partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
		ingredientListOverlay.drawScreen(minecraft, guiGraphics, mouseX, mouseY, partialTicks);
		bookmarkOverlay.drawScreen(minecraft, guiGraphics, mouseX, mouseY, partialTicks);

		if (guiProperties != null && screen instanceof AbstractContainerScreen<?> guiContainer) {
			int guiLeft = guiProperties.guiLeft();
			int guiTop = guiProperties.guiTop();
			this.screenHelper.getGuiClickableArea(guiContainer, mouseX - guiLeft, mouseY - guiTop)
				.filter(IGuiClickableArea::isTooltipEnabled)
				.findFirst()
				.ifPresent(area -> {
					JeiTooltip tooltip = new JeiTooltip();
					area.getTooltip(tooltip);
					if (tooltip.isEmpty()) {
						tooltip.add(Component.translatable("jei.tooltip.show.recipes"));
					}
					tooltip.draw(guiGraphics, mouseX, mouseY);
				});
		}

		ingredientListOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);
		bookmarkOverlay.drawTooltips(minecraft, guiGraphics, mouseX, mouseY);

		if (DebugConfig.isDebugGuisEnabled()) {
			drawDebugInfoForScreen(screen, guiProperties, guiGraphics);
		}
	}

	public boolean renderCompactPotionIndicators() {
		return ingredientListOverlay.isListDisplayed();
	}

	private void drawDebugInfoForScreen(Screen screen, @Nullable IGuiProperties guiProperties, GuiGraphicsExtractor guiGraphics) {
		RectDebugger.INSTANCE.draw(guiGraphics);

		if (guiProperties != null) {
			Set<Rect2i> guiExclusionAreas = screenHelper.getGuiExclusionAreas(screen)
				.collect(Collectors.toUnmodifiableSet());

			// draw the gui exclusion areas
			for (Rect2i area : guiExclusionAreas) {
				guiGraphics.fill(
					area.getX(),
					area.getY(),
					area.getX() + area.getWidth(),
					area.getY() + area.getHeight(),
					0x44FF0000
				);
			}

			// draw the gui area
			guiGraphics.fill(
				guiProperties.guiLeft(),
				guiProperties.guiTop(),
				guiProperties.guiLeft() + guiProperties.guiXSize(),
				guiProperties.guiTop() + guiProperties.guiYSize(),
				0x22CCCC00
			);
		} else {
			return;
		}

	}
}
