package mezz.jei.gui.config;

import mezz.jei.common.config.IClientConfig;
import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.gui.api.ConfigValueApplyMode;
import net.mezzdev.config.gui.api.ConfigValueEditorType;
import net.mezzdev.config.gui.api.ConfigValueEditorTypes;
import net.mezzdev.config.gui.api.IConfigScreenValue;
import net.mezzdev.config.gui.api.IConfigValueEditorMetadata;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Controls when recipe bookmark previews appear. */
final class BookmarkPreviewConfigValue implements IConfigScreenValue<BookmarkPreviewConfigValue.Mode> {
	private static final IConfigValueEditorMetadata<Mode> METADATA = new Metadata();
	private final IConfigValue<Boolean> preview;
	private final IConfigValue<Boolean> requireKey;

	BookmarkPreviewConfigValue(IClientConfig config) {
		preview = config.bookmarkTooltipPreviewEnabled();
		requireKey = config.holdShiftToShowBookmarkTooltipFeaturesEnabled();
	}

	@Override
	public String getName() {
		return "bookmarkPreviewMode";
	}

	@Override
	public String getLocalizationKey() {
		return "jei.config.client.bookmarkList.bookmarkPreviewMode";
	}

	@Override
	public Mode getValue() {
		return Mode.from(preview.get(), requireKey.get());
	}

	@Override
	public Mode getDefaultValue() {
		return Mode.from(preview.getEditorInfo().getDefaultValue(), requireKey.getEditorInfo().getDefaultValue());
	}

	@Override
	public boolean set(Mode value) {
		if (!METADATA.isValid(value)) {
			throw new IllegalArgumentException("Invalid bookmark preview mode: " + value);
		}
		boolean keyChanged = false;
		if (value != Mode.DISABLED) {
			keyChanged = requireKey.set(value == Mode.SHIFT);
		}
		boolean previewChanged = preview.set(value != Mode.DISABLED);
		return keyChanged || previewChanged;
	}

	@Override
	public Runnable addListener(Consumer<Mode> listener) {
		Runnable removePreview = preview.addListener(change -> listener.accept(getValue()));
		Runnable removeKey = requireKey.addListener(change -> listener.accept(getValue()));
		return () -> {
			removePreview.run();
			removeKey.run();
		};
	}

	@Override
	public ConfigValueApplyMode getApplyMode() {
		return ConfigValueApplyMode.IMMEDIATE;
	}

	@Override
	public IConfigValueEditorMetadata<Mode> getEditorMetadata() {
		return METADATA;
	}

	enum Mode {
		DISABLED,
		SHIFT,
		ALWAYS;

		static Mode from(boolean preview, boolean requireKey) {
			if (!preview) {
				return DISABLED;
			}
			if (requireKey) {
				return SHIFT;
			}
			return ALWAYS;
		}
	}

	private static final class Metadata implements IConfigValueEditorMetadata<Mode> {
		@Override
		public ConfigValueEditorType<Mode> getEditorType() {
			return ConfigValueEditorTypes.getSelection();
		}

		@Override
		public boolean isValid(Mode value) {
			return value != null;
		}

		@Override
		public Optional<List<Mode>> getAllValidValues() {
			return Optional.of(List.of(Mode.values()));
		}

		@Override
		public Component getLocalizedValueName(String key, Mode value) {
			return Component.translatable(key + ".value." + value.name());
		}

		@Override
		public Optional<Component> getLocalizedValueDescription(String key, Mode value) {
			return Optional.of(Component.translatable(key + ".value." + value.name() + ".description"));
		}
	}
}
