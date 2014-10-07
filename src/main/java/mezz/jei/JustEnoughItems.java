package mezz.jei;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import mezz.jei.gui.GuiWrapperManager;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = JustEnoughItems.MODID, version = JustEnoughItems.VERSION)
public class JustEnoughItems {
    public static final String MODID = "JustEnoughItems";
    public static final String VERSION = "1.0";

	public static ItemRegistry itemRegistry;
	public static RecipeRegistry recipeRegistry;
	public static GuiWrapperManager wrapperManager;

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
		MinecraftForge.EVENT_BUS.register(new GuiEventHandler());

		itemRegistry = new ItemRegistry();
		recipeRegistry = new RecipeRegistry();
		wrapperManager = new GuiWrapperManager();
	}
}
