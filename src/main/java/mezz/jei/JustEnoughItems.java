package mezz.jei;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = JustEnoughItems.MODID,
		name = JustEnoughItems.NAME,
		version = JustEnoughItems.VERSION,
		dependencies = "required-after:Forge@[10.13.0.1207,);")
public class JustEnoughItems {
	public static final String MODID = "JEI";
	public static final String NAME = "JustEnoughItems";
	public static final String VERSION = "1.0";

	@Mod.Instance(JustEnoughItems.MODID)
	public static JustEnoughItems instance;

	public static ItemRegistry itemRegistry;
	public static RecipeRegistry recipeRegistry;

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new GuiEventHandler());

		itemRegistry = new ItemRegistry();
		recipeRegistry = new RecipeRegistry();
	}
}
