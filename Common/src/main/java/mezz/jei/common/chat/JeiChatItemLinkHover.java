package mezz.jei.common.chat;

import mezz.jei.common.chat.JeiChatItemLinks.IngredientLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
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
				Minecraft minecraft = Minecraft.getInstance();
				ChatComponent chat = minecraft.gui.getChat();
				int left = Mth.floor(mouseX);
				int right = left + 1;
				while (left > 0 && style.equals(chat.getClickedComponentStyleAt(left - 1, mouseY))) {
					left--;
				}
				int screenWidth = minecraft.getWindow().getGuiScaledWidth();
				while (right < screenWidth && style.equals(chat.getClickedComponentStyleAt(right, mouseY))) {
					right++;
				}
				// Match the target ChatComponent's scaled line hitboxes.
				int lineHeight = (int) (9 * (minecraft.options.chatLineSpacing().get() + 1));
				double scaledLineHeight = lineHeight * chat.getScale();
				int chatBottom = minecraft.getWindow().getGuiScaledHeight() - 40;
				int line = Mth.floor((chatBottom - mouseY) / scaledLineHeight);
				int top = Mth.floor(chatBottom - (line + 1) * scaledLineHeight);
				int bottom = Mth.ceil(chatBottom - line * scaledLineHeight);
				return new HoveredText(style, new Rect2i(left, top, right - left, bottom - top));
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
