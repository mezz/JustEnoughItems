package mezz.jei.common.input.keys;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.runtime.IJeiKeyMapping;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;

public class JeiMultiKeyMapping implements IJeiKeyMapping {
    private final List<IJeiKeyMapping> mappings;

    public JeiMultiKeyMapping(IJeiKeyMapping... mappings) {
        this.mappings = Arrays.asList(mappings);
    }

    @Override
    public boolean isActiveAndMatches(InputConstants.Key key) {
        return this.mappings.stream()
            .anyMatch(m -> m.isActiveAndMatches(key));
    }

    @Override
    public boolean isUnbound() {
        return this.mappings.stream()
            .allMatch(IJeiKeyMapping::isUnbound);
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return this.mappings.stream()
            .filter(m -> !m.isUnbound())
            .map(IJeiKeyMapping::getTranslatedKeyMessage)
            .findFirst()
            .orElseGet(() ->
                this.mappings.stream()
                .map(IJeiKeyMapping::getTranslatedKeyMessage)
                .findFirst()
                .orElseGet(() -> Component.literal("error"))
            );
    }
}
