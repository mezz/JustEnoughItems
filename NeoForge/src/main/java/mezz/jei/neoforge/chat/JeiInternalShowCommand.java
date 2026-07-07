package mezz.jei.neoforge.chat;
 
import com.mojang.brigadier.arguments.StringArgumentType;
import mezz.jei.gui.chat.JeiChatItemLinks;
import mezz.jei.neoforge.events.PermanentEventSubscriptions;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

public final class JeiInternalShowCommand {
	private JeiInternalShowCommand() {
	}
 
	public static void register(PermanentEventSubscriptions subscriptions) {
		subscriptions.register(RegisterClientCommandsEvent.class, JeiInternalShowCommand::onRegisterClientCommands);
	}
 
	private static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
		event.getDispatcher().register(
			Commands.literal(JeiChatItemLinks.SHOW_RECIPE_COMMAND)
				.then(Commands.argument("itemId", StringArgumentType.greedyString())
					.executes(context -> {
						String itemId = StringArgumentType.getString(context, "itemId");
						boolean shown = JeiChatItemLinks.showRecipeForItemId(itemId);
						return shown ? 1 : 0;
					})
				)
		);
	}
}
 