package mezz.jei.library.plugins.vanilla.gui;

import mezz.jei.api.gui.handlers.IGlobalGuiHandler;
import mezz.jei.common.platform.IPlatformScreenHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.minecraft.client.renderer.Rect2i;

import java.util.Collection;
import java.util.List;

public class ToastGuiHandler implements IGlobalGuiHandler {
	@Override
	public Collection<Rect2i> getGuiExtraAreas() {
		IPlatformScreenHelper screenHelper = Services.PLATFORM.getScreenHelper();
		ImmutableRect2i toastsArea = screenHelper.getToastsArea();
		if (toastsArea.isEmpty()) {
			return List.of();
		}
		return List.of(toastsArea.toMutable());
	}
}
