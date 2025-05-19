package mezz.jei.common.util;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

public final class QuadUtil {
	private QuadUtil() {}

	public static List<BakedQuad> getQuadsFacingDirection(List<BakedQuad> quads, PoseStack poseStack, Direction facing) {
		Matrix4f pose = poseStack.last().pose();

		Direction.Axis axis = facing.getAxis();
		float facingStep = facing.getAxisDirection().getStep();

		return quads
			.stream()
			.filter(q -> {
				Direction quadDirection = q.getDirection();
				Vector3f transformedDirection = pose.transformDirection(quadDirection.step());
				double value = axis.choose(transformedDirection.x, transformedDirection.y, transformedDirection.z);
				if (facingStep > 0) {
					return value > 0;
				} else {
					return value < 0;
				}
			})
			.toList();
	}
}
