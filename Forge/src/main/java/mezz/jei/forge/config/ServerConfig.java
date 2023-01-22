package mezz.jei.forge.config;

import mezz.jei.common.config.IServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.util.function.Supplier;

public final class ServerConfig implements IServerConfig {
	// Forge config
	private final Supplier<Boolean> enableCheatModeForOp;
	private final Supplier<Boolean> enableCheatModeForCreative;
	private final Supplier<Boolean> enableCheatModeForGive;

	public static IServerConfig register(ModLoadingContext modLoadingContext) {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		ServerConfig instance = new ServerConfig(builder);
		ForgeConfigSpec config = builder.build();
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, config);
		return instance;
	}

	private ServerConfig(ForgeConfigSpec.Builder builder) {
		builder.push("cheat mode");
		{
			builder.comment("Enable Cheat Mode for Operators (/op)");
			enableCheatModeForOp = builder.define("enableCheatModeForOp", true);

			builder.comment("Enable Cheat Mode for users in Creative Mode");
			enableCheatModeForCreative = builder.define("enableCheatModeForCreative", true);

			builder.comment("Enable Cheat Mode for users who can use /give");
			enableCheatModeForGive = builder.define("enableCheatModeForGive", false);
		}
		builder.pop();
	}

	@Override
	public boolean isCheatModeEnabledForOp() {
		return enableCheatModeForOp.get();
	}

	@Override
	public boolean isCheatModeEnabledForCreative() {
		return enableCheatModeForCreative.get();
	}

	@Override
	public boolean isCheatModeEnabledForGive() {
		return enableCheatModeForGive.get();
	}
}
