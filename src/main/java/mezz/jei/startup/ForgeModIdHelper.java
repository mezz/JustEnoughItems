package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import mezz.jei.util.Log;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

public class ForgeModIdHelper extends AbstractModIdHelper {
	@Nullable
	private static ForgeModIdHelper INSTANCE;

	public static IModIdHelper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new ForgeModIdHelper();
		}
		return INSTANCE;
	}

	private final Map<String, ModContainer> modMap;

	private ForgeModIdHelper() {
		this.modMap = Loader.instance().getIndexedModList();
	}

	@Override
	public String getModNameForModId(String modId) {
		ModContainer modContainer = this.modMap.get(modId);
		if (modContainer == null) {
			return modId;
		}
		return modContainer.getName();
	}

	@Nullable
	@Override
	public String getModNameTooltipFormatting() {
		try {
			ItemStack itemStack = new ItemStack(Items.APPLE);
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			List<String> tooltip = new ArrayList<>();
			tooltip.add("JEI Tooltip Testing for mod name formatting");
			ItemTooltipEvent tooltipEvent = ForgeEventFactory.onItemTooltip(itemStack, player, tooltip, ITooltipFlag.TooltipFlags.NORMAL);
			tooltip = tooltipEvent.getToolTip();

			if (tooltip.size() > 1) {
				String lastLine = tooltip.get(tooltip.size() - 1);
				if (lastLine.contains("Minecraft")) {
					String withoutFormatting = TextFormatting.getTextWithoutFormattingCodes(lastLine);
					if (withoutFormatting != null) {
						if (lastLine.equals(withoutFormatting)) {
							return "";
						} else if (lastLine.endsWith(withoutFormatting)) {
							int i = lastLine.length() - withoutFormatting.length();
							return lastLine.substring(0, i);
						}
					}
				}
			}
		} catch (LinkageError | RuntimeException e) {
			Log.get().error("Error while Testing for mod name formatting", e);
		}
		return null;
	}
}
