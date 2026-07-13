package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.Optional;

final class ConfigValueInfoFactory {
	private ConfigValueInfoFactory() {
	}

	public static ConfigInfo create(IJeiConfigValue<?> configValue, Object value) {
		Component valueName = getValueName(configValue, value);
		return new ConfigInfo(
			Component.translatable("jei.config.screen.value.info.title", valueName),
			List.of(getValueDescription(configValue, value))
		);
	}

	private static Component getValueName(IJeiConfigValue<?> configValue, Object value) {
		if (value instanceof Boolean boolValue) {
			return Component.translatable(boolValue ? "jei.config.value.boolean.true" : "jei.config.value.boolean.false");
		}
		if (value instanceof Enum<?> enumValue) {
			return getTranslatedEnumValue(configValue, enumValue, ".name")
				.orElseGet(() -> Component.literal(getDisplayName(enumValue.name())));
		}
		return Component.literal(String.valueOf(value));
	}

	private static Component getValueDescription(IJeiConfigValue<?> configValue, Object value) {
		if (value instanceof Boolean boolValue) {
			return Component.translatable(boolValue ? "jei.config.value.boolean.true.description" : "jei.config.value.boolean.false.description");
		}
		if (value instanceof Enum<?> enumValue) {
			return getTranslatedEnumValue(configValue, enumValue, ".description")
				.orElse(configValue.getLocalizedDescription());
		}
		return configValue.getLocalizedDescription();
	}

	private static Optional<Component> getTranslatedEnumValue(IJeiConfigValue<?> configValue, Enum<?> value, String suffix) {
		Language language = Language.getInstance();
		String valueName = value.name();
		Optional<String> configValueKey = getConfigValueTranslationKey(configValue);
		if (configValueKey.isPresent()) {
			String key = configValueKey.get() + ".value." + valueName + suffix;
			if (language.has(key)) {
				return Optional.of(Component.translatable(key));
			}
		}

		String enumKey = "jei.config.value." + value.getDeclaringClass().getSimpleName() + "." + valueName + suffix;
		if (language.has(enumKey)) {
			return Optional.of(Component.translatable(enumKey));
		}
		return Optional.empty();
	}

	private static Optional<String> getConfigValueTranslationKey(IJeiConfigValue<?> configValue) {
		if (configValue.getLocalizedName().getContents() instanceof TranslatableContents translatableContents) {
			return Optional.of(translatableContents.getKey());
		}
		return Optional.empty();
	}

	private static String getDisplayName(String name) {
		String[] words = name.toLowerCase().split("_");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty()) {
				continue;
			}
			if (!result.isEmpty()) {
				result.append(' ');
			}
			result.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() > 1) {
				result.append(word.substring(1));
			}
		}
		return result.toString();
	}
}
