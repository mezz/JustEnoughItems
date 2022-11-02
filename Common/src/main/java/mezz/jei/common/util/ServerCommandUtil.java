package mezz.jei.common.util;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import mezz.jei.common.network.IConnectionToClient;
import mezz.jei.common.network.ServerPacketContext;
import mezz.jei.common.network.packets.PacketCheatPermission;
import mezz.jei.core.config.GiveMode;
import mezz.jei.core.config.IServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Server-side-safe utilities for commands.
 */
public final class ServerCommandUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private ServerCommandUtil() {
	}

	public static boolean hasPermissionForCheatMode(ServerPlayer sender, IServerConfig serverConfig) {
		if (serverConfig.isCheatModeEnabledForCreative() &&
			sender.isCreative()) {
			return true;
		}

		CommandSourceStack commandSource = sender.createCommandSourceStack();
		if (serverConfig.isCheatModeEnabledForOp()) {
			MinecraftServer minecraftServer = sender.getServer();
			if (minecraftServer != null) {
				int opPermissionLevel = minecraftServer.getOperatorUserPermissionLevel();
				return commandSource.hasPermission(opPermissionLevel);
			}
		}

		if (serverConfig.isCheatModeEnabledForGive()) {
			CommandNode<CommandSourceStack> giveCommand = getGiveCommand(sender);
			if (giveCommand != null) {
				return giveCommand.canUse(commandSource);
			}
		}

		return false;
	}

	/**
	 * Gives a player an item.
	 */
	public static void executeGive(
		ServerPacketContext context,
		ItemStack itemStack,
		GiveMode giveMode
	) {
		ServerPlayer sender = context.player();
		IServerConfig serverConfig = context.serverConfig();
		if (hasPermissionForCheatMode(sender, serverConfig)) {
			if (itemStack.isEmpty()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Player '{} ({})' tried to give an empty ItemStack.", sender.getName(), sender.getUUID());
				}
				return;
			}
			if (giveMode == GiveMode.INVENTORY) {
				giveToInventory(sender, itemStack);
			} else if (giveMode == GiveMode.MOUSE_PICKUP) {
				mousePickupItemStack(sender, itemStack);
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Player '{} ({})' tried to cheat an ItemStack '{}' but does not have permission.", sender.getName(), sender.getUUID(), itemStack.getDisplayName());
			}
			IConnectionToClient connection = context.connection();
			connection.sendPacketToClient(new PacketCheatPermission(false), sender);
		}
	}

	public static void setHotbarSlot(
		ServerPacketContext context,
		ItemStack itemStack,
		int hotbarSlot
	) {
		ServerPlayer sender = context.player();
		IServerConfig serverConfig = context.serverConfig();
		if (hasPermissionForCheatMode(sender, serverConfig)) {
			if (itemStack.isEmpty()) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Player '{} ({})' tried to set an empty ItemStack to the hotbar slot: {}", sender.getName(), sender.getUUID(), hotbarSlot);
				}
				return;
			}
			if (!Inventory.isHotbarSlot(hotbarSlot)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Player '{} ({})' tried to set slot that is not in the hotbar: {}", sender.getName(), sender.getUUID(), hotbarSlot);
				}
				return;
			}
			ItemStack stackInSlot = sender.getInventory().getItem(hotbarSlot);
			if (ItemStack.matches(stackInSlot, itemStack)) {
				return;
			}
			ItemStack itemStackCopy = itemStack.copy();
			sender.getInventory().setItem(hotbarSlot, itemStack);
			sender.level.playSound(null, sender.getX(), sender.getY(), sender.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((sender.getRandom().nextFloat() - sender.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			sender.inventoryMenu.broadcastChanges();
			notifyGive(sender, itemStackCopy);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Player '{} ({})' tried to cheat an item '{}' to their hotbar but does not have permission.", sender.getName(), sender.getUUID(), itemStack.getDisplayName());
			}
			IConnectionToClient connection = context.connection();
			connection.sendPacketToClient(new PacketCheatPermission(false), sender);
		}
	}

	public static void mousePickupItemStack(Player sender, ItemStack itemStack) {
		AbstractContainerMenu containerMenu = sender.containerMenu;

		ItemStack itemStackCopy = itemStack.copy();
		ItemStack existingStack = containerMenu.getCarried();

		final int giveCount;
		if (canStack(existingStack, itemStack)) {
			int newCount = Math.min(existingStack.getMaxStackSize(), existingStack.getCount() + itemStack.getCount());
			giveCount = newCount - existingStack.getCount();
			existingStack.setCount(newCount);
		} else {
			containerMenu.setCarried(itemStack);
			giveCount = itemStack.getCount();
		}

		if (giveCount > 0) {
			itemStackCopy.setCount(giveCount);
			notifyGive(sender, itemStackCopy);
			containerMenu.broadcastChanges();
		}
	}

	public static boolean canStack(ItemStack a, ItemStack b) {
		ItemStack singleStack = a.copy();
		singleStack.setCount(1);
		return ItemEntity.areMergable(singleStack, b);
	}

	/**
	 * Gives a player an item.
	 *
	 * @see GiveCommand#giveItem(CommandSource, ItemInput, Collection, int)
	 */
	@SuppressWarnings("JavadocReference")
	private static void giveToInventory(Player entityplayermp, ItemStack itemStack) {
		ItemStack itemStackCopy = itemStack.copy();
		boolean flag = entityplayermp.getInventory().add(itemStack);
		if (flag && itemStack.isEmpty()) {
			itemStack.setCount(1);
			ItemEntity entityitem = entityplayermp.drop(itemStack, false);
			if (entityitem != null) {
				entityitem.makeFakeItem();
			}

			entityplayermp.level.playSound(null, entityplayermp.getX(), entityplayermp.getY(), entityplayermp.getZ(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, ((entityplayermp.getRandom().nextFloat() - entityplayermp.getRandom().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			entityplayermp.inventoryMenu.broadcastChanges();
		} else {
			ItemEntity entityitem = entityplayermp.drop(itemStack, false);
			if (entityitem != null) {
				entityitem.setNoPickUpDelay();
				entityitem.setOwner(entityplayermp.getUUID());
			}
		}

		notifyGive(entityplayermp, itemStackCopy);
	}

	private static void notifyGive(Player player, ItemStack stack) {
		if (player.getServer() == null) {
			return;
		}
		CommandSourceStack commandSource = player.createCommandSourceStack();
		int count = stack.getCount();
		Component stackTextComponent = stack.getDisplayName();
		Component displayName = player.getDisplayName();
		Component message = Component.translatable("commands.give.success.single", count, stackTextComponent, displayName);
		commandSource.sendSuccess(message, true);
	}

	@Nullable
	private static CommandNode<CommandSourceStack> getGiveCommand(Player sender) {
		MinecraftServer minecraftServer = sender.getServer();
		if (minecraftServer == null) {
			return null;
		}
		Commands commandManager = minecraftServer.getCommands();
		CommandDispatcher<CommandSourceStack> dispatcher = commandManager.getDispatcher();
		RootCommandNode<CommandSourceStack> root = dispatcher.getRoot();
		return root.getChild("give");
	}
}
