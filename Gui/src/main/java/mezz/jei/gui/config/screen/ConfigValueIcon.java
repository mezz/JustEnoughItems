package mezz.jei.gui.config.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

final class ConfigValueIcon {
	static final int ICON_SIZE = 18;
	static final int TEXT_GAP = 3;

	private static final ResourceLocation ENABLED_ICON = ResourceLocation.withDefaultNamespace("container/beacon/confirm");
	private static final ResourceLocation DISABLED_ICON = ResourceLocation.withDefaultNamespace("container/beacon/cancel");

	private ConfigValueIcon() {

	}

	public static int getTextOffset(Object value) {
		return getIcon(value) == null ? 0 : ICON_SIZE + TEXT_GAP;
	}

	public static void draw(GuiGraphics guiGraphics, Object value, int x, int y) {
		@Nullable ResourceLocation icon = getIcon(value);
		if (icon != null) {
			guiGraphics.blitSprite(icon, x, y, ICON_SIZE, ICON_SIZE);
		}
	}

	@Nullable
	private static ResourceLocation getIcon(Object value) {
		if (value instanceof Boolean booleanValue) {
			return booleanValue ? ENABLED_ICON : DISABLED_ICON;
		}
		return null;
	}
}
