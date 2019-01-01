package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.impl.GiveCommand;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketCheatPermission;

/**
 * Server-side-safe utilities for commands.
 */
public final class CommandUtilServer {
	private CommandUtilServer() {
	}

	public static String[] getGiveCommandParameters(EntityPlayer sender, ItemStack itemStack, int amount) {
		ITextComponent senderName = sender.getName();
		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		if (itemResourceLocation == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			throw new IllegalArgumentException("item.getRegistryName() returned null for: " + stackInfo);
		}

		List<String> commandStrings = new ArrayList<>();
		commandStrings.add(senderName.getString());
		commandStrings.add(itemResourceLocation.toString());
		NBTTagCompound tagCompound = itemStack.getTag();
		if (tagCompound != null) {
			commandStrings.add(tagCompound.toString());
		}
		commandStrings.add(String.valueOf(amount));
		return commandStrings.toArray(new String[0]);
	}

	public static void writeChatMessage(EntityPlayer player, String translationKey, TextFormatting color) {
		TextComponentTranslation component = new TextComponentTranslation(translationKey);
		component.getStyle().setColor(color);
		player.sendMessage(component);
	}

	public static boolean hasPermission(EntityPlayerMP sender) {
		if (sender.isCreative()) {
			return true;
		}
		CommandNode<CommandSource> giveCommand = getGiveCommand(sender);
		CommandSource commandSource = sender.getCommandSource();
		if (giveCommand != null) {
			return giveCommand.canUse(commandSource);
		} else {
			MinecraftServer minecraftServer = sender.server;
			int opPermissionLevel = minecraftServer.getOpPermissionLevel();
			return commandSource.hasPermissionLevel(opPermissionLevel);
		}
	}

	/**
	 * Gives a player an item.
	 */
	public static void executeGive(EntityPlayerMP sender, ItemStack itemStack, GiveMode giveMode) {
		if (hasPermission(sender)) {
			if (giveMode == GiveMode.INVENTORY) {
				giveToInventory(sender, itemStack);
			} else if (giveMode == GiveMode.MOUSE_PICKUP) {
				mousePickupItemStack(sender, itemStack);
			}
		} else {
			Network.sendPacketToClient(new PacketCheatPermission(false), sender);
		}
	}

	public static void setHotbarSlot(EntityPlayerMP sender, ItemStack itemStack, int hotbarSlot) {
		if (hasPermission(sender)) {
			if (!InventoryPlayer.isHotbar(hotbarSlot)) {
				Log.get().error("Tried to set slot that is not in the hotbar: {}", hotbarSlot);
				return;
			}
			ItemStack stackInSlot = sender.inventory.getStackInSlot(hotbarSlot);
			if (ItemStack.areItemStacksEqual(stackInSlot, itemStack)) {
				return;
			}
			ItemStack itemStackCopy = itemStack.copy();
			sender.inventory.setInventorySlotContents(hotbarSlot, itemStack);
			sender.world.playSound(null, sender.posX, sender.posY, sender.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((sender.getRNG().nextFloat() - sender.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			sender.inventoryContainer.detectAndSendChanges();
			notifyGive(sender, itemStackCopy);
		} else {
			Network.sendPacketToClient(new PacketCheatPermission(false), sender);
		}
	}

	public static void mousePickupItemStack(EntityPlayer sender, ItemStack itemStack) {
		int giveCount;
		ItemStack itemStackCopy = itemStack.copy();
		ItemStack existingStack = sender.inventory.getItemStack();
		if (ItemHandlerHelper.canItemStacksStack(existingStack, itemStack)) {
			int newCount = Math.min(existingStack.getMaxStackSize(), existingStack.getCount() + itemStack.getCount());
			giveCount = newCount - existingStack.getCount();
			existingStack.setCount(newCount);
		} else {
			sender.inventory.setItemStack(itemStack);
			giveCount = itemStack.getCount();
		}

		if (sender instanceof EntityPlayerMP) {
			EntityPlayerMP playerMP = (EntityPlayerMP) sender;
			itemStackCopy.setCount(giveCount);
			notifyGive(playerMP, itemStackCopy);
			playerMP.updateHeldItem();
		}
	}

	/**
	 * Gives a player an item.
	 *
	 * @see GiveCommand#giveItem(net.minecraft.command.CommandSource, net.minecraft.command.arguments.ItemInput, java.util.Collection, int)
	 */
	@SuppressWarnings("JavadocReference")
	private static void giveToInventory(EntityPlayerMP entityplayermp, ItemStack itemStack) {
		ItemStack itemStackCopy = itemStack.copy();
		boolean flag = entityplayermp.inventory.addItemStackToInventory(itemStack);
		if (flag && itemStack.isEmpty()) {
			itemStack.setCount(1);
			EntityItem entityitem = entityplayermp.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.makeFakeItem();
			}

			entityplayermp.world.playSound(null, entityplayermp.posX, entityplayermp.posY, entityplayermp.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayermp.getRNG().nextFloat() - entityplayermp.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			entityplayermp.inventoryContainer.detectAndSendChanges();
		} else {
			EntityItem entityitem = entityplayermp.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.setNoPickupDelay();
				entityitem.setOwnerId(entityplayermp.getUniqueID());
			}
		}

		notifyGive(entityplayermp, itemStackCopy);
	}

	private static void notifyGive(EntityPlayerMP entityPlayerMP, ItemStack stack) {
		CommandSource commandSource = entityPlayerMP.getCommandSource();
		int count = stack.getCount();
		ITextComponent stackTextComponent = stack.getTextComponent();
		ITextComponent displayName = entityPlayerMP.getDisplayName();
		TextComponentTranslation message = new TextComponentTranslation("commands.give.success.single", count, stackTextComponent, displayName);
		commandSource.sendFeedback(message, true);
	}

	@Nullable
	private static CommandNode<CommandSource> getGiveCommand(EntityPlayerMP sender) {
		MinecraftServer minecraftServer = sender.server;
		Commands commandManager = minecraftServer.getCommandManager();
		CommandDispatcher<CommandSource> dispatcher = commandManager.getDispatcher();
		RootCommandNode<CommandSource> root = dispatcher.getRoot();
		return root.getChild("give");
	}
}
