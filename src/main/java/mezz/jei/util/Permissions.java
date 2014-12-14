package mezz.jei.util;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.FMLCommonHandler;

public class Permissions {
	public static boolean canPlayerSpawnItems(@Nonnull EntityPlayer player) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152596_g(player.getGameProfile());
	}
}
