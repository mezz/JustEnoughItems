package mezz.jei.config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ModIds;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModIdFormattingConfig {
	private static final Logger LOGGER = LogManager.getLogger();
	public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";

	@Nullable
	static String detectModNameTooltipFormatting() {
		try {
			ItemStack itemStack = new ItemStack(Items.APPLE);
			EntityPlayerSP player = Minecraft.getInstance().player;
			List<ITextComponent> tooltip = new ArrayList<>();
			tooltip.add(new TextComponentString("JEI Tooltip Testing for mod name formatting"));
			ItemTooltipEvent tooltipEvent = ForgeEventFactory.onItemTooltip(itemStack, player, tooltip, ITooltipFlag.TooltipFlags.NORMAL);
			tooltip = tooltipEvent.getToolTip();

			if (tooltip.size() > 1) {
				for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
					ITextComponent line = tooltip.get(lineNum);
					String lineString = line.getString();
					if (lineString.contains(ModIds.MINECRAFT_NAME)) {
						String withoutFormatting = TextFormatting.getTextWithoutFormattingCodes(lineString);
						if (withoutFormatting != null) {
							if (lineString.equals(withoutFormatting)) {
								return "";
							} else if (lineString.contains(withoutFormatting)) {
								return StringUtils.replaceOnce(lineString, ModIds.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
							}
						}
					}
				}
			}
		} catch (LinkageError | RuntimeException e) {
			LOGGER.error("Error while Testing for mod name formatting", e);
		}
		return null;
	}
}
