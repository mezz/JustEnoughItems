package mezz.jei.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry lookup for evaluating cooking fuel on the client.
 * <p>
 * Minecraft does not sync fuel's context number providers or predicates to clients.
 * This lookup uses vanilla definitions and world registries in place of matching connection
 * data, so it may differ from server datapacks.
 */
public final class ClientCookingFuelRegistryProvider {
	private static HolderLookup.@Nullable Provider cachedProvider;

	private ClientCookingFuelRegistryProvider() {
	}

	/**
	 * Returns the cooking fuel lookup for the current client connection.
	 */
	public static HolderLookup.Provider get() {
		if (cachedProvider == null) {
			cachedProvider = create(RegistryUtil.getRegistryAccess());
		}
		return cachedProvider;
	}

	static HolderLookup.Provider create(HolderLookup.Provider connectionRegistries) {
		Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> lookups = new HashMap<>();
		connectionRegistries.listRegistries()
			.forEach(lookup -> lookups.put(lookup.key(), lookup));
		// The client may need entries that an older server does not have.
		VanillaRegistries.createWorldLookup().listRegistries()
			.forEach(lookup -> lookups.put(lookup.key(), lookup));
		HolderLookup.Provider context = HolderLookup.Provider.create(lookups.values().stream());
		return new RegistrySetBuilder()
			.add(Registries.PREDICATE, LootPredicates::bootstrap)
			.add(Registries.CONTEXT_INT_PROVIDER, ContextIntProviders::bootstrap)
			.add(Registries.CONTEXT_FLOAT_PROVIDER, ContextFloatProviders::bootstrap)
			.build(context);
	}

	static void clearCache() {
		cachedProvider = null;
	}

	/**
	 * Overrides the cooking fuel lookup for tests.
	 *
	 * @param registryProvider replacement lookup, or {@code null} to restore the default lookup
	 */
	@TestOnly
	public static void setProviderForTests(HolderLookup.@Nullable Provider registryProvider) {
		cachedProvider = registryProvider;
	}
}
