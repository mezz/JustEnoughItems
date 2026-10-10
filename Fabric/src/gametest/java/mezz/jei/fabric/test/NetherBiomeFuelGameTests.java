package mezz.jei.fabric.test;

import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ClientCookingFuelRegistryProvider;
import mezz.jei.common.util.RegistryUtil;
import net.fabricmc.fabric.api.biome.v1.NetherBiomes;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NetherBiomeFuelGameTests {
	@GameTest
	public void fuelLookupRetainsDatapackNetherBiomes(GameTestHelper helper) {
		helper.assertTrue(NetherBiomes.canGenerateInNether(NetherBiomeTestMod.TEST_NETHER), "Expected the test biome in Fabric's Nether preset");
		RegistryAccess connectionRegistries = createClientRegistries(helper.getLevel().registryAccess());
		var biome = connectionRegistries.lookupOrThrow(Registries.BIOME).getOrThrow(NetherBiomeTestMod.TEST_NETHER);
		RegistryUtil.setRegistryAccess(connectionRegistries);
		try {
			var fuelRegistries = ClientCookingFuelRegistryProvider.get();
			helper.assertTrue(
				fuelRegistries.get(NetherBiomeTestMod.TEST_NETHER).orElseThrow().value() == biome.value(),
				"Expected the fuel lookup to retain the connection's Nether biome"
			);
			helper.assertFalse(
				fuelRegistries.lookup(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).isPresent(),
				"Expected fuel lookup to omit world generation registries"
			);
			ItemStack coal = new ItemStack(Items.COAL);
			assertFuel(helper, coal, RecipeType.SMELTING, 1600, 1);
			assertFuel(helper, coal, RecipeType.BLASTING, 800, 2);
			assertFuel(helper, coal, RecipeType.SMOKING, 800, 2);
		} finally {
			RegistryUtil.setRegistryAccess(null);
		}
		helper.succeed();
	}

	private static RegistryAccess createClientRegistries(RegistryAccess worldRegistries) {
		var syncedRegistryKeys = RegistryDataLoader.SYNCHRONIZED_REGISTRIES.stream()
			.map(RegistryDataLoader.RegistryData::key)
			.collect(Collectors.toSet());
		return new RegistryAccess.ImmutableRegistryAccess(Stream.concat(
			RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).registries(),
			worldRegistries.registries().filter(entry -> syncedRegistryKeys.contains(entry.key()))
		));
	}

	private static void assertFuel(GameTestHelper helper, ItemStack coal, RecipeType<?> recipeType, int burnTime, float speed) {
		var fuel = Services.PLATFORM.getItemStackHelper().getFuelProperties(coal, recipeType);
		helper.assertTrue(fuel.burnTime() == burnTime, "Expected coal burn time " + burnTime + " for " + recipeType);
		helper.assertTrue(fuel.speedMultiplier() == speed, "Expected coal cooking speed " + speed + " for " + recipeType);
	}
}
