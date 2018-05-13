package mezz.jei.util;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import mezz.jei.JustEnoughItems;
import mezz.jei.config.SessionData;
import mezz.jei.network.packets.PacketGiveItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
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

	/**
	 * /give <player> <item> [amount] [data] [dataTag]
	 * {@link GuiContainerCreative} has special client-side handling for itemStacks, just give the item on the client
	 */
	public static void giveStack(ItemStack itemStack, int mouseButton) {
		final int amount = (mouseButton == 0) ? itemStack.getMaxStackSize() : 1;
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
	private static void giveStackVanilla(@Nullable ItemStack itemStack, int amount) {
		if (itemStack == null || itemStack.stackSize == 0) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.error("Empty itemStack: {}", stackInfo, new IllegalArgumentException());
			return;
		}

		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		Preconditions.checkNotNull(itemResourceLocation, "itemStack.getItem().getRegistryName()");

		EntityPlayerSP sender = Minecraft.getMinecraft().thePlayer;
		if (sender.canCommandSenderUseCommand(2, "give")) {
			sendGiveAction(sender, itemStack, amount);
		} else if (sender.isCreative()) {
			sendCreativeInventoryActions(sender, itemStack, amount);
		} else {
			// try this in case the vanilla server has permissions set so regular players can use /give
			sendGiveAction(sender, itemStack, amount);
		}
	}

	private static void sendGiveAction(EntityPlayerSP sender, ItemStack itemStack, int amount) {
		String[] commandParameters = CommandUtilServer.getGiveCommandParameters(sender, itemStack, amount);
		String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
		sendChatMessage(sender, fullCommand);
	}

	private static void sendChatMessage(EntityPlayerSP sender, String chatMessage) {
		if (chatMessage.length() <= 256) {
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

	private static void sendCreativeInventoryActions(EntityPlayerSP sender, ItemStack stack, int amount) {
		int i = 0; // starting in the inventory, not armour or crafting slots
		while (i < sender.inventory.mainInventory.length && amount > 0) {
			ItemStack currentStack = sender.inventory.mainInventory[i];
			if (currentStack == null || currentStack.stackSize == 0) {
				ItemStack sendAllRemaining = ItemHandlerHelper.copyStackWithSize(stack, amount);
				sendSlotPacket(sendAllRemaining, i);
				amount = 0;
			} else if (currentStack.isItemEqual(stack) && currentStack.getMaxStackSize() > currentStack.stackSize) {
				int canAdd = Math.min(currentStack.getMaxStackSize() - currentStack.stackSize, amount);
				ItemStack fillRemainingSpace = ItemHandlerHelper.copyStackWithSize(stack, canAdd + currentStack.stackSize);
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
