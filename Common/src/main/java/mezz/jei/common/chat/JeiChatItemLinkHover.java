package mezz.jei.common.chat;

import mezz.jei.common.chat.JeiChatItemLinks.IngredientLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class JeiChatItemLinkHover {
	private JeiChatItemLinkHover() {
	}

	public record HoveredText(Style style, Rect2i area) {
	}

	public static Optional<Style> getHoveredStyle(Screen screen, double mouseX, double mouseY) {
		if (!(screen instanceof ChatScreen)) {
			return Optional.empty();
		}

		Minecraft minecraft = Minecraft.getInstance();
		ChatComponent chatComponent = minecraft.gui.getChat();
		Style style = chatComponent.getClickedComponentStyleAt(mouseX, mouseY);
		return Optional.ofNullable(style);
	}

	public static Optional<HoveredText> getHoveredText(Screen screen, double mouseX, double mouseY) {
		return getHoveredStyle(screen, mouseX, mouseY)
			.map(style -> {
				Rect2i area = new Rect2i((int) mouseX, (int) mouseY, 1, 1);
				return new HoveredText(style, area);
			});
	}

	public static Optional<IngredientLink> getIngredientLink(@Nullable Style style) {
		if (style == null) {
			return Optional.empty();
		}
		ClickEvent clickEvent = style.getClickEvent();
		if (clickEvent == null || clickEvent.getAction() != ClickEvent.Action.RUN_COMMAND) {
			return Optional.empty();
		}
		String command = clickEvent.getValue();
		return JeiChatItemLinks.parseShowRecipeCommand(command);
	}
}
