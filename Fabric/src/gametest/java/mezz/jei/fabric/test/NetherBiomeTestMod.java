package mezz.jei.fabric.test;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.NetherBiomes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;

/**
 * Registers the datapack Nether biome used by server game tests.
 */
public class NetherBiomeTestMod implements ModInitializer {
	public static final ResourceKey<Biome> TEST_NETHER = ResourceKey.create(
		Registries.BIOME,
		Identifier.fromNamespaceAndPath("jei-test", "test_nether")
	);

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			NetherBiomes.addNetherBiome(TEST_NETHER, Climate.parameters(0, 0.5f, 0, 0, 0, 0, 0));
		}
	}
}
