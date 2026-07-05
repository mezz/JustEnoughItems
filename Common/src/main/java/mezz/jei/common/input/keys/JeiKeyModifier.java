package mezz.jei.common.input.keys;

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
		public Component getCombinedName(Component component) {
			if (Minecraft.ON_OSX) {
				return Component.translatable("jei.key.combo.command", component);
			} else {
				return Component.translatable("jei.key.combo.control", component);
			}
		}
	},
	SHIFT {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			return Screen.hasShiftDown();
		}

		@Override
		public Component getCombinedName(Component component) {
			return Component.translatable("jei.key.combo.shift", component);
		}
	},
	ALT {
		@Override
		public boolean isActive(JeiKeyConflictContext context) {
			return Screen.hasAltDown();
		}

		@Override
		public Component getCombinedName(Component component) {
			return Component.translatable("jei.key.combo.alt", component);
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
		public Component getCombinedName(Component component) {
			return component;
		}
	};

	public abstract boolean isActive(JeiKeyConflictContext context);

	public abstract Component getCombinedName(Component component);
}
