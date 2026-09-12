package mezz.jei.common.input.keys;

import net.minecraft.client.Minecraft;

public enum JeiKeyConflictContext {
	UNIVERSAL {
		@Override
		public boolean isActive() {
			return true;
		}

		@Override
		public boolean conflicts(JeiKeyConflictContext other) {
			return true;
		}
	},
	GUI {
		@Override
		public boolean isActive() {
			return Minecraft.getInstance().screen != null;
		}
	},
	IN_GAME {
		@Override
		public boolean isActive() {
			return !GUI.isActive();
		}
	},
	JEI_GUI_HOVER,
	JEI_GUI_HOVER_BOOKMARK,
	JEI_GUI_HOVER_CHEAT_MODE,
	JEI_GUI_HOVER_CONFIG_BUTTON,
	JEI_GUI_HOVER_INGREDIENT,
	JEI_GUI_HOVER_SEARCH,
	JEI_GUI_FOCUSED_SEARCH;

	public boolean isActive() {
		return GUI.isActive();
	}

	public boolean conflicts(JeiKeyConflictContext other) {
		return this == other;
	}
}
