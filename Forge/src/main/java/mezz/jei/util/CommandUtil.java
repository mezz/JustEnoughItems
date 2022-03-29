package mezz.jei.util;

import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.util.GiveAmount;
import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IClientConfig;
import mezz.jei.network.packets.PacketGiveItemStack;
import mezz.jei.network.packets.PacketSetHotbarItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CommandUtil {
	private static final Logger LOGGER = LogManager.getLogger();
	private final IClientConfig clientConfig;
	private final IConnectionToServer serverConnection;

	public CommandUtil(IClientConfig clientConfig, IConnectionToServer serverConnection) {
		this.clientConfig = clientConfig;
		this.serverConnection = serverConnection;
	}

	/**
	 * /give &lt;player&gt; &lt;item&gt; [amount]
	 * <p>
	 * {@link CreativeModeInventoryScreen} has special client-side handling for itemStacks, just give the item on the client
	 */
	public void giveStack(ItemStack itemStack, GiveAmount giveAmount) {
		final GiveMode giveMode = clientConfig.getGiveMode();
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		if (player == null) {
			LOGGER.error("Can't give stack, there is no player");
			return;
		}
		final int amount = giveAmount.getAmountForStack(itemStack);
		if (minecraft.screen instanceof CreativeModeInventoryScreen && giveMode == GiveMode.MOUSE_PICKUP) {
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
			ServerCommandUtil.mousePickupItemStack(player, sendStack);
		} else if (serverConnection.isJeiOnServer()) {
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, amount);
			PacketGiveItemStack packet = new PacketGiveItemStack(sendStack, giveMode);
			serverConnection.sendPacketToServer(packet);
		} else {
			giveStackVanilla(itemStack, amount);
		}
	}

	public void setHotbarStack(ItemStack itemStack, int hotbarSlot) {
		if (serverConnection.isJeiOnServer()) {
			ItemStack sendStack = ItemHandlerHelper.copyStackWithSize(itemStack, itemStack.getMaxStackSize());
			PacketSetHotbarItemStack packet = new PacketSetHotbarItemStack(sendStack, hotbarSlot);
			serverConnection.sendPacketToServer(packet);
		}
	}

	/**
	 * Fallback for when JEI is not on the server, tries to use the /give command
	 * Uses the Creative Inventory Action Packet when in creative, which doesn't require the player to be op.
	 */
	private static void giveStackVanilla(ItemStack itemStack, int amount) {
		if (itemStack.isEmpty()) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			LOGGER.error("Empty itemStack: {}", stackInfo, new IllegalArgumentException());
			return;
		}

		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		ErrorUtil.checkNotNull(itemResourceLocation, "itemStack.getItem().getRegistryName()");

		LocalPlayer sender = Minecraft.getInstance().player;
		if (sender != null) {
			if (sender.createCommandSourceStack().hasPermission(2)) {
				sendGiveAction(sender, itemStack, amount);
			} else if (sender.isCreative()) {
				sendCreativeInventoryActions(sender, itemStack, amount);
			} else {
				// try this in case the vanilla server has permissions set so regular players can use /give
				sendGiveAction(sender, itemStack, amount);
			}
		}
	}

	private static void sendGiveAction(LocalPlayer sender, ItemStack itemStack, int amount) {
		String[] commandParameters = ServerCommandUtil.getGiveCommandParameters(sender, itemStack, amount);
		String fullCommand = "/give " + StringUtils.join(commandParameters, " ");
		sendChatMessage(sender, fullCommand);
	}

	private static void sendChatMessage(LocalPlayer sender, String chatMessage) {
		if (chatMessage.length() <= 256) {
			sender.chat(chatMessage);
		} else {
			Component errorMessage = new TranslatableComponent("jei.chat.error.command.too.long");
			errorMessage.getStyle().applyFormat(ChatFormatting.RED);
			sender.displayClientMessage(errorMessage, false);

			Component chatMessageComponent = new TextComponent(chatMessage);
			chatMessageComponent.getStyle().applyFormat(ChatFormatting.RED);
			sender.displayClientMessage(chatMessageComponent, false);
		}
	}

	private static void sendCreativeInventoryActions(LocalPlayer sender, ItemStack stack, int amount) {
		int i = 0; // starting in the inventory, not armour or crafting slots
		while (i < sender.getInventory().items.size() && amount > 0) {
			ItemStack currentStack = sender.getInventory().items.get(i);
			if (currentStack.isEmpty()) {
				ItemStack sendAllRemaining = ItemHandlerHelper.copyStackWithSize(stack, amount);
				sendSlotPacket(sendAllRemaining, i);
				amount = 0;
			} else if (currentStack.sameItem(stack) && currentStack.getMaxStackSize() > currentStack.getCount()) {
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
		Minecraft minecraft = Minecraft.getInstance();
		MultiPlayerGameMode playerController = minecraft.gameMode;
		if (playerController != null) {
			playerController.handleCreativeModeItemAdd(stack, mainInventorySlot);
		} else {
			LOGGER.error("Cannot send slot packet, minecraft.playerController is null");
		}
	}
}
