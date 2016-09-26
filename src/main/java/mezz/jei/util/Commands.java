package mezz.jei.util;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.JustEnoughItems;
import mezz.jei.config.SessionData;
import mezz.jei.network.packets.PacketGiveItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
			sendStack.stackSize = amount;
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
		Item item = itemStack.getItem();
		if (item == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Null item in itemStack: " + stackInfo, new NullPointerException());
			return;
		}

		ResourceLocation itemResourceLocation = item.getRegistryName();
		if (itemResourceLocation == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("item.getRegistryName() returned null for: " + stackInfo, new NullPointerException());
			return;
		}

		EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
		String[] commandParameters = getGiveCommandParameters(sender, itemStack, amount);
		String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
		sendChatMessage(sender, fullCommand);
	}

	public static String[] getGiveCommandParameters(EntityPlayer sender, ItemStack itemStack, int amount) {
		String senderName = sender.getName();
		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		if (itemResourceLocation == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			throw new IllegalArgumentException("item.getRegistryName() returned null for: " + stackInfo);
		}

		List<String> commandStrings = new ArrayList<String>();
		commandStrings.add(senderName);
		commandStrings.add(itemResourceLocation.toString());
		commandStrings.add(String.valueOf(amount));
		commandStrings.add(String.valueOf(itemStack.getMetadata()));

		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound != null) {
			commandStrings.add(tagCompound.toString());
		}

		return commandStrings.toArray(new String[commandStrings.size()]);
	}

	private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
		if (chatMessage.length() <= 100) {
			sender.sendChatMessage(chatMessage);
		} else {
			ITextComponent errorMessage = new TextComponentTranslation("jei.chat.error.command.too.long");
			errorMessage.getStyle().setColor(TextFormatting.RED);
			sender.addChatComponentMessage(errorMessage);

			ITextComponent chatMessageComponent = new TextComponentString(chatMessage);
			chatMessageComponent.getStyle().setColor(TextFormatting.RED);
			sender.addChatComponentMessage(chatMessageComponent);
		}
	}
}
