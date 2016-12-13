package mezz.jei.util;

import com.google.common.base.Preconditions;
import mezz.jei.JustEnoughItems;
import mezz.jei.config.SessionData;
import mezz.jei.network.packets.PacketGiveItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.StringUtils;

public class Commands {

	public static void giveFullStack(ItemStack itemstack) {
		giveStack(itemstack, itemstack.getMaxStackSize());
	}

	public static void giveOneFromStack(ItemStack itemstack) {
		giveStack(itemstack, 1);
	}

	/**
	 * /give <player> <item> [amount] [data] [dataTag]
	 */
	public static void giveStack(ItemStack itemStack, int amount) {
		if (SessionData.isJeiOnServer()) {
			ItemStack sendStack = itemStack.copy();
			sendStack.setCount(amount);
			PacketGiveItemStack packet = new PacketGiveItemStack(sendStack);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		} else {
			giveStackVanilla(itemStack, amount);
		}
	}

	/**
	 * Fallback for when JEI is not on the server, tries to use the /give command.
	 */
	private static void giveStackVanilla(ItemStack itemStack, int amount) {
		if (itemStack.isEmpty()) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Empty itemStack: " + stackInfo, new IllegalArgumentException());
			return;
		}

		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		Preconditions.checkNotNull(itemResourceLocation);

		EntityPlayerSP sender = Minecraft.getMinecraft().player;
		String[] commandParameters = CommandUtil.getGiveCommandParameters(sender, itemStack, amount);
		String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
		sendChatMessage(sender, fullCommand);
	}

	private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
		if (chatMessage.length() <= 256) {
			sender.sendChatMessage(chatMessage);
		} else {
			ITextComponent errorMessage = new TextComponentTranslation("jei.chat.error.command.too.long");
			errorMessage.getStyle().setColor(TextFormatting.RED);
			sender.sendStatusMessage(errorMessage, false);

			ITextComponent chatMessageComponent = new TextComponentString(chatMessage);
			chatMessageComponent.getStyle().setColor(TextFormatting.RED);
			sender.sendStatusMessage(chatMessageComponent, false);
		}
	}
}
