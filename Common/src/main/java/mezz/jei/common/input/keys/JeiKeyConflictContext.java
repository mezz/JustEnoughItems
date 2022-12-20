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
    JEI_GUI_HOVER {
        @Override
        public boolean isActive() {
            return GUI.isActive();
        }
    },
    JEI_GUI_HOVER_CHEAT_MODE {
        @Override
        public boolean isActive() {
            return GUI.isActive();
        }
    },
    JEI_GUI_HOVER_CONFIG_BUTTON {
        @Override
        public boolean isActive() {
            return GUI.isActive();
        }
    },
    JEI_GUI_HOVER_SEARCH {
        @Override
        public boolean isActive() {
            return GUI.isActive();
        }
    };

    public abstract boolean isActive();

    public boolean conflicts(JeiKeyConflictContext other) {
        return this == other;
    }
}
