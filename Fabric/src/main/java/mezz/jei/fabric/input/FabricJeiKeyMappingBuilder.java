package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.AbstractJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.IJeiKeyMappingBuilder;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;

public class FabricJeiKeyMappingBuilder extends AbstractJeiKeyMappingBuilder {
    private final String category;
    private final String description;
    private JeiKeyConflictContext context = JeiKeyConflictContext.UNIVERSAL;
    private JeiKeyModifier modifier = JeiKeyModifier.NONE;

    public FabricJeiKeyMappingBuilder(String category, String description) {
        this.category = category;
        this.description = description;
    }

    @Override
    public IJeiKeyMappingBuilder setContext(JeiKeyConflictContext context) {
        this.context = context;
        return this;
    }

    @Override
    public IJeiKeyMappingBuilder setModifier(JeiKeyModifier modifier) {
        this.modifier = modifier;
        return this;
    }

    @Override
    protected IJeiKeyMappingInternal buildMouse(int mouseButton) {
        return new FabricJeiKeyMapping(
            category,
            description,
            context,
            modifier,
            InputConstants.Type.MOUSE,
            mouseButton
        );
    }

    @Override
    public IJeiKeyMappingInternal buildKeyboardKey(int key) {
        return new FabricJeiKeyMapping(
            category,
            description,
            context,
            modifier,
            InputConstants.Type.KEYSYM,
            key
        );
    }
}
