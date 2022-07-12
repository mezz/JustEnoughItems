package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public interface IJeiKeyMapping {
    boolean isActiveAndMatches(InputConstants.Key key);

    boolean isUnbound();

    Component getTranslatedKeyMessage();

    IJeiKeyMapping register(Consumer<KeyMapping> registerMethod);
}
