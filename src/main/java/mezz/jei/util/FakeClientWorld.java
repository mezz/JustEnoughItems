package mezz.jei.util;

import javax.annotation.Nullable;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveDataMemoryStorage;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

public class FakeClientWorld extends World {
	private static final WorldSettings worldSettings = new WorldSettings(0, GameType.SURVIVAL, false, false, WorldType.DEFAULT);
	private static final WorldInfo worldInfo = new WorldInfo(worldSettings, "jei_fake");
	private static final ISaveHandler saveHandler = new SaveHandlerMP();
	private static final WorldProvider worldProvider = new WorldProvider() {
		@Override
		public DimensionType getDimensionType() {
			return DimensionType.OVERWORLD;
		}
	};
	@Nullable
	private static FakeClientWorld INSTANCE;

	public static FakeClientWorld getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new FakeClientWorld();
		}
		return INSTANCE;
	}

	private FakeClientWorld() {
		super(saveHandler, worldInfo, worldProvider, new Profiler(), true);
		this.provider.registerWorld(this);
		this.mapStorage = new SaveDataMemoryStorage();
	}

	@Override
	public BlockPos getSpawnPoint() {
		return new BlockPos(0, 0, 0);
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return new IChunkProvider() {
			@Nullable
			@Override
			public Chunk getLoadedChunk(int x, int z) {
				return new EmptyChunk(FakeClientWorld.this, x, z);
			}

			@Override
			public Chunk provideChunk(int x, int z) {
				return new EmptyChunk(FakeClientWorld.this, x, z);
			}

			@Override
			public boolean unloadQueuedChunks() {
				return false;
			}

			@Override
			public String makeString() {
				return "";
			}
		};
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return false;
	}
}
