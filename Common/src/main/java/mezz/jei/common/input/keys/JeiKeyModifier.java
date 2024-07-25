package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.KeyNameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public enum JeiKeyModifier {
	CONTROL_OR_COMMAND {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			return Screen.hasControlDown();
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			if (Minecraft.ON_OSX) {
				return Component.translatable("jei.key.combo.command", KeyNameUtil.getKeyDisplayName(key));
			} else {
				return Component.translatable("jei.key.combo.control", KeyNameUtil.getKeyDisplayName(key));
			}
		}
	},
	SHIFT {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			return Screen.hasShiftDown();
		}

		@Override
		public Component getCombinedName(InputConstants.Key key) {
			return Component.translatable("jei.key.combo.shift", KeyNameUtil.getKeyDisplayName(key));
		}
	},
	ALT {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			return Screen.hasAltDown();
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
			return !CONTROL_OR_COMMAND.isActive(context) &&
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
