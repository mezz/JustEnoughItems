package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum JeiKeyModifier {
    CONTROL_OR_COMMAND {
        @Override
        public boolean isActive(JeiKeyConflictContext context) {
            return Screen.hasControlDown();
        }

        @Override
        public Component getCombinedName(InputConstants.Key key) {
            if (Minecraft.ON_OSX) {
                return new TranslatableComponent("jei.key.combo.command", key.getDisplayName());
            } else {
                return new TranslatableComponent("jei.key.combo.control", key.getDisplayName());
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
            return new TranslatableComponent("jei.key.combo.shift", key.getDisplayName());
        }
    },
    ALT {
        @Override
        public boolean isActive(JeiKeyConflictContext context) {
            return Screen.hasAltDown();
        }

        @Override
        public Component getCombinedName(InputConstants.Key key) {
            return new TranslatableComponent("jei.key.combo.alt", key.getDisplayName());
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
            return key.getDisplayName();
        }
    };

    public abstract boolean isActive(JeiKeyConflictContext context);

    public abstract Component getCombinedName(InputConstants.Key key);
}
