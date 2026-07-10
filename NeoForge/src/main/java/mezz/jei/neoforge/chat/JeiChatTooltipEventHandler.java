package mezz.jei.neoforge.chat;

import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.gui.JeiTooltip.TooltipRenderData;
import mezz.jei.gui.chat.ChatIngredientTooltip;
import mezz.jei.gui.chat.ChatIngredientTooltip.IngredientTooltipData;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

import java.util.List;
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
		JeiTooltip tooltip = tooltipData.tooltip();
		TooltipRenderData renderData = tooltip.prepareForIngredientTooltip(
			tooltipData.typedIngredient(),
			tooltipData.ingredientRenderer(),
			tooltipData.ingredientManager()
		);
		if (tooltip.isEmpty()) {
			return false;
		}

		List<ClientTooltipComponent> components = ClientHooks.gatherTooltipComponentsFromElements(
			renderData.itemStack(),
			tooltip.getLines(),
			event.getX(),
			event.getScreenWidth(),
			event.getScreenHeight(),
			renderData.font()
		);
		if (components.isEmpty()) {
			return false;
		}

		renderingJeiChatTooltip = true;
		try {
			event.getGraphics().renderTooltip(
				renderData.font(),
				components,
				event.getX(),
				event.getY(),
				event.getTooltipPositioner(),
				renderData.itemStack().get(DataComponents.TOOLTIP_STYLE)
			);
		} finally {
			renderingJeiChatTooltip = false;
		}
		return true;
	}
}
