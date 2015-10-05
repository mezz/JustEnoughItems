package mezz.jei.util;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.ServerConfigurationManager;

import net.minecraftforge.fml.common.FMLCommonHandler;

public class Permissions {
	public static boolean canPlayerSpawnItems(@Nonnull EntityPlayer player) {
		ServerConfigurationManager configurationManager = FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager();
		return configurationManager.canSendCommands(player.getGameProfile());
	}
}
