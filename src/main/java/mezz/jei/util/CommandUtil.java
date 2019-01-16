package mezz.jei.util;

import net.minecraftforge.items.ItemHandlerHelper;
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

import mezz.jei.JustEnoughItems;
import mezz.jei.config.Config;
import mezz.jei.config.ServerInfo;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketSetHotbarItemStack;
import org.apache.commons.lang3.StringUtils;

public final class CommandUtil {
	private CommandUtil() {
	}

	/**
	 * /give <player> <item> [amount] [data] [dataTag]
	 * {@link GuiContainerCreative} has special client-side handling for itemStacks, just give the item on the client
	 */
	public static void giveStack(ItemStack itemStack, int mouseButton) {
		final GiveMode giveMode = Config.getGiveMode();
		Minecraft minecraft = Minecraft.getMinecraft();
		EntityPlayerSP player = minecraft.player;
		if (player == null) {
			Log.get().error("Can't give stack, there is no player");
			return;
		}
		if (minecraft.currentScreen instanceof GuiContainerCreative && giveMode == GiveMode.MOUSE_PICKUP) {
			final int amount = giveMode.getStackSize(itemStack, mouseButton);
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
			CommandUtilServer.mousePickupItemStack(player, sendStack);
		} else if (ServerInfo.isJeiOnServer()) {
			final int amount = giveMode.getStackSize(itemStack, mouseButton);
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
			PacketGiveItemStack packet = new PacketGiveItemStack(sendStack, giveMode);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		} else {
			int amount = GiveMode.INVENTORY.getStackSize(itemStack, mouseButton);
			giveStackVanilla(itemStack, amount);
		}
	}

	public static void setHotbarStack(ItemStack itemStack, int hotbarSlot) {
		if (ServerInfo.isJeiOnServer()) {
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, itemStack.getMaxStackSize());
			PacketSetHotbarItemStack packet = new PacketSetHotbarItemStack(sendStack, hotbarSlot);
			JustEnoughItems.getProxy().sendPacketToServer(packet);
		}
	}

	/**
	 * Fallback for when JEI is not on the server, tries to use the /give command
	 * Uses the Creative Inventory Action Packet when in creative, which doesn't require the player to be op.
	 */
	private static void giveStackVanilla(ItemStack itemStack, int amount) {
		if (itemStack.isEmpty()) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			Log.get().error("Empty itemStack: {}", stackInfo, new IllegalArgumentException());
			return;
		}

		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		ErrorUtil.checkNotNull(itemResourceLocation, "itemStack.getItem().getRegistryName()");

		EntityPlayerSP sender = Minecraft.getMinecraft().player;
		if (sender != null) {
			if (sender.canUseCommand(2, "give")) {
				sendGiveAction(sender, itemStack, amount);
			} else if (sender.isCreative()) {
				sendCreativeInventoryActions(sender, itemStack, amount);
			} else {
				// try this in case the vanilla server has permissions set so regular players can use /give
				sendGiveAction(sender, itemStack, amount);
			}
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
