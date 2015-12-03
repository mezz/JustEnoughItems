package mezz.jei.api;

/**
 * JEIManager is the API entry point.
 * JEI is almost entirely client-side, expect the API parts to be null on server-side.
 */
public class JEIManager {

	/* The following are available during FMLPreInitializationEvent */
	public static IPluginRegistry pluginRegistry;
	public static IGuiHelper guiHelper;

	/* The following are available after JEI's FMLLoadCompleteEvent */
	public static IItemRegistry itemRegistry;
	public static IRecipeRegistry recipeRegistry;

}
