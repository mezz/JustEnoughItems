package mezz.jei.fabric.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import mezz.jei.common.chat.JeiChatItemLinkRecipeLookup;
import mezz.jei.common.chat.JeiChatItemLinks;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;

public final class JeiInternalShowCommand {
	private JeiInternalShowCommand() {
	}

	public static void register() {
		ClientCommandManager.DISPATCHER.register(
			ClientCommandManager.literal(JeiChatItemLinks.SHOW_RECIPE_COMMAND)
				.then(ClientCommandManager.argument(JeiChatItemLinks.LINK_ARGUMENT, StringArgumentType.greedyString())
					.executes(context -> {
						String link = StringArgumentType.getString(context, JeiChatItemLinks.LINK_ARGUMENT);
						return JeiChatItemLinkRecipeLookup.executeShowRecipeCommand(link);
					})
				)
		);
	}
}
