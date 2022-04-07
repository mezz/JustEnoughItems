package mezz.jei.forge.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.AbstractModIdFormatConfig;
import mezz.jei.core.util.function.CachedSupplierTransformer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;

public class ModIdFormatConfig extends AbstractModIdFormatConfig {
	private static final Logger LOGGER = LogManager.getLogger();

	@Nullable
	private String modNameFormatOverride; // when we detect another mod is adding mod names to tooltips, use its formatting
	private final Supplier<String> modNameFormat;

	public ModIdFormatConfig(ForgeConfigSpec.Builder builder) {
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
		ForgeConfigSpec.ConfigValue<String> configValue = builder.define("ModNameFormat", defaultModNameFormatFriendly);
		this.modNameFormat = new CachedSupplierTransformer<>(configValue::get, AbstractModIdFormatConfig::parseFriendlyModNameFormat);
		builder.pop();
	}

	@Override
	public String getModNameFormat() {
		String override = modNameFormatOverride;
		if (override != null) {
			return override;
		}
		return modNameFormat.get();
	}

	@Override
	public boolean isModNameFormatOverrideActive() {
		return modNameFormatOverride != null;
	}

	public void checkForModNameFormatOverride() {
		String modNameFormatOverride = detectModNameTooltipFormatting();
		if (!Objects.equals(this.modNameFormatOverride, modNameFormatOverride)) {
			this.modNameFormatOverride = modNameFormatOverride;
		}
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
