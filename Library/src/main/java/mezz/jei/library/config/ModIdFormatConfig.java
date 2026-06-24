package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.platform.IPlatformItemStackHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.core.util.function.CachedSupplierTransformer;
import mezz.jei.library.config.serializers.ChatFormattingSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ModIdFormatConfig implements IModIdFormatConfig {
	protected static final List<ChatFormatting> defaultModNameFormat = List.of(ChatFormatting.BLUE, ChatFormatting.ITALIC);
	public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";

	private final Supplier<String> modNameFormat;
	@Nullable
	private String cachedOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	public ModIdFormatConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder modName = builder.addCategory("modName");
		Supplier<List<ChatFormatting>> configValue = modName.addList(
			"modNameFormat",
			defaultModNameFormat,
			ChatFormattingSerializer.INSTANCE
		);
		this.modNameFormat = new CachedSupplierTransformer<>(configValue, ModIdFormatConfig::toFormatString);
	}

	private static String toFormatString(List<ChatFormatting> values) {
		return values.stream()
			.map(ChatFormatting::toString)
			.collect(Collectors.joining());
	}

	private String getOverride() {
		if (cachedOverride == null) {
			cachedOverride = detectModNameTooltipFormatting();
		}
		return cachedOverride;
	}

	/*
	 * Converts styles added directly to a Component back into legacy formatting codes.
	 * Component#getString() does not preserve these styles, so this is needed when another
	 * mod adds a styled mod-name line without legacy formatting codes.
	 */
	private static String getLegacyFormattingFromStyle(Style style) {
		StringBuilder formatting = new StringBuilder();

		if (style.getColor() != null) {
			Integer color = style.getColor().getValue();
			for (ChatFormatting chatFormatting : ChatFormatting.values()) {
				if (
					chatFormatting.isColor() &&
					chatFormatting.getColor() != null &&
					chatFormatting.getColor().equals(color)
				) {
					formatting.append(chatFormatting);
					break;
				}
			}
		}

		if (style.isBold()) {
			formatting.append(ChatFormatting.BOLD);
		}
		if (style.isItalic()) {
			formatting.append(ChatFormatting.ITALIC);
		}
		if (style.isUnderlined()) {
			formatting.append(ChatFormatting.UNDERLINE);
		}
		if (style.isStrikethrough()) {
			formatting.append(ChatFormatting.STRIKETHROUGH);
		}
		if (style.isObfuscated()) {
			formatting.append(ChatFormatting.OBFUSCATED);
		}

		return formatting.toString();
	}

	@Override
	public final String getModNameFormat() {
		String override = getOverride();
		if (!override.isEmpty()) {
			return override;
		}
		return modNameFormat.get();
	}

	@Override
	public final boolean isModNameFormatOverrideActive() {
		return !getOverride().isEmpty();
	}

	private String detectModNameTooltipFormatting() {
		IPlatformItemStackHelper itemStackHelper = Services.PLATFORM.getItemStackHelper();
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		List<Component> tooltip = itemStackHelper.getTestTooltip(player, new ItemStack(Items.APPLE));
		if (tooltip.size() <= 1) {
			return "";
		}

		for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
			Component line = tooltip.get(lineNum);
			String lineString = line.getString();
			if (lineString.contains(ModIds.MINECRAFT_NAME)) {
				String withoutFormatting = ChatFormatting.stripFormatting(lineString);
				if (withoutFormatting.contains(ModIds.MINECRAFT_NAME)) {
					String detectedFormat = StringUtils.replaceOnce(lineString, ModIds.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
					if (detectedFormat.equals(MOD_NAME_FORMAT_CODE)) {
						String legacyFormat = getLegacyFormattingFromStyle(line.getStyle());
						if (!legacyFormat.isEmpty()) {
							return legacyFormat + MOD_NAME_FORMAT_CODE;
						}
					}

					return detectedFormat;
				}
			}
		}
		return "";
	}
}
