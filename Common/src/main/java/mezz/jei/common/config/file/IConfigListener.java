package mezz.jei.common.config.file;

public interface IConfigListener<T> {
	void onConfigValueChanged(T configValue);
}
