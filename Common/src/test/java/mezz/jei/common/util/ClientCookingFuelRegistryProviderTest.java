package mezz.jei.common.util;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootPredicates;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;
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
	void retainsModdedConnectionBiomes() {
		HolderLookup.Provider vanillaConnection = createConnectionRegistries();
		var biome = vanillaConnection.get(Biomes.NETHER_WASTES).orElseThrow().value();
		var biomeKey = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath("test", "nether_biome"));
		var moddedBiomes = new RegistrySetBuilder()
			.add(Registries.BIOME, context -> context.register(biomeKey, biome))
			.build(RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY))
			.lookupOrThrow(Registries.BIOME);
		HolderLookup.Provider connectionRegistries = HolderLookup.Provider.create(Stream.concat(
			vanillaConnection.listRegistries().filter(lookup -> !lookup.key().equals(Registries.BIOME)),
			Stream.of(moddedBiomes)
		));

		HolderLookup.Provider registryProvider = ClientCookingFuelRegistryProvider.create(connectionRegistries);

		assertSame(biome, registryProvider.get(biomeKey).orElseThrow().value());
	}

	@Test
	void suppliesFuelProvidersWithoutGeneratingWorldOrReloadableData() {
		HolderLookup.Provider connectionRegistries = createConnectionRegistries();

		HolderLookup.Provider registryProvider = ClientCookingFuelRegistryProvider.create(connectionRegistries);

		assertTrue(registryProvider.get(ContextIntProviders.COOKING_TIME_COAL).isPresent());
		assertTrue(registryProvider.get(ContextFloatProviders.COOKING_DEFAULT_SPEED_MULTIPLIER).isPresent());
		assertTrue(registryProvider.get(LootPredicates.FAST_FURNACE).isPresent());
		assertFalse(registryProvider.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).isPresent());
		assertFalse(registryProvider.lookup(Registries.NOISE_SETTINGS).isPresent());
		assertFalse(registryProvider.lookup(Registries.LOOT_TABLE).isPresent());
		assertFalse(registryProvider.lookup(Registries.ADVANCEMENT).isPresent());
		assertFalse(registryProvider.lookup(Registries.RECIPE).isPresent());
	}

	@Test
	void retainsConnectionRegistriesOutsideFuelFallback() {
		var lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("test", "connection_loot"));
		HolderLookup.Provider connectionRegistries = new RegistrySetBuilder()
			.add(Registries.LOOT_TABLE, context -> context.register(lootTableKey, LootTable.EMPTY))
			.build(createConnectionRegistries());

		HolderLookup.Provider registryProvider = ClientCookingFuelRegistryProvider.create(connectionRegistries);

		assertSame(LootTable.EMPTY, registryProvider.get(lootTableKey).orElseThrow().value());
	}

	private static HolderLookup.Provider createConnectionRegistries() {
		HolderLookup.Provider staticRegistries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
		HolderLookup.Provider worldRegistries = VanillaRegistries.createWorldLookup();
		var syncedRegistryKeys = RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream()
			.map(RegistryDataLoader.RegistryData::key)
			.collect(Collectors.toSet());
		return HolderLookup.Provider.create(Stream.concat(
			staticRegistries.listRegistries(),
			worldRegistries.listRegistries().filter(lookup -> syncedRegistryKeys.contains(lookup.key()))
		));
	}
}
