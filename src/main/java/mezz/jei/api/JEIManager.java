package mezz.jei.api;

/**
 * JEIManager provides helpers and tools for addon mods.
 * JEI is almost entirely client-side, on server-side these may not function.
 * @deprecated Get these by implementing an IModPlugin
 */
@Deprecated
public class JEIManager {

	/* The following are available after FMLPreInitializationEvent */

	/** @deprecated get this from IJeiHelpers by creating an IModPlugin */
	@Deprecated
	public static IGuiHelper guiHelper;

	/** @deprecated get this from IJeiHelpers by creating an IModPlugin */
	@Deprecated
	public static IItemBlacklist itemBlacklist;

	/** @deprecated get this from IJeiHelpers by creating an IModPlugin */
	@Deprecated
	public static INbtIgnoreList nbtIgnoreList;

	/* The following are available once the world has loaded */

	/** @deprecated get this by creating an IModPlugin */
	@Deprecated
	public static IItemRegistry itemRegistry;
	/** @deprecated get this by creating an IModPlugin */
	@Deprecated
	public static IRecipeRegistry recipeRegistry;

}
