package mezz.jei.gui.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.platform.Services;
import net.mezzdev.config.api.Configs;
import net.mezzdev.config.api.IConfigRegistration;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.UUID;

public final class JeiGuiSortingConfigRegistration {
	private JeiGuiSortingConfigRegistration() {

	}

	public static JeiGuiSortingConfigData register() {
		IConfigRegistration registration = Configs.forMod(ModIds.JEI_ID);
		Path jeiConfigDirectory = Services.PLATFORM.getConfigHelper().createJeiConfigDir();
		UUID profileId = Minecraft.getInstance().getUser().getProfileId();
		return new JeiGuiSortingConfigData(
			ModNameSortingConfig.create(registration, jeiConfigDirectory, profileId),
			IngredientTypeSortingConfig.create(registration, jeiConfigDirectory, profileId)
		);
	}
}
