package mezz.jei.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RegistryUtil {
	private static final Map<ResourceKey<? extends Registry<?>>, Registry<?>> REGISTRY_CACHE = new HashMap<>();
	private static @Nullable RegistryAccess REGISTRY_ACCESS;
	private static HolderLookup.@Nullable Provider REGISTRY_PROVIDER;

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
		return registryAccess.lookupOrThrow(key);
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

	/**
	 * Returns a registry provider that includes reloadable registries used by loot context providers.
	 * The client level does not receive these registries from the server, so vanilla defaults are used as a fallback.
	 */
	public static HolderLookup.Provider getRegistryProvider() {
		if (REGISTRY_PROVIDER == null) {
			Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> lookups = new HashMap<>();
			// Loot tables also reference world-generation registries that are not synced to the client.
			VanillaRegistries.createWorldLookup().listRegistries()
				.forEach(lookup -> lookups.put(lookup.key(), lookup));
			getRegistryAccess().listRegistries()
				.forEach(lookup -> lookups.put(lookup.key(), lookup));
			HolderLookup.Provider context = HolderLookup.Provider.create(lookups.values().stream());
			REGISTRY_PROVIDER = VanillaRegistries.createReloadableLookup(context);
		}
		return REGISTRY_PROVIDER;
	}

	public static void setRegistryAccess(@Nullable RegistryAccess registryAccess) {
		REGISTRY_ACCESS = registryAccess;
		REGISTRY_PROVIDER = null;
		REGISTRY_CACHE.clear();
	}

	public static void setRegistryProvider(HolderLookup.@Nullable Provider registryProvider) {
		REGISTRY_PROVIDER = registryProvider;
	}
}
