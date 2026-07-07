package mezz.jei.fabric.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import mezz.jei.gui.chat.JeiChatItemLinks;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;

public final class JeiInternalShowCommand {
	private JeiInternalShowCommand() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(
				ClientCommands.literal(JeiChatItemLinks.SHOW_RECIPE_COMMAND)
					.then(ClientCommands.argument("itemId", StringArgumentType.greedyString())
						.executes(context -> {
							String itemId = StringArgumentType.getString(context, "itemId");
							boolean shown = JeiChatItemLinks.showRecipeForItemId(itemId);
							return shown ? 1 : 0;
						})
					)
			)
		);
	}
}