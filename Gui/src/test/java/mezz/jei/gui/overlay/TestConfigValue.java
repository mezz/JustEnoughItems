package mezz.jei.gui.overlay;

import net.mezzdev.config.api.value.IConfigValue;
import net.mezzdev.config.api.value.change.IConfigValueBatchChangeListener;
import net.mezzdev.config.api.value.change.IConfigValueChangeListener;
import net.mezzdev.config.api.value.editor.IConfigValueEditorInfo;

public final class TestConfigValue<T> implements IConfigValue<T> {
	private T value;

	public TestConfigValue(T value) {
		this.value = value;
	}

	@Override
	public T get() {
		return value;
	}

	@Override
	public boolean set(T value) {
		this.value = value;
		return true;
	}

	@Override
	public Runnable addListener(IConfigValueChangeListener<T> listener) {
		return () -> {};
	}

	@Override
	public Runnable addBatchListener(IConfigValueBatchChangeListener listener) {
		return () -> {};
	}

	@Override
	public IConfigValueEditorInfo<T> getEditorInfo() {
		throw new UnsupportedOperationException("Test config values do not have editor metadata");
	}
}
