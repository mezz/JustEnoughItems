package mezz.jei.common.gui.textures;

import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class JeiAtlasManager implements PreparableReloadListener, AutoCloseable {
	public static final PreparableReloadListener.StateKey<PendingStitchResults> PENDING_STITCH = new PreparableReloadListener.StateKey<>();
	private final AtlasEntry atlasEntry;

	public JeiAtlasManager(TextureManager textureManager, Config config) {
		TextureAtlas atlas = new TextureAtlas(config.textureId);
		textureManager.register(config.textureId, atlas);
		this.atlasEntry = new AtlasEntry(atlas, config);
	}

	public TextureAtlas getAtlas() {
		return atlasEntry.atlas;
	}

	@Override
	public void close() {
		this.atlasEntry.close();
	}

	@Override
	public void prepareSharedState(PreparableReloadListener.SharedState state) {
		CompletableFuture<SpriteLoader.Preparations> preparations = new CompletableFuture<>();
		PendingStitch pendingStitch = new PendingStitch(atlasEntry, preparations);
		CompletableFuture<?> readyToUpload = preparations.thenCompose(SpriteLoader.Preparations::readyForUpload);

		state.set(PENDING_STITCH, new PendingStitchResults(pendingStitch, readyToUpload));
	}

	@Override
	public CompletableFuture<Void> reload(
		PreparableReloadListener.SharedState state,
		Executor loadAndStitchExecutor,
		PreparableReloadListener.PreparationBarrier preparationBarrier,
		Executor joinAndUploadExecutor
	) {
		PendingStitchResults pendingStitchResults = state.get(PENDING_STITCH);
		ResourceManager resourcemanager = state.resourceManager();

		PendingStitch pendingStitch = pendingStitchResults.pendingStitch;
		pendingStitch.entry.scheduleLoad(resourcemanager, loadAndStitchExecutor)
			.whenComplete((preparations, throwable) -> {
				if (preparations != null) {
					pendingStitch.preparations.complete(preparations);
				} else {
					pendingStitch.preparations.completeExceptionally(throwable);
				}
			});
		return pendingStitchResults.readyToUpload
			.thenCompose(preparationBarrier::wait)
			.thenAcceptAsync(ignored -> pendingStitchResults.joinAndUpload(), joinAndUploadExecutor);
	}

	public record Config(
		Identifier textureId,
		Identifier definitionLocation,
		Set<MetadataSectionType<?>> additionalMetadata
	) {}

	record AtlasEntry(TextureAtlas atlas, Config config) implements AutoCloseable {
		@Override
		public void close() {
			this.atlas.clearTextureData();
		}

		CompletableFuture<SpriteLoader.Preparations> scheduleLoad(ResourceManager resourceManager, Executor executor) {
			return SpriteLoader.create(this.atlas)
				.loadAndStitch(resourceManager, this.config.definitionLocation, 0, executor, this.config.additionalMetadata);
		}
	}

	record PendingStitch(AtlasEntry entry, CompletableFuture<SpriteLoader.Preparations> preparations) {
		public void joinAndUpload() {
			SpriteLoader.Preparations preparations = this.preparations.join();
			this.entry.atlas.upload(preparations);
		}
	}

	public static class PendingStitchResults {
		private final PendingStitch pendingStitch;
		private final CompletableFuture<?> readyToUpload;

		PendingStitchResults(
			PendingStitch pendingStitch,
			CompletableFuture<?> readyToUpload
		) {
			this.pendingStitch = pendingStitch;
			this.readyToUpload = readyToUpload;
		}

		public void joinAndUpload() {
			pendingStitch.joinAndUpload();
		}
	}
}
