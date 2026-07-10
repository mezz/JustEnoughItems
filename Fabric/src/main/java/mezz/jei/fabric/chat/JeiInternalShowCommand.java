package mezz.jei.fabric.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import mezz.jei.common.chat.JeiChatItemLinkRecipeLookup;
import mezz.jei.common.chat.JeiChatItemLinks;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;

public final class JeiInternalShowCommand {
	private JeiInternalShowCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommands.literal(JeiChatItemLinks.SHOW_RECIPE_COMMAND)
					.then(ClientCommands.argument(JeiChatItemLinks.LINK_ARGUMENT, StringArgumentType.greedyString())
						.executes(context -> {
							String link = StringArgumentType.getString(context, JeiChatItemLinks.LINK_ARGUMENT);
							return JeiChatItemLinkRecipeLookup.executeShowRecipeCommand(link);
						})
					)
			)
		);
	}
}
