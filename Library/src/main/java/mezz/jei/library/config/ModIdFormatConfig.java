package mezz.jei.library.config;

import mezz.jei.api.constants.ModIds;
import mezz.jei.common.config.legacy.LegacyConfigValueMigrator;
import mezz.jei.common.config.legacy.LegacyModNameFormatSerializer;
import net.mezzdev.config.api.schema.builder.IConfigCategoryBuilder;
import net.mezzdev.config.api.schema.builder.IConfigSchemaBuilder;
import net.mezzdev.config.api.value.IConfigValue;
import mezz.jei.common.util.function.CachedSupplierTransformer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModIdFormatConfig implements IModIdFormatConfig {
	private static final List<ChatFormatting> validModNameFormats = Arrays.stream(ChatFormatting.values())
		.filter(chatFormatting -> chatFormatting != ChatFormatting.RESET)
		.toList();
	private static final LegacyModNameFormatSerializer LEGACY_SERIALIZER = new LegacyModNameFormatSerializer(validModNameFormats);
	protected static final List<ChatFormatting> defaultModNameFormat = List.of(ChatFormatting.BLUE, ChatFormatting.ITALIC);
	public static final String MOD_NAME_FORMAT_CODE = "%MODNAME%";

	private final Supplier<Component> modNameFormat;
	private final IConfigValue<List<ChatFormatting>> configValue;
	@Nullable
	private Component cachedOverride; // when we detect another mod is adding mod names to tooltips, use its formatting

	public ModIdFormatConfig(IConfigSchemaBuilder builder) {
		IConfigCategoryBuilder modName = builder.addCategory("modName");
		this.configValue = modName.addEnumList("modNameFormat", defaultModNameFormat, validModNameFormats)
			.build();
		this.modNameFormat = new CachedSupplierTransformer<>(this.configValue::get, ModIdFormatConfig::toFormatString);
	}

	public ModIdFormatConfig(IConfigSchemaBuilder builder, List<Path> legacyPaths) {
		this(builder);
		LegacyConfigValueMigrator.register(
			builder,
			legacyPaths,
			configValue,
			"modName",
			"modNameFormat",
			LEGACY_SERIALIZER
		);
	}

	private static Component toFormatString(List<ChatFormatting> values) {
		if (values.isEmpty()) {
			return Component.empty();
		}
		return Component.literal(MOD_NAME_FORMAT_CODE)
			.withStyle(values.toArray(ChatFormatting[]::new));
	}

	private Component getOverride() {
		if (cachedOverride == null) {
			cachedOverride = ModIdFormatDetectionHelper.detectModNameTooltipFormatting();
		}
		return cachedOverride;
	}

	@Override
	public final Component getModNameFormat() {
		Component override = getOverride();
		if (!override.getString().isEmpty()) {
			return override;
		}
		return modNameFormat.get();
	}

	@Override
	public final boolean isModNameFormatOverrideActive() {
		return !getOverride().getString().isEmpty();
	}

	public static Component detectModNameTooltipFormatting(List<Component> tooltip) {
		if (tooltip.size() <= 1) {
			return Component.empty();
		}

		for (int lineNum = 1; lineNum < tooltip.size(); lineNum++) {
			Component line = tooltip.get(lineNum);
			Component result = detectModNameTooltipFormatting(line);
			if (!result.getString().isEmpty()) {
				return result;
			}
		}
		return Component.empty();
	}

	private static Component detectModNameTooltipFormatting(Component line) {
		return StyledTextHelper.replaceFirst(line, ModIds.MINECRAFT_NAME, Component.literal(MOD_NAME_FORMAT_CODE))
			.orElseGet(Component::empty);
	}

	public static Component replaceModNameFormatCode(Component format, String modName) {
		return StyledTextHelper.replaceFirst(format, MOD_NAME_FORMAT_CODE, Component.literal(modName))
			.orElseGet(() -> format.copy().append(Component.literal(modName)));
	}

}
