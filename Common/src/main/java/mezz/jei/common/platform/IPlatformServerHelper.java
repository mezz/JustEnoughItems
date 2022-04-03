package mezz.jei.common.platform;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public interface IPlatformServerHelper {
    @Nullable
    MinecraftServer getServer();
}
