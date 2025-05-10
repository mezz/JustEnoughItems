package mezz.jei.neoforge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.common.util.QuadUtil;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class NeoForgeLimitedQuadItemModel extends BakedModelWrapper<BakedModel> {
	public static BakedModel wrap(BakedModel model) {
		if (model instanceof IDynamicBakedModel || model instanceof NeoForgeLimitedQuadItemModel) {
			return model;
		}
		return new NeoForgeLimitedQuadItemModel(model);
	}

	private @Nullable List<BakedQuad> quads;

	private NeoForgeLimitedQuadItemModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	@Deprecated
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
		if (direction == null) {
			if (quads == null) {
				List<BakedQuad> originalQuads = originalModel.getQuads(blockState, null, randomSource);

				PoseStack poseStack = new PoseStack();

				BakedModel bakedModel = originalModel.applyTransform(ItemDisplayContext.GUI, poseStack, false);
				if (bakedModel != originalModel) {
					// this shouldn't be possible because we don't wrap IDynamicBakedModels
					// we can't easily understand which way the quads will face in this case, so don't cull or cache any quads
					return originalQuads;
				}

				quads = QuadUtil.getQuadsFacingDirection(originalQuads, poseStack, Direction.SOUTH);
				if (quads.isEmpty()) {
					quads = originalQuads;
				}
			}
			return quads;
		}
		return originalModel.getQuads(blockState, direction, randomSource);
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource, ModelData extraData, @Nullable RenderType renderType) {
		if (direction == null) {
			if (quads == null) {
				List<BakedQuad> originalQuads = originalModel.getQuads(blockState, null, randomSource, extraData, renderType);

				PoseStack poseStack = new PoseStack();

				BakedModel bakedModel = originalModel.applyTransform(ItemDisplayContext.GUI, poseStack, false);
				if (bakedModel != originalModel) {
					// this shouldn't be possible because we don't wrap IDynamicBakedModels
					// we can't easily understand which way the quads will face in this case, so don't cull or cache any quads
					return originalQuads;
				}

				quads = QuadUtil.getQuadsFacingDirection(originalQuads, poseStack, Direction.SOUTH);
				if (quads.isEmpty()) {
					quads = originalQuads;
				}
			}
			return quads;
		}
		return List.of();
	}

	@Override
	public BakedModel applyTransform(ItemDisplayContext cameraTransformType, PoseStack poseStack, boolean applyLeftHandTransform) {
		BakedModel model = super.applyTransform(cameraTransformType, poseStack, applyLeftHandTransform);
		if (model == this.originalModel) {
			return this;
		}
		return model;
	}

	@Override
	public List<BakedModel> getRenderPasses(ItemStack itemStack, boolean fabulous) {
		List<BakedModel> renderPasses = super.getRenderPasses(itemStack, fabulous);
		List<BakedModel> result = new ArrayList<>(renderPasses.size());
		for (BakedModel bakedModel : renderPasses) {
			if (bakedModel == this.originalModel) {
				bakedModel = this;
			}
			result.add(bakedModel);
		}
		return result;
	}
}
