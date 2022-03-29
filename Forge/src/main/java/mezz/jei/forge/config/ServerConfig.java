package mezz.jei.forge.config;

import com.google.common.base.Preconditions;
import mezz.jei.core.config.IServerConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import org.jetbrains.annotations.Nullable;

public final class ServerConfig implements IServerConfig {
	@Nullable
	private static IServerConfig instance;

	// Forge config
	private final ForgeConfigSpec.BooleanValue enableCheatModeForOp;
	private final ForgeConfigSpec.BooleanValue enableCheatModeForCreative;
	private final ForgeConfigSpec.BooleanValue enableCheatModeForGive;

	public static IServerConfig register(ModLoadingContext modLoadingContext) {
		ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
		instance = new ServerConfig(builder);
		ForgeConfigSpec config = builder.build();
		modLoadingContext.registerConfig(ModConfig.Type.SERVER, config);
		return instance;
	}

	public ServerConfig(ForgeConfigSpec.Builder builder) {
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

	public static IServerConfig getInstance() {
		Preconditions.checkNotNull(instance);
		return instance;
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
