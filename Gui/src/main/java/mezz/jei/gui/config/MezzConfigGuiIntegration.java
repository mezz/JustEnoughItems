package mezz.jei.gui.config;

import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.common.gui.JeiTooltip;
import mezz.jei.common.platform.Services;
import mezz.jei.common.util.ImmutableRect2i;
import net.mezzdev.config.gui.api.ConfigInfo;
import net.mezzdev.config.gui.api.IQuickConfigMenu;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Guards the optional quick-menu integration with MezzConfigGui. */
public final class MezzConfigGuiIntegration {
	private static final String MOD_ID = "mezz_config_gui";
	private @Nullable IQuickConfigMenu quickMenu;

	public boolean isAvailable() {
		return Services.PLATFORM.getModHelper().isModLoaded(MOD_ID);
	}

	public boolean openQuickMenu(Supplier<ImmutableRect2i> anchor, BooleanSupplier available,
		Runnable onAnchorClick, Consumer<ITooltipBuilder> anchorTooltip) {
		// Check before loading the plugin because MezzConfigGui supplies its interfaces.
		if (!isAvailable()) {
			return false;
		}
		if (quickMenu == null) {
			quickMenu = JeiConfigGuiPlugin.createQuickMenu().orElse(null);
		}
		return quickMenu != null && quickMenu.open(() -> anchor.get().toMutable(), available,
			onAnchorClick, () -> getAnchorInfo(anchorTooltip));
	}

	public boolean isQuickMenuOpen() {
		return quickMenu != null && quickMenu.isOpen();
	}

	private static @Nullable ConfigInfo getAnchorInfo(Consumer<ITooltipBuilder> anchorTooltip) {
		JeiTooltip tooltip = new JeiTooltip();
		anchorTooltip.accept(tooltip);
		var lines = tooltip.getLines().stream().flatMap(line -> line.left().stream()).map(line -> {
				if (line instanceof Component component) {
					return component;
				}
				return Component.literal(line.getString());
			})
			.toList();
		if (lines.isEmpty()) {
			return null;
		}
		return new ConfigInfo(lines.getFirst(), lines.subList(1, lines.size()));
	}
}
