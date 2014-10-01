package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Commands {


	private static void sendCommand(String command) {
		Minecraft.getMinecraft().thePlayer.sendChatMessage(command);
	}

	public static void giveFullStack(ItemStack itemstack) {
		giveStack(itemstack, itemstack.getMaxStackSize());
	}

	public static void giveOneFromStack(ItemStack itemstack) {
		giveStack(itemstack, 1);
	}

	/**
	 * give <player> <item> [amount] [data] [dataTag]
	 */
	public static void giveStack(ItemStack itemStack, int amount) {
		StringBuilder command = new StringBuilder("/give");
		command.append(" ").append(Minecraft.getMinecraft().thePlayer.getCommandSenderName());
		command.append(" ").append(Item.itemRegistry.getNameForObject(itemStack.getItem()));
		command.append(" ").append(amount);
		command.append(" ").append(itemStack.getItemDamage());

		if (itemStack.hasTagCompound())
			command.append(" ").append(itemStack.getTagCompound().toString());

		sendCommand(command.toString());
	}
}
