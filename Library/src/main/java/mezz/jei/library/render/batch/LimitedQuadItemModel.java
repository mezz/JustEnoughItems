package mezz.jei.library.render.batch;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LimitedQuadItemModel implements BakedModel {
	private final BakedModel bakedmodel;
	private @Nullable List<BakedQuad> quads;

	public LimitedQuadItemModel(BakedModel bakedmodel) {
		this.bakedmodel = bakedmodel;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		if (direction == null) {
			if (quads == null) {
				quads = bakedmodel.getQuads(blockState, null, randomSource)
					.stream()
					.filter(q -> q.getDirection() == Direction.SOUTH)
					.toList();
			}
			return quads;
		}
		return List.of();
	}

	@Override
	public boolean useAmbientOcclusion() {
		return bakedmodel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return bakedmodel.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return bakedmodel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return bakedmodel.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return bakedmodel.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return bakedmodel.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return bakedmodel.getOverrides();
	}
}
