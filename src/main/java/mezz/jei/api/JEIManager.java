package mezz.jei.api;

public class JEIManager {

	/* The following are available during FMLPreInitializationEvent */
	public static IPluginRegistry pluginRegistry;
	public static IGuiHelper guiHelper;

	/* The following are available after JEI's FMLLoadCompleteEvent */
	public static IItemRegistry itemRegistry;
	public static IRecipeRegistry recipeRegistry;

}
