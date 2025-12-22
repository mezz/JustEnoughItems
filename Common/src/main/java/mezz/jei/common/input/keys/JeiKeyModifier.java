package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import mezz.jei.common.input.KeyNameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.InputQuirks;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public enum JeiKeyModifier {
	CONTROL {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			Minecraft minecraft = Minecraft.getInstance();
			return minecraft.hasControlDown();
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			return Component.translatable("jei.key.combo.control", KeyNameUtil.getKeyDisplayName(key));
		}
	},
	CONTROL_OR_COMMAND {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
				Minecraft minecraft = Minecraft.getInstance();
				Window window = minecraft.getWindow();
				return InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER) || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER);
			}
			return CONTROL.isActive(context);
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			if (InputQuirks.REPLACE_CTRL_KEY_WITH_CMD_KEY) {
				return Component.translatable("jei.key.combo.command", KeyNameUtil.getKeyDisplayName(key));
			}
			return CONTROL.getCombinedName(key);
		}
	},
	SHIFT {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			Minecraft minecraft = Minecraft.getInstance();
			return minecraft.hasShiftDown();
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			return Component.translatable("jei.key.combo.shift", KeyNameUtil.getKeyDisplayName(key));
		}
	},
	ALT {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			Minecraft minecraft = Minecraft.getInstance();
			return minecraft.hasAltDown();
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			return Component.translatable("jei.key.combo.alt", KeyNameUtil.getKeyDisplayName(key));
		}
	},
	NONE {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			if (context.conflicts(JeiKeyConflictContext.IN_GAME)) {
				return true;
			}
			return !CONTROL.isActive(context) &&
				!CONTROL_OR_COMMAND.isActive(context) &&
				!SHIFT.isActive(context) &&
				!ALT.isActive(context);
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			return KeyNameUtil.getKeyDisplayName(key);
		}
	};

	public abstract boolean isActive(JeiKeyConflictContext context);

	public abstract Component getCombinedName(InputConstants.Key key);
}
