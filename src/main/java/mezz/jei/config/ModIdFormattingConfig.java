package mezz.jei.config;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import mezz.jei.api.constants.ModIds;
import mezz.jei.config.forge.Property;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModIdFormattingConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";
	public static final String defaultModNameFormatFriendly = "blue italic";
	//private final LocalizedConfiguration config;

	public final ForgeConfigSpec.ConfigValue<String> modNameFormatConfig;

	public String modNameFormat = parseFriendlyModNameFormat(defaultModNameFormatFriendly);
	@Nullable
	private String modNameFormatOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	public ModIdFormattingConfig(ForgeConfigSpec.Builder builder) {
		//this.config = config;
		builder.push("formatting");
		modNameFormatConfig = builder.define("Mod name format", defaultModNameFormatFriendly);
		builder.pop();
	}

	public String getModNameFormat() {
		String override = modNameFormatOverride;
		if (override != null) {
			return override;
		}
		return modNameFormat;
	}

	public boolean isModNameFormatOverrideActive() {
		return modNameFormatOverride != null;
	}

	public void checkForModNameFormatOverride() {
		String modNameFormatOverride = ModIdFormattingConfig.detectModNameTooltipFormatting();
		if (!Objects.equals(this.modNameFormatOverride, modNameFormatOverride)) {
			this.modNameFormatOverride = modNameFormatOverride;
			updateModNameFormat();
		}
	}

	private void updateModNameFormat() {
		EnumSet<TextFormatting> validFormatting = EnumSet.allOf(TextFormatting.class);
		validFormatting.remove(TextFormatting.RESET);
		String[] validValues = new String[validFormatting.size()];
		int i = 0;
		for (TextFormatting formatting : validFormatting) {
			validValues[i] = formatting.getFriendlyName().toLowerCase(Locale.ENGLISH);
			i++;
		}
		/*
		Property property = config.getString("modNameFormat", ClientConfig.CATEGORY_ADVANCED, defaultModNameFormatFriendly, validValues);
		boolean showInGui = !isModNameFormatOverrideActive();
		property.setShowInGui(showInGui);*/
		String modNameFormatFriendly = modNameFormatConfig.get();
		modNameFormat = parseFriendlyModNameFormat(modNameFormatFriendly);
	}

	private static String parseFriendlyModNameFormat(String formatWithEnumNames) {
		if (formatWithEnumNames.isEmpty()) {
			return "";
		}
		StringBuilder format = new StringBuilder();
		String[] strings = formatWithEnumNames.split(" ");
		for (String string : strings) {
			TextFormatting valueByName = TextFormatting.getValueByName(string);
			if (valueByName != null) {
				format.append(valueByName.toString());
			} else {
				LOGGER.error("Invalid format: {}", string);
			}
		}
		return format.toString();
	}

	@Nullable
	private static String detectModNameTooltipFormatting() {
		try {
			ItemStack itemStack = new ItemStack(Items.APPLE);
			ClientPlayerEntity player = Minecraft.getInstance().player;
			List<ITextComponent> tooltip = new ArrayList<>();
			tooltip.add(new StringTextComponent("JEI Tooltip Testing for mod name formatting"));
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
