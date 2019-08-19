package mezz.jei.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.network.NetworkManager;

import mezz.jei.JustEnoughItems;
import mezz.jei.gui.recipes.RecipesGui;
import mezz.jei.network.packets.PacketRequestCheatPermission;
import mezz.jei.util.Translator;

public class JEIModConfigGui extends GuiConfig {

	public JEIModConfigGui(GuiScreen parent) {
		super(getParent(parent), getConfigElements(), Constants.MOD_ID, false, false, getTitle(parent));
	}

	/**
	 * Don't return to a RecipesGui, it will not be valid after configs are changed.
	 */
	private static GuiScreen getParent(GuiScreen parent) {
		if (parent instanceof RecipesGui) {
			GuiScreen parentScreen = ((RecipesGui) parent).getParentScreen();
			if (parentScreen != null) {
				return parentScreen;
			} else {
				Minecraft minecraft = parent.mc;
				if (minecraft != null) {
					EntityPlayerSP player = minecraft.player;
					if (player != null) {
						return new GuiInventory(player);
					}
				}
			}
		}
		return parent;
	}

	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> configElements = new ArrayList<>();

		if (Minecraft.getMinecraft().world != null) {
			Configuration worldConfig = Config.getWorldConfig();
			if (worldConfig != null) {
				NetworkManager networkManager = FMLClientHandler.instance().getClientToServerNetworkManager();
				ConfigCategory categoryWorldConfig = worldConfig.getCategory(ServerInfo.getWorldUid(networkManager));
				configElements.addAll(new ConfigElement(categoryWorldConfig).getChildElements());
			}
		}

		LocalizedConfiguration config = Config.getConfig();
		if (config != null) {
			ConfigCategory categoryAdvanced = config.getCategory(Config.CATEGORY_ADVANCED);
			configElements.addAll(new ConfigElement(categoryAdvanced).getChildElements());

			ConfigCategory categorySearch = config.getCategory(Config.CATEGORY_SEARCH);
			configElements.add(new ConfigElement(categorySearch));
		}

		return configElements;
	}

	private static String getTitle(GuiScreen parent) {
		if (parent instanceof GuiModList) {
			LocalizedConfiguration config = Config.getConfig();
			if (config != null) {
				return GuiConfig.getAbridgedConfigPath(config.toString());
			}
		}
		return Translator.translateToLocal("config.jei.title").replace("%MODNAME", Constants.NAME);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		super.actionPerformed(button);

		if (Config.isCheatItemsEnabled() && ServerInfo.isJeiOnServer()) {
			JustEnoughItems.getProxy().sendPacketToServer(new PacketRequestCheatPermission());
		}
	}
}
