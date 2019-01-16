package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.Config;
import mezz.jei.config.Constants;
import mezz.jei.util.Log;
import org.apache.commons.lang3.StringUtils;

public class ForgeModIdHelper extends AbstractModIdHelper {
	private static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";
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

	@Override
	public String getFormattedModNameForModId(String modId) {
		String modNameFormat = Config.getModNameFormat();
		if (modNameFormat.isEmpty()) {
			return null;
		}
		String modName = getModNameForModId(modId);
		modName = removeChatFormatting(modName); // some crazy mod has formatting in the name
		if (modNameFormat.contains(MOD_NAME_FORMAT_CODE)) {
			return StringUtils.replaceOnce(modNameFormat, MOD_NAME_FORMAT_CODE, modName);
		}
		return modNameFormat + modName;
	}

	private static String removeChatFormatting(String string) {
		String withoutFormattingCodes = TextFormatting.getTextWithoutFormattingCodes(string);
		return (withoutFormattingCodes == null) ? "" : withoutFormattingCodes;
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
				for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
					String line = tooltip.get(lineNum);
					if (line.contains(Constants.MINECRAFT_NAME)) {
						String withoutFormatting = TextFormatting.getTextWithoutFormattingCodes(line);
						if (withoutFormatting != null) {
							if (line.equals(withoutFormatting)) {
								return "";
							} else if (line.contains(withoutFormatting)) {
								return StringUtils.replaceOnce(line, Constants.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
							}
						}
					}
				}
			}
		} catch (LinkageError | RuntimeException e) {
			Log.get().error("Error while Testing for mod name formatting", e);
		}
		return null;
	}

	@Override
	public <T> List<String> addModNameToIngredientTooltip(List<String> tooltip, T ingredient, IIngredientHelper<T> ingredientHelper) {
		if (Config.isDebugModeEnabled() && Minecraft.getMinecraft().gameSettings.advancedItemTooltips) {
			tooltip = new ArrayList<>(tooltip);
			tooltip.add(TextFormatting.GRAY + "JEI Debug:");
			tooltip.add(TextFormatting.GRAY + "info: " + ingredientHelper.getErrorInfo(ingredient));
			tooltip.add(TextFormatting.GRAY + "uid: " + ingredientHelper.getUniqueId(ingredient));
		}
		if (Config.isModNameFormatOverrideActive() && (ingredient instanceof ItemStack || ingredient instanceof EnchantmentData)) {
			// we detected that another mod is adding the mod name already
			return tooltip;
		}
		return super.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
	}
}
