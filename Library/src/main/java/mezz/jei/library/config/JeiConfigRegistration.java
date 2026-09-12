package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.Internal;
import mezz.jei.common.config.ClientConfigs;
import mezz.jei.common.config.DebugConfig;
import mezz.jei.common.config.legacy.LegacyConfigPaths;
import mezz.jei.common.platform.Services;
import net.mezzdev.config.api.Configs;
import net.mezzdev.config.api.IConfigRegistration;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.minecraft.client.Minecraft;

import java.nio.file.Path;
import java.util.UUID;

public final class JeiConfigRegistration {
	private JeiConfigRegistration() {

	}

	public static JeiConfigData register() {
		Path jeiConfigDirectory = Services.PLATFORM.getConfigHelper().createJeiConfigDir();
		UUID profileId = Minecraft.getInstance().getUser().getProfileId();

		IConfigRegistration registration = Configs.forMod(ModIds.JEI_ID);
		IConfigSchemaBuilder debugFileBuilder = registration.createClientSchemaBuilder("jei-debug.ini", "jei.config.debug");
		DebugConfig.create(debugFileBuilder);
		debugFileBuilder.setLegacySources(LegacyConfigPaths.get(jeiConfigDirectory, profileId, "jei-debug.ini"));
		debugFileBuilder.build();

		IConfigSchemaBuilder modFileBuilder = registration.createClientSchemaBuilder("jei-mod-id-format.ini", "jei.config.modIdFormat");
		var legacyModNameFormatPaths = LegacyConfigPaths.get(jeiConfigDirectory, profileId, "jei-mod-id-format.ini");
		ModIdFormatConfig modIdFormatConfig = new ModIdFormatConfig(modFileBuilder, legacyModNameFormatPaths);
		modFileBuilder.build();

		IConfigSchemaBuilder colorFileBuilder = registration.createClientSchemaBuilder("jei-colors.ini", "jei.config.colors");
		var legacyColorPaths = LegacyConfigPaths.get(jeiConfigDirectory, profileId, "jei-colors.ini");
		ColorNameConfig colorNameConfig = new ColorNameConfig(colorFileBuilder, legacyColorPaths);
		colorFileBuilder.build();

		boolean isDev = Services.PLATFORM.getModHelper().isInDev();
		String localizationPath = "jei.config.client";
		IConfigSchemaBuilder clientFileBuilder = registration.createClientSchemaBuilder("jei-client.ini", localizationPath);
		clientFileBuilder.setLegacySources(LegacyConfigPaths.get(jeiConfigDirectory, profileId, "jei-client.ini"));
		ClientConfigs clientConfigs = new ClientConfigs(
			clientFileBuilder,
			isDev
		);

		Internal.setClientConfigs(clientConfigs);
		return new JeiConfigData(modIdFormatConfig, colorNameConfig, clientConfigs);
	}
}
