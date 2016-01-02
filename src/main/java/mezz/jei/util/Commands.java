package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

import mezz.jei.JustEnoughItems;
import mezz.jei.config.Config;
import mezz.jei.network.packets.PacketGiveItemMessageBig;

public class Commands {

	public static void giveFullStack(@Nonnull ItemStack itemstack) {
		giveStack(itemstack, itemstack.getMaxStackSize());
	}

	public static void giveOneFromStack(@Nonnull ItemStack itemstack) {
		giveStack(itemstack, 1);
	}

	/**
	 * /give <player> <item> [amount] [data] [dataTag]
	 */
	public static void giveStack(@Nonnull ItemStack itemStack, int amount) {
		EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
		String senderName = sender.getName();
		
		List<String> commandStrings = new ArrayList<>();
		commandStrings.add("/give");
		commandStrings.add(senderName);
		commandStrings.add(Item.itemRegistry.getNameForObject(itemStack.getItem()).toString());
		commandStrings.add(String.valueOf(amount));
		commandStrings.add(String.valueOf(itemStack.getMetadata()));

		if (itemStack.hasTagCompound()) {
			commandStrings.add(itemStack.getTagCompound().toString());
		}

		String fullCommand = StringUtils.join(commandStrings, " ");
		sendChatMessage(sender, fullCommand);
	}

	private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
		if (chatMessage.length() <= 100) {
			sender.sendChatMessage(chatMessage);
		} else {
			if (Config.isJeiOnServer()) {
				PacketGiveItemMessageBig packet = new PacketGiveItemMessageBig(chatMessage);
				JustEnoughItems.getProxy().sendPacketToServer(packet);
			} else {
				ChatComponentTranslation errorMessage = new ChatComponentTranslation("jei.chat.error.command.too.long");
				errorMessage.getChatStyle().setColor(EnumChatFormatting.RED);
				sender.addChatComponentMessage(errorMessage);

				ChatComponentText chatMessageComponent = new ChatComponentText(chatMessage);
				chatMessageComponent.getChatStyle().setColor(EnumChatFormatting.RED);
				sender.addChatComponentMessage(chatMessageComponent);
			}
		}
	}
}
