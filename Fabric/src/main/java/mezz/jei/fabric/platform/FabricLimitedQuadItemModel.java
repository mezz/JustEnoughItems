package mezz.jei.fabric.platform;

import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class FabricLimitedQuadItemModel extends ForwardingBakedModel {
	public static BakedModel wrap(BakedModel model) {
		FabricBakedModel fabricModel = (FabricBakedModel) model;
		if (!fabricModel.isVanillaAdapter() || model instanceof FabricLimitedQuadItemModel) {
			return model;
		}
		return new FabricLimitedQuadItemModel(model);
	}

	private @Nullable List<BakedQuad> quads;

	private FabricLimitedQuadItemModel(BakedModel model) {
		this.wrapped = model;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction face, Random rand) {
		if (face == null) {
			if (quads == null) {
				quads = wrapped.getQuads(blockState, null, rand)
					.stream()
					.filter(q -> q.getDirection() == Direction.SOUTH)
					.toList();
			}
			return quads;
		}
		return List.of();
	}
}
