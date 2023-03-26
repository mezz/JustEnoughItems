package mezz.jei.common.util;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.List;
import java.util.stream.Collectors;

public class TooltipComponentHelper {

    public static ClientTooltipComponent create(String text) {
        return ClientTooltipComponent.create(Component.translatable(text).getVisualOrderText());
    }

    public static ClientTooltipComponent create(String text, Object... args) {
        return ClientTooltipComponent.create(Component.translatable(text, args).getVisualOrderText());
    }

    public static ClientTooltipComponent create(Component text) {
        return ClientTooltipComponent.create(text.getVisualOrderText());
    }

    public static ClientTooltipComponent create(TooltipComponent component) {
        return ClientTooltipComponent.create(component);
    }

    public static List<ClientTooltipComponent> from(List<Component> components){
        return components.stream().map(TooltipComponentHelper::create).collect(Collectors.toList());
    }

}
