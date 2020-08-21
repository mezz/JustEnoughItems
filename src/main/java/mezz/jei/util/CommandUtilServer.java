package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.util.Util;
import net.minecraftforge.items.ItemHandlerHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ItemInput;
import net.minecraft.command.impl.GiveCommand;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import mezz.jei.network.Network;
import mezz.jei.network.packets.PacketCheatPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Server-side-safe utilities for commands.
 */
public final class CommandUtilServer {
	private static final Logger LOGGER = LogManager.getLogger();

	private CommandUtilServer() {
	}

	public static String[] getGiveCommandParameters(PlayerEntity sender, ItemStack itemStack, int amount) {
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
		CompoundNBT tagCompound = itemStack.getTag();
		if (tagCompound != null) {
			commandStrings.add(tagCompound.toString());
		}
		commandStrings.add(String.valueOf(amount));
		return commandStrings.toArray(new String[0]);
	}

	public static void writeChatMessage(PlayerEntity player, String translationKey, TextFormatting color) {
		TranslationTextComponent component = new TranslationTextComponent(translationKey);
		component.getStyle().applyFormatting(color);
		player.sendMessage(component, Util.DUMMY_UUID);
	}

	public static boolean hasPermission(PlayerEntity sender) {
		if (sender.isCreative()) {
			return true;
		}
		CommandNode<CommandSource> giveCommand = getGiveCommand(sender);
		CommandSource commandSource = sender.getCommandSource();
		if (giveCommand != null) {
			return giveCommand.canUse(commandSource);
		} else {
			MinecraftServer minecraftServer = sender.getServer();
			if (minecraftServer == null) {
				return false;
			}
			int opPermissionLevel = minecraftServer.getOpPermissionLevel();
			return commandSource.hasPermissionLevel(opPermissionLevel);
		}
	}

	/**
	 * Gives a player an item.
	 */
	public static void executeGive(ServerPlayerEntity sender, ItemStack itemStack, GiveMode giveMode) {
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

	public static void setHotbarSlot(ServerPlayerEntity sender, ItemStack itemStack, int hotbarSlot) {
		if (hasPermission(sender)) {
			if (!PlayerInventory.isHotbar(hotbarSlot)) {
				LOGGER.error("Tried to set slot that is not in the hotbar: {}", hotbarSlot);
				return;
			}
			ItemStack stackInSlot = sender.inventory.getStackInSlot(hotbarSlot);
			if (ItemStack.areItemStacksEqual(stackInSlot, itemStack)) {
				return;
			}
			ItemStack itemStackCopy = itemStack.copy();
			sender.inventory.setInventorySlotContents(hotbarSlot, itemStack);
			sender.world.playSound(null, sender.getPosX(), sender.getPosY(), sender.getPosZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((sender.getRNG().nextFloat() - sender.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			sender.container.detectAndSendChanges();
			notifyGive(sender, itemStackCopy);
		} else {
			Network.sendPacketToClient(new PacketCheatPermission(false), sender);
		}
	}

	public static void mousePickupItemStack(PlayerEntity sender, ItemStack itemStack) {
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

		if (sender instanceof ServerPlayerEntity) {
			ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) sender;
			itemStackCopy.setCount(giveCount);
			notifyGive(serverPlayerEntity, itemStackCopy);
			serverPlayerEntity.updateHeldItem();
		}
	}

	/**
	 * Gives a player an item.
	 *
	 * @see GiveCommand#giveItem(CommandSource, ItemInput, Collection, int)
	 */
	@SuppressWarnings("JavadocReference")
	private static void giveToInventory(PlayerEntity entityplayermp, ItemStack itemStack) {
		ItemStack itemStackCopy = itemStack.copy();
		boolean flag = entityplayermp.inventory.addItemStackToInventory(itemStack);
		if (flag && itemStack.isEmpty()) {
			itemStack.setCount(1);
			ItemEntity entityitem = entityplayermp.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.makeFakeItem();
			}

			entityplayermp.world.playSound(null, entityplayermp.getPosX(), entityplayermp.getPosY(), entityplayermp.getPosZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayermp.getRNG().nextFloat() - entityplayermp.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			entityplayermp.container.detectAndSendChanges();
		} else {
			ItemEntity entityitem = entityplayermp.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.setNoPickupDelay();
				entityitem.setOwnerId(entityplayermp.getUniqueID());
			}
		}

		notifyGive(entityplayermp, itemStackCopy);
	}

	private static void notifyGive(PlayerEntity entityPlayerMP, ItemStack stack) {
		CommandSource commandSource = entityPlayerMP.getCommandSource();
		int count = stack.getCount();
		ITextComponent stackTextComponent = stack.getTextComponent();
		ITextComponent displayName = entityPlayerMP.getDisplayName();
		TranslationTextComponent message = new TranslationTextComponent("commands.give.success.single", count, stackTextComponent, displayName);
		commandSource.sendFeedback(message, true);
	}

	@Nullable
	private static CommandNode<CommandSource> getGiveCommand(PlayerEntity sender) {
		MinecraftServer minecraftServer = sender.getServer();
		if (minecraftServer == null) {
			return null;
		}
		Commands commandManager = minecraftServer.getCommandManager();
		CommandDispatcher<CommandSource> dispatcher = commandManager.getDispatcher();
		RootCommandNode<CommandSource> root = dispatcher.getRoot();
		return root.getChild("give");
	}
}
