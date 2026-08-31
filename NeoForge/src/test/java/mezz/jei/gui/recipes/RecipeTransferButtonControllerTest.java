package mezz.jei.gui.recipes;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.transfer.RecipeTransferUtil;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeTransferButtonControllerTest {
	@Test
	public void restoredPoseKeepsMatrixChangesLocal() {
		// Setup: the GUI already has a translated pose before a transfer error draws its overlay.
		PoseStack poseStack = new PoseStack();
		poseStack.translate(4, 8, 0);
		Matrix4f originalPose = new Matrix4f(poseStack.last().pose());

		// Operation: run a transfer-error draw callback that mutates the pose stack.
		RecipeTransferButtonController.runWithRestoredPose(
			poseStack,
			() -> poseStack.translate(16, 32, 0)
		);

		// Assertions: the callback's matrix changes are popped before JEI returns to the caller.
		assertEquals(originalPose, new Matrix4f(poseStack.last().pose()));
	}

	@Test
	public void restoredPosePopsAfterTransferErrorThrows() {
		// Setup: the GUI already has a translated pose before a bad plugin transfer error draws.
		PoseStack poseStack = new PoseStack();
		poseStack.translate(4, 8, 0);
		Matrix4f originalPose = new Matrix4f(poseStack.last().pose());

		// Operation: run a transfer-error draw callback that mutates the pose stack and then crashes.
		IllegalStateException thrown = assertThrows(
			IllegalStateException.class,
			() -> RecipeTransferButtonController.runWithRestoredPose(
				poseStack,
				() -> {
					poseStack.translate(16, 32, 0);
					throw new IllegalStateException("bad transfer error renderer");
				}
			)
		);

		// Assertions: even a throwing plugin cannot leak its matrix changes into later JEI rendering.
		assertEquals("bad transfer error renderer", thrown.getMessage());
		assertEquals(originalPose, new Matrix4f(poseStack.last().pose()));
	}

	@Test
	public void cosmeticTransferErrorsLeaveButtonActiveAndVisible() {
		// Setup: a cosmetic transfer error warns the user but still permits transfer.
		TestButtonState state = new TestButtonState();
		IRecipeTransferError error = new TestTransferError(IRecipeTransferError.Type.COSMETIC, Component.literal("Warning"), 2);

		// Operation: apply the transfer-error state to the button.
		RecipeTransferButtonController.updateStateForTransferError(state, error);

		// Assertions: cosmetic errors keep the transfer button available.
		assertTrue(state.active);
		assertTrue(state.visible);
	}

	@Test
	public void blockingTransferErrorVisibilityFollowsErrorType() {
		// Setup: user-facing errors should be visible, but internal errors should hide the button.
		TestButtonState userFacingState = new TestButtonState();
		TestButtonState internalState = new TestButtonState();

		// Operation: apply each blocking transfer-error state to a button.
		RecipeTransferButtonController.updateStateForTransferError(
			userFacingState,
			new TestTransferError(IRecipeTransferError.Type.USER_FACING, Component.literal("Missing items"), 1)
		);
		RecipeTransferButtonController.updateStateForTransferError(
			internalState,
			new TestTransferError(IRecipeTransferError.Type.INTERNAL, Component.literal("Internal error"), -1)
		);

		// Assertions: both are inactive, but only user-facing errors are exposed to the player.
		assertTrue(!userFacingState.active);
		assertTrue(userFacingState.visible);
		assertTrue(!internalState.active);
		assertTrue(!internalState.visible);
	}

	@Test
	public void transferErrorTooltipAndMissingCountComeFromError() {
		// Setup: a transfer error supplies user-facing text and a missing-count hint for recipe sorting.
		Component warning = Component.literal("Partial transfer");
		TestTransferError error = new TestTransferError(IRecipeTransferError.Type.COSMETIC, warning, 3);
		JeiTooltip tooltip = new JeiTooltip();

		// Operation: read the state exposed by the transfer button controller.
		RecipeTransferUtil.addTransferRecipeTooltip(error, tooltip);
		int missingCountHint = RecipeTransferButtonController.getMissingCountHint(error);

		// Assertions: the button exposes the plugin error's tooltip and missing-count hint.
		assertEquals(List.of(warning), tooltip.getLegacyComponents());
		assertEquals(3, missingCountHint);
	}

	@Test
	public void successfulTransferTooltipUsesDefaultText() {
		// Setup: no transfer error is cached for the current recipe.
		JeiTooltip tooltip = new JeiTooltip();

		// Operation: read the default transfer tooltip.
		RecipeTransferUtil.addTransferRecipeTooltip(null, tooltip);

		// Assertions: the normal transfer tooltip is still available.
		List<Component> components = tooltip.getLegacyComponents();
		assertEquals(1, components.size());
		assertEquals("jei.tooltip.transfer", components.getFirst().getString());
		assertEquals(0, RecipeTransferButtonController.getMissingCountHint(null));
	}

	private static class TestButtonState implements IButtonState {
		private boolean active;
		private boolean visible;

		@Override
		public void setIcon(IDrawable icon) {

		}

		@Override
		public void setActive(boolean value) {
			this.active = value;
		}

		@Override
		public void setVisible(boolean value) {
			this.visible = value;
		}

		@Override
		public void setForcePressed(boolean value) {

		}
	}

	private record TestTransferError(Type type, Component tooltip, int missingCountHint) implements IRecipeTransferError {
		@Override
		public Type getType() {
			return type;
		}

		@Override
		public void getTooltip(ITooltipBuilder tooltip) {
			tooltip.add(this.tooltip);
		}

		@Override
		public int getMissingCountHint() {
			return missingCountHint;
		}
	}
}
