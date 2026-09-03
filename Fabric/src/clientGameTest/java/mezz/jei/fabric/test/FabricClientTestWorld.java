package mezz.jei.fabric.test;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldOptions;
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
		return create(GameType.SURVIVAL);
	}

	public static FabricClientTestWorld create(GameType gameType) {
		String levelId = "jei-fabric-client-test-" + UUID.randomUUID();
		ClientTestUtil.runOnClient(client -> {
			LevelSettings levelSettings = new LevelSettings(
				"JEI Fabric Client Test",
				gameType,
				false,
				Difficulty.PEACEFUL,
				true,
				new GameRules(),
				WorldDataConfiguration.DEFAULT
			);
			client.createWorldOpenFlows()
				.createFreshLevel(
					levelId,
					levelSettings,
					new WorldOptions(0L, false, false),
					FabricClientTestWorld::createFastWorldDimensions,
					new TitleScreen()
				);
		}, WORLD_LOAD_TIMEOUT);
		ClientTestUtil.waitUntil(
			() -> ClientTestUtil.computeOnClient(client ->
				client.level != null &&
				client.player != null &&
				client.hasSingleplayerServer() &&
				client.levelRenderer.isSectionCompiled(client.player.blockPosition())
			),
			ASSERTION_TIMEOUT,
			() -> "Timed out rendering the integrated Fabric test world: " + levelId
		);
		return new FabricClientTestWorld(levelId);
	}

	private static WorldDimensions createFastWorldDimensions(RegistryAccess registryAccess) {
		return registryAccess.registryOrThrow(Registries.WORLD_PRESET)
			.getOrThrow(WorldPresets.FLAT)
			.createWorldDimensions();
	}

	@Override
	public void close() {
		ClientTestUtil.runOnClient(client -> {
			if (client.level != null) {
				client.level.disconnect();
				client.disconnect();
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
