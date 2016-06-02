package mezz.jei.network.packets;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Map;

import mezz.jei.network.IPacketId;
import mezz.jei.network.PacketIdServer;
import net.minecraft.command.CommandGive;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.server.FMLServerHandler;

public class PacketGiveItemStack extends PacketJEI {
	private ItemStack itemStack;

	public PacketGiveItemStack() {

	}

	public PacketGiveItemStack(@Nonnull ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@Override
	public IPacketId getPacketId() {
		return PacketIdServer.GIVE_BIG;
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeItemStackToBuffer(itemStack);
	}

	@Override
	public void readPacketData(PacketBuffer buf, EntityPlayer player) throws IOException {
		if (player instanceof EntityPlayerMP) {
			EntityPlayerMP sender = (EntityPlayerMP) player;

			MinecraftServer minecraftServer = sender.mcServer;
			ICommandManager commandManager = minecraftServer.getCommandManager();
			Map<String, ICommand> commands = commandManager.getCommands();
			ICommand giveCommand = commands.get("give");
			if (giveCommand != null && giveCommand.checkPermission(minecraftServer, sender)) {
				ItemStack itemStack = buf.readItemStackFromBuffer();
				executeGive(sender, itemStack);
			} else {
				TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation("commands.generic.permission");
				textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
				sender.addChatMessage(textcomponenttranslation1);
			}
		}
	}

	public void executeGive(EntityPlayer entityplayer, ItemStack itemStack) {
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
