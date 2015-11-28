package mezz.jei;

import javax.annotation.Nonnull;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import mezz.jei.api.JEIManager;
import mezz.jei.config.Config;

public class TooltipEventHandler {
	private static final String chatFormatting = EnumChatFormatting.BLUE.toString() + EnumChatFormatting.ITALIC.toString();

	@SubscribeEvent
	public void onToolTip(@Nonnull ItemTooltipEvent event) {
		if (!Config.tooltipModNameEnabled) {
			return;
		}

		ItemStack itemStack = event.itemStack;
		if (itemStack == null) {
			return;
		}

		Item item = itemStack.getItem();
		if (item == null) {
			return;
		}

		String modName = JEIManager.itemRegistry.getModNameForItem(item);
		event.toolTip.add(chatFormatting + modName);
	}
}
