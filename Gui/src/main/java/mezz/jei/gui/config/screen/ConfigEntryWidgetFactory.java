package mezz.jei.gui.config.screen;

import mezz.jei.api.runtime.config.IJeiConfigValue;
import mezz.jei.common.config.file.serializers.BooleanSerializer;
import mezz.jei.common.config.file.serializers.EnumSerializer;
import mezz.jei.common.config.file.serializers.IntegerSerializer;
import mezz.jei.common.config.file.serializers.ListSerializer;

import java.util.function.Consumer;

final class ConfigEntryWidgetFactory {
	private final Consumer<ConfigValueSelector<?>> valueSelectorOpener;
	private final Runnable layoutUpdater;

	ConfigEntryWidgetFactory(Consumer<ConfigValueSelector<?>> valueSelectorOpener, Runnable layoutUpdater) {
		this.valueSelectorOpener = valueSelectorOpener;
		this.layoutUpdater = layoutUpdater;
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	ConfigEntryWidget<?> create(IJeiConfigValue<?> value) {
		return switch (value.getSerializer()) {
			case BooleanSerializer ignored -> new BooleanConfigEntry((IJeiConfigValue<Boolean>) value);
			case IntegerSerializer ignored -> new IntegerConfigEntry((IJeiConfigValue<Integer>) value);
			case EnumSerializer ignored -> new EnumConfigEntry(value, valueSelectorOpener);
			case ListSerializer ignored -> new ListConfigEntry(value, valueSelectorOpener, layoutUpdater);
			default -> throw new UnsupportedOperationException("Unsupported serializer: " + value.getSerializer());
		};
	}
}
