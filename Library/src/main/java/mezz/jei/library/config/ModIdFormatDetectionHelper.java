package mezz.jei.library.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;

import java.util.List;

final class ModIdFormatDetectionHelper {
	private static final Logger LOGGER = LogManager.getLogger();

	private ModIdFormatDetectionHelper() {
	}

	public static Component detectModNameTooltipFormatting() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		List<Component> tooltip = getTestTooltip(player, new ItemStack(Items.APPLE));
		return ModIdFormatConfig.detectModNameTooltipFormatting(tooltip);
	}

	private static List<Component> getTestTooltip(@Nullable Player player, ItemStack itemStack) {
		try {
			return itemStack.getTooltipLines(Item.TooltipContext.EMPTY, player, TooltipFlag.Default.NORMAL);
		} catch (LinkageError | RuntimeException e) {
			LOGGER.error("Error while Testing for mod name formatting", e);
		}
		return List.of();
	}
}
