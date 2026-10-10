package mezz.jei.common.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.jetbrains.annotations.TestOnly;
import org.jspecify.annotations.Nullable;

/**
 * Registry lookup for evaluating cooking fuel on the client.
 * <p>
 * Minecraft does not sync fuel's context number providers or predicates to clients.
 * This lookup adds vanilla definitions to the connection's registries, so fuel values may
 * differ from server datapacks.
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
		return new RegistrySetBuilder()
			.add(Registries.PREDICATE, LootPredicates::bootstrap)
			.add(Registries.CONTEXT_INT_PROVIDER, ContextIntProviders::bootstrap)
			.add(Registries.CONTEXT_FLOAT_PROVIDER, ContextFloatProviders::bootstrap)
			.build(connectionRegistries);
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
