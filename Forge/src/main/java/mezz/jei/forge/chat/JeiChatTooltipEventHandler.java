package mezz.jei.forge.chat;

import mezz.jei.forge.events.PermanentEventSubscriptions;
import mezz.jei.gui.chat.ChatIngredientTooltip;
import mezz.jei.gui.chat.ChatIngredientTooltip.IngredientTooltipData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.client.event.RenderTooltipEvent;

import java.util.Optional;

public final class JeiChatTooltipEventHandler {
	private static boolean renderingJeiChatTooltip;

	private JeiChatTooltipEventHandler() {
	}

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RenderTooltipEvent.Pre.class, JeiChatTooltipEventHandler::onRenderTooltipPre);
	}

	private static void onRenderTooltipPre(RenderTooltipEvent.Pre event) {
		if (renderingJeiChatTooltip) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		Screen screen = minecraft.screen;
		Optional<IngredientTooltipData<?>> optionalTooltipData = ChatIngredientTooltip.getTooltipForHoveredChatLink(
			screen,
			event.getX(),
			event.getY()
		);
		if (optionalTooltipData.isEmpty()) {
			return;
		}

		IngredientTooltipData<?> tooltipData = optionalTooltipData.get();
		if (renderJeiChatTooltip(event, tooltipData)) {
			event.setCanceled(true);
		}
	}

	private static <T> boolean renderJeiChatTooltip(RenderTooltipEvent.Pre event, IngredientTooltipData<T> tooltipData) {
		if (tooltipData.tooltip().isEmpty()) {
			return false;
		}

		renderingJeiChatTooltip = true;
		try {
			tooltipData.draw(event.getGraphics(), event.getX(), event.getY());
		} finally {
			renderingJeiChatTooltip = false;
		}
		return true;
	}
}
