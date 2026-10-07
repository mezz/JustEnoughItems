package mezz.jei.common.util;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientCookingFuelRegistryProviderTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
	}

	@Test
	void includesClientWorldEntriesMissingFromConnection() {
		HolderLookup.Provider olderServerRegistries = createOlderServerRegistries();
		assertTrue(olderServerRegistries.get(Biomes.DAPPLED_FOREST).isEmpty());

		HolderLookup.Provider registryProvider = ClientCookingFuelRegistryProvider.create(olderServerRegistries);

		assertTrue(registryProvider.get(Biomes.DAPPLED_FOREST).isPresent());
	}

	@Test
	void suppliesFuelProvidersWithoutGeneratingUnrelatedReloadableData() {
		HolderLookup.Provider connectionRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

		HolderLookup.Provider registryProvider = ClientCookingFuelRegistryProvider.create(connectionRegistries);

		assertTrue(registryProvider.get(ContextIntProviders.COOKING_TIME_COAL).isPresent());
		assertTrue(registryProvider.get(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER).isPresent());
		assertTrue(registryProvider.get(LootPredicates.FAST_FURNACE).isPresent());
		assertFalse(registryProvider.lookup(Registries.LOOT_TABLE).isPresent());
		assertFalse(registryProvider.lookup(Registries.ADVANCEMENT).isPresent());
		assertFalse(registryProvider.lookup(Registries.RECIPE).isPresent());
	}

	@Test
	void retainsConnectionRegistriesOutsideFuelFallback() {
		var lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("test", "connection_loot"));
		HolderLookup.Provider connectionRegistries = new RegistrySetBuilder()
			.add(Registries.LOOT_TABLE, context -> context.register(lootTableKey, LootTable.EMPTY))
			.build(VanillaRegistries.createWorldLookup());

		HolderLookup.Provider registryProvider = ClientCookingFuelRegistryProvider.create(connectionRegistries);

		assertSame(LootTable.EMPTY, registryProvider.get(lootTableKey).orElseThrow().value());
	}

	private static HolderLookup.Provider createOlderServerRegistries() {
		HolderLookup.Provider clientRegistries = VanillaRegistries.createWorldLookup();
		HolderLookup.RegistryLookup<Biome> clientBiomes = clientRegistries.lookupOrThrow(Registries.BIOME);
		HolderLookup.RegistryLookup<Biome> olderServerBiomes = new HolderLookup.RegistryLookup.Delegate<>() {
			@Override
			public HolderLookup.RegistryLookup<Biome> parent() {
				return clientBiomes;
			}

			@Override
			public Optional<Holder.Reference<Biome>> get(ResourceKey<Biome> key) {
				if (key.equals(Biomes.DAPPLED_FOREST)) {
					return Optional.empty();
				}
				return parent().get(key);
			}

			@Override
			public Stream<Holder.Reference<Biome>> listElements() {
				return parent().listElements()
					.filter(holder -> !holder.is(Biomes.DAPPLED_FOREST));
			}
		};

		Map<ResourceKey<? extends Registry<?>>, HolderLookup.RegistryLookup<?>> lookups = new HashMap<>();
		clientRegistries.listRegistries()
			.forEach(lookup -> lookups.put(lookup.key(), lookup));
		lookups.put(Registries.BIOME, olderServerBiomes);
		return HolderLookup.Provider.create(lookups.values().stream());
	}
}
