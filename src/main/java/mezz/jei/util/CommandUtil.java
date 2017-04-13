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
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;

public final class CommandUtil {
	private CommandUtil() {
	}

	public static void giveStack(ItemStack itemstack, boolean fullStack) {
		int amount = fullStack ? itemstack.getMaxStackSize() : 1;
		giveStack(itemstack, amount);
	}

	/**
	 * /give <player> <item> [amount] [data] [dataTag]
	 */
	public static void giveStack(ItemStack itemStack, int amount) {
		if (SessionData.isJeiOnServer()) {
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
			PacketGiveItemStack packet = new PacketGiveItemStack(sendStack);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		} else {
			giveStackVanilla(itemStack, amount);
		}
	}

	/**
	 * Fallback for when JEI is not on the server, tries to use the /give command
	 * Uses the Creative Inventory Action Packet when in creative, which doesn't require the player to be op.
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
		if (sender.isCreative()) {
			sendCreativeInventoryActions(sender, itemStack, amount);
		} else {
			String[] commandParameters = CommandUtilServer.getGiveCommandParameters(sender, itemStack, amount);
			String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
			sendChatMessage(sender, fullCommand);
		}
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

	private static void sendCreativeInventoryActions(EntityPlayerSP sender, ItemStack stack, int amount) {
		int i = 0; // starting in the inventory, not armour or crafting slots
		while (i < sender.inventory.mainInventory.size() && amount > 0) {
			ItemStack currentStack = sender.inventory.mainInventory.get(i);
			if (currentStack.isEmpty()) {
				ItemStack sendAllRemaining = ItemHandlerHelper.copyStackWithSize(stack, amount);
				sendSlotPacket(sendAllRemaining, i);
				amount = 0;
			} else if (currentStack.isItemEqual(stack) && currentStack.getMaxStackSize() > currentStack.getCount()) {
				int canAdd = Math.min(currentStack.getMaxStackSize() - currentStack.getCount(), amount);
				ItemStack fillRemainingSpace = ItemHandlerHelper.copyStackWithSize(stack, canAdd + currentStack.getCount());
				sendSlotPacket(fillRemainingSpace, i);
				amount -= canAdd;
			}
			i++;
		}
		if (amount > 0) {
			ItemStack toDrop = ItemHandlerHelper.copyStackWithSize(stack, amount);
			sendSlotPacket(toDrop, -1);
		}
	}

	private static void sendSlotPacket(ItemStack stack, int mainInventorySlot) {
		if (mainInventorySlot < 9 && mainInventorySlot != -1) {
			// slot ID for the message is different from the slot id used in the mainInventory
			mainInventorySlot += 36;
		}
		Minecraft.getMinecraft().playerController.sendSlotPacket(stack, mainInventorySlot);
	}
}
