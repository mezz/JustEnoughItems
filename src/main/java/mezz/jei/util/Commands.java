package mezz.jei.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

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
		String senderName = sender.getCommandSenderName();
		
		List<String> commandStrings = new ArrayList<>();
		commandStrings.add("/give");
		commandStrings.add(senderName);
		commandStrings.add(Item.itemRegistry.getNameForObject(itemStack.getItem()).toString());
		commandStrings.add(String.valueOf(amount));
		commandStrings.add(String.valueOf(itemStack.getItemDamage()));

		if (itemStack.hasTagCompound()) {
			commandStrings.add(itemStack.getTagCompound().toString());
		}

		String fullCommand = StringUtils.join(commandStrings, " ");
		sender.sendChatMessage(fullCommand);
	}
}
