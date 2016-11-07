package mezz.jei.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Throwables;
import mezz.jei.config.Config;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
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
public class CommandUtil {
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
		player.addChatMessage(component);
	}

	public static boolean hasPermission(EntityPlayerMP sender, ItemStack itemStack) {
		if (sender.isCreative()) {
			return true;
		}

		MinecraftServer minecraftServer = sender.mcServer;
		ICommandManager commandManager = minecraftServer.getCommandManager();
		Map<String, ICommand> commands = commandManager.getCommands();
		ICommand giveCommand = commands.get("give");
		if (giveCommand != null && giveCommand.checkPermission(minecraftServer, sender)) {
			String[] commandParameters = getGiveCommandParameters(sender, itemStack, itemStack.stackSize);
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
			return sender.canCommandSenderUseCommand(minecraftServer.getOpPermissionLevel(), "give");
		}
	}

	public static void executeGive(EntityPlayer entityplayer, ItemStack itemStack) {
		boolean addedToInventory = entityplayer.inventory.addItemStackToInventory(itemStack);

		if (addedToInventory) {
			entityplayer.worldObj.playSound(null, entityplayer.posX, entityplayer.posY, entityplayer.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((entityplayer.getRNG().nextFloat() - entityplayer.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
			entityplayer.inventoryContainer.detectAndSendChanges();
		}

		if (!addedToInventory || itemStack.stackSize > 0) {
			EntityItem entityitem = entityplayer.dropItem(itemStack, false);
			if (entityitem != null) {
				entityitem.setNoPickupDelay();
				entityitem.setOwner(entityplayer.getName());
			}
		}
	}
}
