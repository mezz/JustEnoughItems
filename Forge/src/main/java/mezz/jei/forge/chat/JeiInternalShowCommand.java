package mezz.jei.forge.chat;

import com.mojang.brigadier.arguments.StringArgumentType;
import mezz.jei.common.chat.JeiChatItemLinkRecipeLookup;
import mezz.jei.common.chat.JeiChatItemLinks;
import mezz.jei.forge.events.PermanentEventSubscriptions;
import net.minecraft.commands.Commands;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;

public final class JeiInternalShowCommand {
	private JeiInternalShowCommand() {
	}

	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RegisterClientCommandsEvent.class, JeiInternalShowCommand::onRegisterClientCommands);
	}

	private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			Commands.literal(JeiChatItemLinks.SHOW_RECIPE_COMMAND)
				.then(Commands.argument(JeiChatItemLinks.LINK_ARGUMENT, StringArgumentType.greedyString())
					.executes(context -> {
						String link = StringArgumentType.getString(context, JeiChatItemLinks.LINK_ARGUMENT);
						return JeiChatItemLinkRecipeLookup.executeShowRecipeCommand(link);
					})
				)
		);
	}
}
