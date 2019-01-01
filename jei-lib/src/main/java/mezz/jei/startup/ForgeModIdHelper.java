package mezz.jei.startup;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.fml.ModList;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.config.ClientConfig;
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

	@Override
	public String getModNameForModId(String modId) {
		return ModList.get().getModContainerById(modId)
			.map(modContainer -> modContainer.getModInfo().getDisplayName())
			.orElse(modId);
	}

	@Override
	public String getFormattedModNameForModId(String modId) {
		String modNameFormat = ClientConfig.getInstance().getModNameFormat();
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
			EntityPlayerSP player = Minecraft.getInstance().player;
			List<ITextComponent> tooltip = new ArrayList<>();
			tooltip.add(new TextComponentString("JEI Tooltip Testing for mod name formatting"));
			ItemTooltipEvent tooltipEvent = ForgeEventFactory.onItemTooltip(itemStack, player, tooltip, ITooltipFlag.TooltipFlags.NORMAL);
			tooltip = tooltipEvent.getToolTip();

			if (tooltip.size() > 1) {
				for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
					ITextComponent line = tooltip.get(lineNum);
					String lineString = line.getString();
					if (lineString.contains(Constants.MINECRAFT_NAME)) {
						String withoutFormatting = TextFormatting.getTextWithoutFormattingCodes(lineString);
						if (withoutFormatting != null) {
							if (lineString.equals(withoutFormatting)) {
								return "";
							} else if (lineString.contains(withoutFormatting)) {
								return StringUtils.replaceOnce(lineString, Constants.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
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
		if (ClientConfig.getInstance().isDebugModeEnabled() && Minecraft.getInstance().gameSettings.advancedItemTooltips) {
			tooltip = new ArrayList<>(tooltip);
			tooltip.add(TextFormatting.GRAY + "JEI Debug:");
			tooltip.add(TextFormatting.GRAY + "info: " + ingredientHelper.getErrorInfo(ingredient));
			tooltip.add(TextFormatting.GRAY + "uid: " + ingredientHelper.getUniqueId(ingredient));
		}
		if (ClientConfig.getInstance().isModNameFormatOverrideActive() && (ingredient instanceof ItemStack || ingredient instanceof EnchantmentData)) {
			// we detected that another mod is adding the mod name already
			return tooltip;
		}
		return super.addModNameToIngredientTooltip(tooltip, ingredient, ingredientHelper);
	}
}
