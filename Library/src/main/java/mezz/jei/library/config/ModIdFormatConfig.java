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
		IConfigCategoryBuilder modName = builder.addCategory("modname");
		Supplier<List<ChatFormatting>> configValue = modName.addList(
			"ModNameFormat",
			defaultModNameFormat,
			ChatFormattingSerializer.INSTANCE,
			"Formatting for the mod names in tooltips for JEI GUIs. Leave blank to disable."
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
					return StringUtils.replaceOnce(lineString, ModIds.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
				}
			}
		}
		return "";
	}
}
