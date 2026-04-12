package mezz.jei.gui.overlay.elements;

import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public interface IElementOverlay {
	void draw(GuiGraphicsExtractor guiGraphics, List<ImmutableRect2i> slots);
}
