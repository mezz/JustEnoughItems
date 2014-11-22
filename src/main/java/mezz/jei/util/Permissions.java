package mezz.jei.util;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;

public class Permissions {
	public static boolean canPlayerSpawnItems(@Nonnull EntityPlayer player) {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().func_152596_g(player.getGameProfile());
	}
}
