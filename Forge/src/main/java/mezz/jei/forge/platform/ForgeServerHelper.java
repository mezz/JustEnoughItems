package mezz.jei.forge.platform;

import mezz.jei.common.platform.IPlatformServerHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

public class ForgeServerHelper implements IPlatformServerHelper {
    @Override
    @Nullable
    public MinecraftServer getServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }
}
