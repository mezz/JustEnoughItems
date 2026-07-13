package mezz.jei.gui.config.screen;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

final class ConfigScreenGuiHandler implements IGlobalGuiHandler {
	@Override
	public Collection<Rect2i> getGuiExtraAreas() {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.screen instanceof JeiConfigScreen screen) {
			@Nullable ImmutableRect2i selectorArea = screen.getValueSelectorArea();
			if (selectorArea != null) {
				return List.of(new Rect2i(
					selectorArea.getX(),
					selectorArea.getY(),
					selectorArea.getWidth(),
					selectorArea.getHeight()
				));
			}
		}
		return List.of();
	}
}
