package mezz.jei.gui.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PinnedTooltipManagerTest {
	private static final InputConstants.Key ACTION_KEY = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_A);
	private static final InputConstants.Key PIN_KEY = InputConstants.Type.KEYSYM.getOrCreate(GLFW.GLFW_KEY_LEFT_SHIFT);

	@Test
	public void pinnedTooltipAllowsExtraKeyboardModifier() {
		// Setup: simulate NeoForge rejecting an unmodified action because the pin modifier is held.
		TestKeyMapping actionMapping = new TestKeyMapping(ACTION_KEY, false);
		TestKeyMapping pinMapping = new TestKeyMapping(PIN_KEY, true);
		IPinnedTooltipHolder holder = () -> {};

		// Operation and assertions: the relaxed match only applies while a tooltip is pinned.
		assertFalse(PinnedTooltipManager.matchesInput(ACTION_KEY, actionMapping, pinMapping));
		PinnedTooltipManager.opened(holder);
		try {
			assertTrue(PinnedTooltipManager.matchesInput(ACTION_KEY, actionMapping, pinMapping));
		} finally {
			PinnedTooltipManager.closed(holder);
		}
	}

	@Test
	public void pinnedTooltipDoesNotRelaxMouseBindings() {
		// Setup: an unmatched mouse action while the pin key and a tooltip are active.
		InputConstants.Key mouseKey = InputConstants.Type.MOUSE.getOrCreate(InputConstants.MOUSE_BUTTON_LEFT);
		TestKeyMapping actionMapping = new TestKeyMapping(mouseKey, false);
		TestKeyMapping pinMapping = new TestKeyMapping(PIN_KEY, true);
		IPinnedTooltipHolder holder = () -> {};

		// Operation and assertion: mouse modifier matching remains strict.
		PinnedTooltipManager.opened(holder);
		try {
			assertFalse(PinnedTooltipManager.matchesInput(mouseKey, actionMapping, pinMapping));
		} finally {
			PinnedTooltipManager.closed(holder);
		}
	}

	@Test
	public void pinnedTooltipSuppressesOnlyExternalTooltips() {
		IPinnedTooltipHolder holder = () -> {};

		assertFalse(PinnedTooltipManager.shouldSuppressExternalTooltip());
		PinnedTooltipManager.opened(holder);
		try {
			assertTrue(PinnedTooltipManager.shouldSuppressExternalTooltip());
			PinnedTooltipManager.draw(holder, () -> assertFalse(PinnedTooltipManager.shouldSuppressExternalTooltip()));
			assertTrue(PinnedTooltipManager.shouldSuppressExternalTooltip());
		} finally {
			PinnedTooltipManager.closed(holder);
		}
		assertFalse(PinnedTooltipManager.shouldSuppressExternalTooltip());
	}

	private static class TestKeyMapping implements IJeiKeyMappingInternal {
		private final InputConstants.Key key;
		private final boolean down;

		private TestKeyMapping(InputConstants.Key key, boolean down) {
			this.key = key;
			this.down = down;
		}

		@Override
		public boolean isActiveAndMatches(InputConstants.Key key) {
			return false;
		}

		@Override
		public boolean isActiveAndMatchesAllowingExtraModifiers(InputConstants.Key key) {
			return this.key.equals(key);
		}

		@Override
		public boolean isUnbound() {
			return false;
		}

		@Override
		public Component getTranslatedKeyMessage() {
			return Component.empty();
		}

		@Override
		public boolean isDown() {
			return down;
		}

		@Override
		public IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod) {
			return this;
		}
	}
}
