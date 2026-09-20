package mezz.jei.common.util;

import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistryUtilTest {
	@Test
	void createRegistryProviderUsesCompleteClientWorldRegistries() {
		SharedConstants.setVersion(DetectedVersion.BUILT_IN);
		Bootstrap.bootStrap();
		HolderLookup.Provider olderServerRegistries = createOlderServerRegistries();
		assertTrue(olderServerRegistries.get(Biomes.DAPPLED_FOREST).isEmpty());

		HolderLookup.Provider registryProvider = RegistryUtil.createRegistryProvider(olderServerRegistries);

		assertTrue(registryProvider.get(Biomes.DAPPLED_FOREST).isPresent());
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
