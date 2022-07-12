package mezz.jei.fabric.input;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.function.Consumer;
import mezz.jei.common.input.keys.IJeiKeyMapping;
import mezz.jei.common.input.keys.JeiKeyConflictContext;
import mezz.jei.common.input.keys.JeiKeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

public class FabricJeiKeyMapping implements IJeiKeyMapping {
    private final String category;
    private final String description;
    private final JeiKeyConflictContext context;
    private final JeiKeyModifier modifier;
    private final InputConstants.Type type;
    private final InputConstants.Key key;

    public FabricJeiKeyMapping(
        String category,
        String description,
        JeiKeyConflictContext context,
        JeiKeyModifier modifier,
        InputConstants.Type type,
        int keyCode
    ) {
        this.category = category;
        this.description = description;
        this.context = context;
        this.modifier = modifier;
        this.type = type;
        this.key = type.getOrCreate(keyCode);
    }

    @Override
    public boolean isActiveAndMatches(InputConstants.Key key) {
        if (isUnbound()) {
            return false;
        }
        if (!this.key.equals(key)) {
            return false;
        }
        return context.isActive() && modifier.isActive(context);
    }

    @Override
    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return modifier.getCombinedName(key);
    }

    @Override
    public IJeiKeyMapping register(Consumer<KeyMapping> registerMethod) {
        return this;
    }
}
