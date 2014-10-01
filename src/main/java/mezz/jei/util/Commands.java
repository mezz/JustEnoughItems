package mezz.jei.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.command.CommandGive;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

public class Commands {

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
		EntityClientPlayerMP sender = Minecraft.getMinecraft().thePlayer;
		String senderName = sender.getCommandSenderName();

		ArrayList<String> commandStrings = new ArrayList<String>();
		commandStrings.add(senderName);
		commandStrings.add(Item.itemRegistry.getNameForObject(itemStack.getItem()));
		commandStrings.add("" + amount);
		commandStrings.add("" + itemStack.getItemDamage());

		if (itemStack.hasTagCompound())
			commandStrings.add(itemStack.getTagCompound().toString());

		CommandGive commandGive = new CommandGive();
		commandGive.processCommand(sender, commandStrings.toArray(new String[commandStrings.size()]));
	}
}
