package mezz.jei.api;

/**
 * JEIManager provides helpers and tools for addon mods.
 * JEI is almost entirely client-side, on server-side these are not functioning and may be null.
 */
public class JEIManager {

	/* The following are available after FMLPreInitializationEvent */
	public static IGuiHelper guiHelper;
	public static IItemBlacklist itemBlacklist;

	/* The following are available once the world has loaded */
	public static IItemRegistry itemRegistry;
	public static IRecipeRegistry recipeRegistry;

}
