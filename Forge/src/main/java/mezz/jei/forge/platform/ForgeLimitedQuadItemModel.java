package mezz.jei.forge.platform;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;
import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ForgeLimitedQuadItemModel extends BakedModelWrapper<BakedModel> {
	public static BakedModel wrap(BakedModel model) {
		if (model instanceof IDynamicBakedModel || model instanceof ForgeLimitedQuadItemModel) {
			return model;
		}
		return new ForgeLimitedQuadItemModel(model);
	}

	private @Nullable List<BakedQuad> quads;

	private ForgeLimitedQuadItemModel(BakedModel originalModel) {
		super(originalModel);
	}

	@Override
	@Deprecated
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random) {
		if (direction == null) {
			if (quads == null) {
				quads = originalModel.getQuads(blockState, null, random)
					.stream()
					.filter(q -> q.getDirection() == Direction.SOUTH)
					.toList();
			}
			return quads;
		}
		return List.of();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, Random random, IModelData extraData) {
		if (direction == null) {
			if (quads == null) {
				quads = originalModel.getQuads(blockState, null, random, extraData)
					.stream()
					.filter(q -> q.getDirection() == Direction.SOUTH)
					.toList();
			}
			return quads;
		}
		return List.of();
	}

	@Override
	public BakedModel handlePerspective(ItemTransforms.TransformType cameraTransformType, PoseStack poseStack) {
		BakedModel model = super.handlePerspective(cameraTransformType, poseStack);
		if (model == this.originalModel) {
			return this;
		}
		return model;
	}

	@Override
	public List<Pair<BakedModel, net.minecraft.client.renderer.RenderType>> getLayerModels(ItemStack itemStack, boolean fabulous) {
		List<Pair<BakedModel, net.minecraft.client.renderer.RenderType>> layers = super.getLayerModels(itemStack, fabulous);
		List<Pair<BakedModel, net.minecraft.client.renderer.RenderType>> result = new ArrayList<>(layers.size());
		for (Pair<BakedModel, net.minecraft.client.renderer.RenderType> layer : layers) {
			BakedModel bakedModel = layer.getFirst();
			if (bakedModel == this.originalModel) {
				bakedModel = this;
			}
			result.add(Pair.of(bakedModel, layer.getSecond()));
		}
		return result;
	}
}
