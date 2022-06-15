package mezz.jei.forge.input;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.common.input.keys.IJeiKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ClientRegistry;

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
    public IJeiKeyMapping register() {
        ClientRegistry.registerKeyBinding(keyMapping);
        return this;
    }
}
