package mezz.jei.fabric.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.util.QuadUtil;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FabricLimitedQuadItemModel extends ForwardingBakedModel {
	public static BakedModel wrap(BakedModel model) {
		if (!model.isVanillaAdapter() || model instanceof FabricLimitedQuadItemModel) {
			return model;
		}
		return new FabricLimitedQuadItemModel(model);
	}

	private @Nullable List<BakedQuad> quads;

	private FabricLimitedQuadItemModel(BakedModel model) {
		this.wrapped = model;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState blockState, @Nullable Direction face, RandomSource rand) {
		if (face == null) {
			if (quads == null) {
				List<BakedQuad> originalQuads = wrapped.getQuads(blockState, null, rand);

				PoseStack poseStack = new PoseStack();

				ItemTransform guiTransform = wrapped.getTransforms().getTransform(ItemDisplayContext.GUI);
				guiTransform.apply(false, poseStack);

				quads = QuadUtil.getQuadsFacingDirection(originalQuads, poseStack, Direction.SOUTH);
				if (quads.isEmpty()) {
					quads = originalQuads;
				}
			}
			return quads;
		}
		return List.of();
	}
}
