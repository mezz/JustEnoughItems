package mezz.jei.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.util.GiveMode;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigValues {
	// advanced
	/*
	public boolean debugModeEnabled = false;
	public boolean centerSearchBarEnabled = false;
	public GiveMode giveMode = GiveMode.MOUSE_PICKUP;
	public int maxColumns = 100;
	public int maxRecipeGuiHeight = 350;*/
	public ForgeConfigSpec.BooleanValue debugModeEnabled;
	public ForgeConfigSpec.BooleanValue centerSearchBarEnabled;
	public ForgeConfigSpec.EnumValue<GiveMode> giveMode;
	public ForgeConfigSpec.IntValue maxColumns;
	public ForgeConfigSpec.IntValue maxRecipeGuiHeight;
	
	public ConfigValues(ForgeConfigSpec.Builder builder) {
		builder.push("client");
		debugModeEnabled = builder.define("Debug mode", false);
		centerSearchBarEnabled = builder.define("Center search bar enabled", false);
		giveMode = builder.defineEnum("Give mode", GiveMode.MOUSE_PICKUP);
		maxColumns = builder.defineInRange("Max columns", 100, 1, 1000);
		maxRecipeGuiHeight = builder.defineInRange("Max recipe gui height", 350, 1, 10000);
		builder.pop();
	}
}
