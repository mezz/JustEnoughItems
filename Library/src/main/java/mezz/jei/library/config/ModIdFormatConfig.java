package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.file.IConfigCategoryBuilder;
import mezz.jei.common.config.file.IConfigSchemaBuilder;
import mezz.jei.common.util.StringUtil;
import mezz.jei.core.util.function.CachedSupplierTransformer;
import mezz.jei.library.config.serializers.ChatFormattingSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
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
			cachedOverride = ModIdFormatDetectionHelper.detectModNameTooltipFormatting();
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

	public static String detectModNameTooltipFormatting(List<Component> tooltip) {
		if (tooltip.size() <= 1) {
			return "";
		}

		for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
			Component line = tooltip.get(lineNum);
			String lineString = line.getString();
			if (lineString.contains(ModIds.MINECRAFT_NAME)) {
				String withoutFormatting = StringUtil.removeChatFormatting(lineString);
				if (withoutFormatting.contains(ModIds.MINECRAFT_NAME)) {
					return StyledTextHelper.replaceFirst(line, ModIds.MINECRAFT_NAME, MOD_NAME_FORMAT_CODE);
				}
			}
		}
		return "";
	}
}
