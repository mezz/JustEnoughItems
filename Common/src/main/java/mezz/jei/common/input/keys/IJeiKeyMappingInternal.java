package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;

import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public interface IJeiKeyMappingInternal extends IJeiKeyMapping {
    @Override
    boolean isActiveAndMatches(InputConstants.Key key);

    @Override
    boolean isUnbound();

    @Override
    Component getTranslatedKeyMessage();

    IJeiKeyMapping register(Consumer<KeyMapping> registerMethod);
}
