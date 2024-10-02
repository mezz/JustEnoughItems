package mezz.jei.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RegistryUtil {
	private static final Map<ResourceKey<? extends Registry<?>>, Registry<?>> REGISTRY_CACHE = new HashMap<>();
	private static @Nullable RegistryAccess REGISTRY_ACCESS;

	public static <T> Registry<T> getRegistry(ResourceKey<? extends Registry<T>> key) {
		Registry<?> registry = REGISTRY_CACHE.get(key);
		if (registry == null) {
			registry = getRegistryUncached(key);
			REGISTRY_CACHE.put(key, registry);
		}
		@SuppressWarnings("unchecked")
		Registry<T> castRegistry = (Registry<T>) registry;
		return castRegistry;
	}

	private static Registry<?> getRegistryUncached(ResourceKey<? extends Registry<?>> key) {
		RegistryAccess registryAccess = getRegistryAccess();
		return registryAccess.registryOrThrow(key);
	}

	public static RegistryAccess getRegistryAccess() {
		if (REGISTRY_ACCESS == null) {
			Minecraft minecraft = Minecraft.getInstance();
			ClientLevel level = minecraft.level;
			if (level == null) {
				throw new IllegalStateException("Could not get registry, registry access is unavailable because the level is currently null");
			}
			REGISTRY_ACCESS = level.registryAccess();
		}
		return REGISTRY_ACCESS;
	}

	public static void setRegistryAccess(@Nullable RegistryAccess registryAccess) {
		REGISTRY_ACCESS = registryAccess;
		REGISTRY_CACHE.clear();
	}
}
