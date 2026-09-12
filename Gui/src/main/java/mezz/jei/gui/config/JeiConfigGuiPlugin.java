package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import net.mezzdev.config.gui.api.ConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiPlugin;
import net.mezzdev.config.gui.api.IConfigGuiRegistration;
import net.minecraft.network.chat.Component;

/**
 * JEI config GUI customizations.
 */
@ConfigGuiPlugin
public class JeiConfigGuiPlugin implements IConfigGuiPlugin {
	@Override
	public String getModId() {
		return ModIds.JEI_ID;
	}

	@Override
	public void register(IConfigGuiRegistration registration) {
		registration.configureScreen(screenBuilder -> {
			screenBuilder.setTitle(Component.translatable("jei.config"));
			screenBuilder.configureCategory("debug")
				.clearDefaultValues();
		});
	}
}
