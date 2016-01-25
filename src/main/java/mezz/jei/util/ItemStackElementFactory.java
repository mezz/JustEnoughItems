package mezz.jei.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemStackElementFactory {
	private final List<ItemStackElement> needsTooltip = new ArrayList<>();

	public ItemStackElementFactory() {
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Nullable
	public ItemStackElement create(@Nonnull ItemStack itemStack) {
		ItemStackElement itemStackElement;
		try {
			itemStackElement = new ItemStackElement(itemStack);
		} catch (RuntimeException e) {
			Log.warning("Found broken itemStack.", e);
			return null;
		}

		synchronized(this) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null) {
				itemStackElement.setTooltip(player);
			} else {
				needsTooltip.add(itemStackElement);
			}
		}

		return itemStackElement;
	}

	@SubscribeEvent
	public void onEntityJoinedWorld(EntityJoinWorldEvent event) {
		synchronized(this) {
			EntityPlayer player = Minecraft.getMinecraft().thePlayer;
			if (player != null) {
				for (ItemStackElement itemStackElement : needsTooltip) {
					itemStackElement.setTooltip(player);
				}
				needsTooltip.clear();
				MinecraftForge.EVENT_BUS.unregister(this);
			}
		}
	}
}
