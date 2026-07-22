package mezz.jei.fabric.test;

import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

final class FabricClientTestWorld implements AutoCloseable {
	private static final Duration ASSERTION_TIMEOUT = Duration.ofSeconds(60);
	private static final Duration WORLD_LOAD_TIMEOUT = Duration.ofSeconds(120);

	private final String levelId;

	private FabricClientTestWorld(String levelId) {
		this.levelId = levelId;
	}

	public static FabricClientTestWorld create() {
		String levelId = "jei-fabric-client-test-" + UUID.randomUUID();
		ClientTestUtil.runOnClient(client -> {
			LevelSettings levelSettings = new LevelSettings(
				"JEI Fabric Client Test",
				GameType.CREATIVE,
				false,
				Difficulty.NORMAL,
				true,
				new GameRules(),
				DataPackConfig.DEFAULT
			);
			RegistryAccess registryAccess = RegistryAccess.BUILTIN.get();
			WorldGenSettings worldGenSettings = WorldPresets.createNormalWorldFromPreset(registryAccess);
			client.createWorldOpenFlows()
				.createFreshLevel(levelId, levelSettings, registryAccess, worldGenSettings);
		}, WORLD_LOAD_TIMEOUT);
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> client.level != null && client.hasSingleplayerServer()),
			ASSERTION_TIMEOUT,
			() -> "Timed out creating integrated Fabric test world: " + levelId
		);
		return new FabricClientTestWorld(levelId);
	}

	@Override
	public void close() {
		ClientTestUtil.runOnClient(client -> {
			if (client.level != null) {
				client.level.disconnect();
				client.clearLevel();
			}
		}, WORLD_LOAD_TIMEOUT);
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client -> client.level == null && !client.hasSingleplayerServer()),
			ASSERTION_TIMEOUT,
			() -> "Timed out disconnecting from integrated Fabric test world: " + levelId
		);
		ClientTestUtil.runOnClient(client -> {
			try (LevelStorageSource.LevelStorageAccess access = client.getLevelSource().createAccess(levelId)) {
				access.deleteLevel();
			} catch (IOException e) {
				throw new AssertionError("Failed to delete integrated Fabric test world: " + levelId, e);
			}
			client.setScreen(new TitleScreen());
		}, WORLD_LOAD_TIMEOUT);
	}
}
