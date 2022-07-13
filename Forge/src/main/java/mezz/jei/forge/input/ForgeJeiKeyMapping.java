package mezz.jei.forge.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import mezz.jei.common.input.keys.IJeiKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class ForgeJeiKeyMapping implements IJeiKeyMapping {
    private final KeyMapping keyMapping;

    public ForgeJeiKeyMapping(KeyMapping keyMapping) {
        this.keyMapping = keyMapping;
    }

    @Override
    public boolean isActiveAndMatches(InputConstants.Key key) {
        return keyMapping.isActiveAndMatches(key);
    }

    @Override
    public boolean isUnbound() {
        return keyMapping.isUnbound();
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return keyMapping.getTranslatedKeyMessage();
    }

    @Override
    public IJeiKeyMapping register(Consumer<KeyMapping> registerMethod) {
        registerMethod.accept(keyMapping);
        return this;
    }
}
