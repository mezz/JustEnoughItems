package mezz.jei.common.platform;

import net.minecraft.server.MinecraftServer;

import java.util.Optional;

public interface IPlatformWorldHelper {
	Optional<String> getLevelId(MinecraftServer server);
}
