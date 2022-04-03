package mezz.jei.forge.config;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

import mezz.jei.api.constants.ModIds;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ModIdFormattingConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";
	public static final String defaultModNameFormatFriendly = "blue italic";

	private String modNameFormatFriendly = defaultModNameFormatFriendly;
	public String modNameFormat = parseFriendlyModNameFormat(defaultModNameFormatFriendly);
	@Nullable
	private String modNameFormatOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	// Forge config
	public final ForgeConfigSpec.ConfigValue<String> modNameFormatConfig;

	public ModIdFormattingConfig(ForgeConfigSpec.Builder builder) {
		EnumSet<ChatFormatting> validFormatting = EnumSet.allOf(ChatFormatting.class);
		validFormatting.remove(ChatFormatting.RESET);

		StringJoiner validColorsJoiner = new StringJoiner(", ");
		StringJoiner validFormatsJoiner = new StringJoiner(", ");

		for (ChatFormatting chatFormatting : validFormatting) {
			String lowerCaseName = chatFormatting.getName().toLowerCase(Locale.ENGLISH);
			if (chatFormatting.isColor()) {
				validColorsJoiner.add(lowerCaseName);
			} else if (chatFormatting.isFormat()) {
				validFormatsJoiner.add(lowerCaseName);
			}
		}
		String validColors = validColorsJoiner.toString();
		String validFormats = validFormatsJoiner.toString();

		builder.push("modname");
		builder.comment("Formatting for mod name tooltip", "Use these formatting colors:", validColors, "With these formatting options:", validFormats);
		modNameFormatConfig = builder.define("ModNameFormat", defaultModNameFormatFriendly);
		builder.pop();
	}

	public void reload() {
		modNameFormatFriendly = modNameFormatConfig.get();
		updateModNameFormat();
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
		modNameFormat = parseFriendlyModNameFormat(modNameFormatFriendly);
	}

	private static String parseFriendlyModNameFormat(String formatWithEnumNames) {
		if (formatWithEnumNames.isEmpty()) {
			return "";
		}
		StringBuilder format = new StringBuilder();
		String[] strings = formatWithEnumNames.split(" ");
		for (String string : strings) {
			ChatFormatting valueByName = ChatFormatting.getByName(string);
			if (valueByName != null) {
				format.append(valueByName);
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
			Minecraft minecraft = Minecraft.getInstance();
			LocalPlayer player = minecraft.player;
			List<Component> tooltip = new ArrayList<>();
			tooltip.add(new TextComponent("JEI Tooltip Testing for mod name formatting"));
			ItemTooltipEvent tooltipEvent = ForgeEventFactory.onItemTooltip(itemStack, player, tooltip, TooltipFlag.Default.NORMAL);
			tooltip = tooltipEvent.getToolTip();

			if (tooltip.size() > 1) {
				for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
					Component line = tooltip.get(lineNum);
					String lineString = line.getString();
					if (lineString.contains(ModIds.MINECRAFT_NAME)) {
						String withoutFormatting = ChatFormatting.stripFormatting(lineString);
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
