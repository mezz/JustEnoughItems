package mezz.jei.library.config;

import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

final class ModIdFormatDetectionHelper {
	private ModIdFormatDetectionHelper() {
	}

	public static Component detectModNameTooltipFormatting() {
		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		return ModIdFormatConfig.detectModNameTooltipFormatting(itemStackHelper, player);
	}
}
