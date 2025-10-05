package mezz.jei.common.input;

import net.minecraft.client.input.MouseButtonEvent;

public record MouseButtonEventData(MouseButtonEvent event, boolean doubleClicked) {}
