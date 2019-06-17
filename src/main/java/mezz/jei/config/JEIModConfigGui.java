//package mezz.jei.config;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import mezz.jei.JustEnoughItems;
//import mezz.jei.gui.recipes.RecipesGui;
//import mezz.jei.network.packets.PacketRequestCheatPermission;
//import mezz.jei.util.Translator;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.entity.ClientPlayerEntity;
//import net.minecraft.client.gui.GuiButton;
//import net.minecraft.client.gui.Screen;
//import net.minecraft.client.gui.inventory.GuiInventory;
//import net.minecraft.network.NetworkManager;
//import net.minecraftforge.common.config.ConfigCategory;
//import net.minecraftforge.common.config.ConfigElement;
//import net.minecraftforge.common.config.Configuration;
//import net.minecraftforge.fml.client.FMLClientHandler;
//import net.minecraftforge.fml.client.GuiModList;
//import net.minecraftforge.fml.client.config.GuiConfig;
//import net.minecraftforge.fml.client.config.IConfigElement;
//
//public class JEIModConfigGui extends GuiConfig {
//
//	public JEIModConfigGui(Screen parent) {
//		super(getParent(parent), getConfigElements(), Constants.JEI_ID, false, false, getTitle(parent));
//	}
//
//	/**
//	 * Don't return to a RecipesGui, it will not be valid after configs are changed.
//	 */
//	private static Screen getParent(Screen parent) {
//		if (parent instanceof RecipesGui) {
//			Screen parentScreen = ((RecipesGui) parent).getParentScreen();
//			if (parentScreen != null) {
//				return parentScreen;
//			} else {
//				Minecraft minecraft = parent.minecraft;
//				ClientPlayerEntity player = minecraft.player;
//				if (player != null) {
//					return new GuiInventory(player);
//				}
//			}
//		}
//		return parent;
//	}
//
//	private static List<IConfigElement> getConfigElements() {
//		List<IConfigElement> configElements = new ArrayList<>();
//
//		if (Minecraft.getInstance().world != null) {
//			Configuration worldConfig = ClientConfig.getWorldConfig();
//			if (worldConfig != null) {
//				NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
//				ConfigCategory categoryWorldConfig = worldConfig.getCategory(ServerInfo.getWorldUid(networkManager));
//				configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
//			}
//		}
//
//		LocalizedConfiguration config = ClientConfig.getConfig();
//		if (config != null) {
//			ConfigCategory categoryAdvanced = config.getCategory(ClientConfig.CATEGORY_ADVANCED);
//			configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());
//
//			ConfigCategory categorySearch = config.getCategory(ClientConfig.CATEGORY_SEARCH);
//			configElements.add(new ConfigElement(categorySearch));
//		}
//
//		return configElements;
//	}
//
//	private static String getTitle(Screen parent) {
//		if (parent instanceof GuiModList) {
//			LocalizedConfiguration config = ClientConfig.getConfig();
//			if (config != null) {
//				return GuiConfig.getAbridgedConfigPath(config.toString());
//			}
//		}
//		return Translator.translateToLocal("config.jei.title").replace("%MODNAME", Constants.NAME);
//	}
//
//	@Override
//	protected void actionPerformed(GuiButton button) {
//		super.actionPerformed(button);
//
//		if (ClientConfig.isCheatItemsEnabled() && ServerInfo.isJeiOnServer()) {
//			JustEnoughItems.getProxy().sendPacketToServer(new PacketRequestCheatPermission());
//		}
//	}
//}
