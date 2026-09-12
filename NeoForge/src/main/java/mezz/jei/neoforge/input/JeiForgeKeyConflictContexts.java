package mezz.jei.neoforge.input;

import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public enum JeiForgeKeyConflictContexts implements IKeyConflictContext {
	JEI_GUI_HOVER,
	JEI_GUI_HOVER_BOOKMARK,
	JEI_GUI_HOVER_CHEAT_MODE,
	JEI_GUI_HOVER_CONFIG_BUTTON,
	JEI_GUI_HOVER_INGREDIENT,
	JEI_GUI_HOVER_SEARCH,
	JEI_GUI_FOCUSED_SEARCH;

	@Override
	public boolean isActive() {
		return KeyConflictContext.GUI.isActive();
	}

	@Override
	public boolean conflicts(IKeyConflictContext other) {
		return this == other;
	}
}
