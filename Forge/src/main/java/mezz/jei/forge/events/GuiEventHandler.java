package mezz.jei.forge.events;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.common.util.LimitedLogger;
import mezz.jei.gui.GuiScreenHelper;
import mezz.jei.gui.TooltipRenderer;
import mezz.jei.gui.overlay.IngredientListOverlay;
import mezz.jei.gui.overlay.bookmarks.LeftAreaDispatcher;
import mezz.jei.input.MouseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.event.ContainerScreenEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.List;

public class GuiEventHandler {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final LimitedLogger missingBackgroundLogger = new LimitedLogger(LOGGER, Duration.ofHours(1));

	private final IngredientListOverlay ingredientListOverlay;
	private final GuiScreenHelper guiScreenHelper;
	private final LeftAreaDispatcher leftAreaDispatcher;
	private boolean drawnOnBackground = false;

	public GuiEventHandler(
		GuiScreenHelper guiScreenHelper,
		LeftAreaDispatcher leftAreaDispatcher,
		IngredientListOverlay ingredientListOverlay
	) {
		this.guiScreenHelper = guiScreenHelper;
		this.leftAreaDispatcher = leftAreaDispatcher;
		this.ingredientListOverlay = ingredientListOverlay;
	}

	public void register(RuntimeEventSubscriptions subscriptions) {
		subscriptions.register(ScreenEvent.InitScreenEvent.Post.class, this::onGuiInit);
		subscriptions.register(ScreenOpenEvent.class, this::onGuiOpen);
		subscriptions.register(ScreenEvent.BackgroundDrawnEvent.class, this::onDrawBackgroundEventPost);
		subscriptions.register(ContainerScreenEvent.DrawForeground.class, this::onDrawForegroundEvent);
		subscriptions.register(ScreenEvent.DrawScreenEvent.Post.class, this::onDrawScreenEventPost);
		subscriptions.register(TickEvent.ClientTickEvent.class, this::onClientTick);
		subscriptions.register(ScreenEvent.PotionSizeEvent.class, this::onPotionSizeEvent);
	}

	public void onGuiInit(ScreenEvent.InitScreenEvent.Post event) {
		Screen screen = event.getScreen();
		ingredientListOverlay.updateScreen(screen, false);
		leftAreaDispatcher.updateScreen(screen, false);
	}

	public void onGuiOpen(ScreenOpenEvent event) {
		Screen screen = event.getScreen();
		ingredientListOverlay.updateScreen(screen, false);
		leftAreaDispatcher.updateScreen(screen, false);
	}

	public void onDrawBackgroundEventPost(ScreenEvent.BackgroundDrawnEvent event) {
		Screen screen = event.getScreen();
		Minecraft minecraft = screen.getMinecraft();
		boolean exclusionAreasChanged = guiScreenHelper.updateGuiExclusionAreas(screen);
		ingredientListOverlay.updateScreen(screen, exclusionAreasChanged);
		leftAreaDispatcher.updateScreen(screen, exclusionAreasChanged);

		drawnOnBackground = true;
		double mouseX = MouseUtil.getX();
		double mouseY = MouseUtil.getY();
		PoseStack poseStack = event.getPoseStack();
		ingredientListOverlay.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
		leftAreaDispatcher.drawScreen(minecraft, poseStack, (int) mouseX, (int) mouseY, minecraft.getFrameTime());
	}

	/**
	 * Draws above most ContainerScreen elements, but below the tooltips.
	 */
	public void onDrawForegroundEvent(ContainerScreenEvent.DrawForeground event) {
		AbstractContainerScreen<?> screen = event.getContainerScreen();
		Minecraft minecraft = screen.getMinecraft();
		ingredientListOverlay.drawOnForeground(minecraft, event.getPoseStack(), screen, event.getMouseX(), event.getMouseY());
	}

	public void onDrawScreenEventPost(ScreenEvent.DrawScreenEvent.Post event) {
		Screen screen = event.getScreen();
		Minecraft minecraft = screen.getMinecraft();
		PoseStack poseStack = event.getPoseStack();

		ingredientListOverlay.updateScreen(screen, false);
		leftAreaDispatcher.updateScreen(screen, false);

		if (!drawnOnBackground) {
			if (screen instanceof AbstractContainerScreen) {
				String guiName = screen.getClass().getName();
				missingBackgroundLogger.log(Level.WARN, guiName, "GUI did not draw the dark background layer behind itself, this may result in display issues: {}", guiName);
			}
			ingredientListOverlay.drawScreen(minecraft, poseStack, event.getMouseX(), event.getMouseY(), minecraft.getFrameTime());
			leftAreaDispatcher.drawScreen(minecraft, poseStack, event.getMouseX(), event.getMouseY(), minecraft.getFrameTime());
		}
		drawnOnBackground = false;

		if (screen instanceof AbstractContainerScreen<?> guiContainer) {
			guiScreenHelper.getGuiClickableArea(guiContainer, event.getMouseX() - guiContainer.getGuiLeft(), event.getMouseY() - guiContainer.getGuiTop())
				.map(IGuiClickableArea::getTooltipStrings)
				.ifPresent(tooltipStrings -> {
					if (tooltipStrings.isEmpty()) {
						tooltipStrings = List.of(new TranslatableComponent("jei.tooltip.show.recipes"));
					}
					TooltipRenderer.drawHoveringText(poseStack, tooltipStrings, event.getMouseX(), event.getMouseY());
				});
		}

		ingredientListOverlay.drawTooltips(minecraft, poseStack, event.getMouseX(), event.getMouseY());
		leftAreaDispatcher.drawTooltips(minecraft, poseStack, event.getMouseX(), event.getMouseY());
	}

	public void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		ingredientListOverlay.handleTick();
	}

	public void onPotionSizeEvent(ScreenEvent.PotionSizeEvent event) {
		if (ingredientListOverlay.isListDisplayed()) {
			// Forcibly renders the potion indicators in compact mode.
			// This gives the ingredient list overlay more room to display ingredients.
			event.setResult(Event.Result.ALLOW);
		}
	}
}
