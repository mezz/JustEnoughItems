package mezz.jei.util;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Throwables;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandGive;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;

/**
 * Server-side-safe utilities for commands.
 */
public final class CommandUtilServer {
	private CommandUtilServer() {
	}

	public static String[] getGiveCommandParameters(EntityPlayer sender, ItemStack itemStack, int amount) {
		String senderName = sender.getName();
		Item item = itemStack.getItem();
		ResourceLocation itemResourceLocation = item.getRegistryName();
		if (itemResourceLocation == null) {
			String stackInfo = ErrorUtil.getItemStackInfo(itemStack);
			throw new IllegalArgumentException("item.getRegistryName() returned null for: " + stackInfo);
		}

		List<String> commandStrings = new ArrayList<String>();
		commandStrings.add(senderName);
		commandStrings.add(itemResourceLocation.toString());
		commandStrings.add(String.valueOf(amount));
		commandStrings.add(String.valueOf(itemStack.getMetadata()));

		NBTTagCompound tagCompound = itemStack.getTagCompound();
		if (tagCompound != null) {
			commandStrings.add(tagCompound.toString());
		}

		return commandStrings.toArray(new String[commandStrings.size()]);
	}

	public static void writeChatMessage(EntityPlayer player, String translationKey, TextFormatting color) {
		TextComponentTranslation component = new TextComponentTranslation(translationKey);
		component.getStyle().setColor(color);
		player.sendMessage(component);
	}

	public static boolean hasPermission(EntityPlayerMP sender, ItemStack itemStack) {
		if (sender.isCreative()) {
			return true;
		}

		MinecraftServer minecraftServer = sender.mcServer;
		ICommand giveCommand = getGiveCommand(sender);
		if (giveCommand != null && giveCommand.checkPermission(minecraftServer, sender)) {
			String[] commandParameters = getGiveCommandParameters(sender, itemStack, itemStack.getCount());
			CommandEvent event = new CommandEvent(giveCommand, sender, commandParameters);
			if (MinecraftForge.EVENT_BUS.post(event)) {
				Throwable exception = event.getException();
				if (exception != null) {
					Throwables.propagateIfPossible(exception);
				}
				return false;
			}
			return true;
		} else {
			return sender.canUseCommand(minecraftServer.getOpPermissionLevel(), "give");
		}
	}

	/**
	 * Gives a player an item. Similar to vanilla but without the "fake" itemStack popping into the player's face.
	 * (no {@link EntityItem#makeFakeItem()}
	 *
	 * @see CommandGive#execute(MinecraftServer, ICommandSender, String[])
	 */
	public static void executeGive(EntityPlayerMP sender, ItemStack itemStack) {
		int count = itemStack.getCount();
		boolean addedToInventory = sender.inventory.addItemStackToInventory(itemStack);

		if (addedToInventory) {
			sender.world.playSound(null, sender.posX, sender.posY, sender.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((sender.getRNG().nextFloat() - sender.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			sender.inventoryContainer.detectAndSendChanges();
		}

		if (addedToInventory && itemStack.isEmpty()) {
			sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, count);
		} else {
			sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, count - itemStack.getCount());
			EntityItem entityitem = sender.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.setNoPickupDelay();
				entityitem.setOwner(sender.getName());
			}
		}

		ICommand giveCommand = getGiveCommand(sender);
		if (giveCommand != null) {
			itemStack.setCount(1);
			CommandBase.notifyCommandListener(sender, giveCommand, "commands.give.success", itemStack.getTextComponent(), count, sender.getName());
		}
	}

	@Nullable
	private static ICommand getGiveCommand(EntityPlayerMP sender) {
		MinecraftServer minecraftServer = sender.mcServer;
		ICommandManager commandManager = minecraftServer.getCommandManager();
		Map<String, ICommand> commands = commandManager.getCommands();
		return commands.get("give");
	}
}
