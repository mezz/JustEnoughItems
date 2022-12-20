package mezz.jei.forge.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.AbstractJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;

public class ForgeJeiKeyMappingBuilder extends AbstractJeiKeyMappingBuilder {
    private final String category;
    private final String description;
    private IKeyConflictContext keyConflictContext = KeyConflictContext.UNIVERSAL;
    private KeyModifier keyModifier = KeyModifier.NONE;

    public ForgeJeiKeyMappingBuilder(String category, String description) {
        this.category = category;
        this.description = description;
    }

    @Override
    public IJeiKeyMappingBuilder setContext(JeiKeyConflictContext context) {
        this.keyConflictContext = switch (context) {
            case UNIVERSAL -> KeyConflictContext.UNIVERSAL;
            case GUI -> KeyConflictContext.GUI;
            case IN_GAME -> KeyConflictContext.IN_GAME;
            case JEI_GUI_HOVER -> JeiForgeKeyConflictContexts.JEI_GUI_HOVER;
            case JEI_GUI_HOVER_CHEAT_MODE -> JeiForgeKeyConflictContexts.JEI_GUI_HOVER_CHEAT_MODE;
            case JEI_GUI_HOVER_CONFIG_BUTTON -> JeiForgeKeyConflictContexts.JEI_GUI_HOVER_CONFIG_BUTTON;
            case JEI_GUI_HOVER_SEARCH -> JeiForgeKeyConflictContexts.JEI_GUI_HOVER_SEARCH;
        };
        return this;
    }

    @Override
    public IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier) {
        this.keyModifier = switch (modifier) {
            case CONTROL_OR_COMMAND -> KeyModifier.CONTROL;
            case SHIFT -> KeyModifier.SHIFT;
            case ALT -> KeyModifier.ALT;
            case NONE -> KeyModifier.NONE;
        };
        return this;
    }

    @Override
    protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
        KeyMapping keyMapping = new KeyMapping(
            description,
            keyConflictContext,
            keyModifier,
            InputConstants.Type.MOUSE,
            mouseButton,
            category
        );
        return new ForgeJeiKeyMapping(keyMapping);
    }

    @Override
    public IJeiKeyMappingInternal buildKeyboardKey(int key) {
        KeyMapping keyMapping = new KeyMapping(
            description,
            keyConflictContext,
            keyModifier,
            InputConstants.Type.KEYSYM,
            key,
            category
        );
        return new ForgeJeiKeyMapping(keyMapping);
    }
}
