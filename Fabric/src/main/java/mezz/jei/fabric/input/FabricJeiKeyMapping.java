package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import mezz.jei.common.input.keys.IJeiKeyMappingInternal;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class FabricJeiKeyMapping implements IJeiKeyMappingInternal {
    protected final KeyMapping keyMapping;
    protected final JeiKeyConflictContext context;

    public FabricJeiKeyMapping(KeyMapping keyMapping, JeiKeyConflictContext context) {
        this.keyMapping = keyMapping;
        this.context = context;
    }

    @Override
    public boolean isActiveAndMatches(InputConstants.Key key) {
        if (isUnbound()) {
            return false;
        }
        if (!KeyBindingHelper.getBoundKeyOf(this.keyMapping).equals(key)) {
            return false;
        }
        return context.isActive();
    }

    @Override
    public boolean isUnbound() {
        return this.keyMapping.isUnbound();
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return this.keyMapping.getTranslatedKeyMessage();
    }

    @Override
    public IJeiKeyMappingInternal register(Consumer<KeyMapping> registerMethod) {
        registerMethod.accept(this.keyMapping);
        return this;
    }
}
