package mezz.jei.config;

import java.io.File;
import java.util.Arrays;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import mezz.jei.util.Translator;

public class LocalizedConfiguration {
	private static final String commentPostfix = ".comment";
	private final String keyPrefix;
	private final String defaultLocalized;
	private final String validLocalized;
	private final Configuration configuration;
	
	public LocalizedConfiguration(String keyPrefix, File file, String configVersion) {
		this.configuration = new Configuration(file, configVersion);
		this.keyPrefix = keyPrefix + '.';
		this.defaultLocalized = Translator.translateToLocal(this.keyPrefix + "default");
		this.validLocalized = Translator.translateToLocal(this.keyPrefix + "valid");
	}
	
	public void addCategory(String categoryName) {
		String categoryKey = keyPrefix + categoryName;
		String commentKey = categoryKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		configuration.setCategoryComment(categoryName, comment);
		configuration.setCategoryLanguageKey(categoryName, categoryKey);
	}

	public ConfigCategory getCategory(String categoryName) {
		return configuration.getCategory(categoryName);
	}
	
	public boolean getBoolean(String category, String name, boolean defaultValue) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		return configuration.getBoolean(name, category, defaultValue, comment, langKey);
	}
	
	public String getString(String name, String category, String defaultValue) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		return configuration.getString(name, category, defaultValue, comment, langKey);
	}
	
	public String getString(String name, String category, String defaultValue, String[] validValues) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		
		Property prop = configuration.get(category, name, defaultValue);
		prop.setValidValues(validValues);
		prop.setLanguageKey(langKey);
		prop.comment = comment + " [" + defaultLocalized + ": " + defaultValue + "] [" + validLocalized + ": " + Arrays.toString(prop.getValidValues()) + ']';
		return prop.getString();
	}
	
	public <T extends Enum<T>> T getEnum(String name, String category, T defaultValue, T[] validEnumValues) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		Property prop = configuration.get(category, name, defaultValue.name());
		
		String[] validValues = new String[validEnumValues.length];
		for (int i = 0; i < validEnumValues.length; i++) {
			T enumValue = validEnumValues[i];
			validValues[i] = enumValue.name();
		}
		
		prop.setValidValues(validValues);
		prop.setLanguageKey(langKey);
		prop.comment = comment + " [" + defaultLocalized + ": " + defaultValue + "] [" + validLocalized + ": " + Arrays.toString(prop.getValidValues()) + ']';
		String stringValue = prop.getString();
		
		T enumValue = defaultValue;
		for (int i = 0; i < validValues.length; i++) {
			if (stringValue.equals(validValues[i])) {
				enumValue = validEnumValues[i];
			}
		}
		
		return enumValue;
	}
	
	public String[] getStringList(String name, String category, String[] defaultValue) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);

		Property prop = configuration.get(category, name, defaultValue);
		prop.setLanguageKey(langKey);
		prop.comment = comment + " [" + defaultLocalized + ": " + Arrays.toString(defaultValue) + ']';
		return prop.getStringList();
	}
	
	public String[] getStringList(String name, String category, String[] defaultValue, String[] validValues) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		
		Property prop = configuration.get(category, name, defaultValue);
		prop.setLanguageKey(langKey);
		prop.setValidValues(validValues);
		prop.comment = comment + " [" + defaultLocalized + ": " + Arrays.toString(defaultValue) + "] [" + validLocalized + ": " + Arrays.toString(prop.getValidValues()) + ']';
		return prop.getStringList();
	}
	
	public float getFloat(String name, String category, float defaultValue, float minValue, float maxValue) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		return configuration.getFloat(name, category, defaultValue, minValue, maxValue, comment, langKey);
	}
	
	public int getInt(String name, String category, int defaultValue, int minValue, int maxValue) {
		String langKey = keyPrefix + category + '.' + name;
		String commentKey = langKey + commentPostfix;
		String comment = Translator.translateToLocal(commentKey);
		return configuration.getInt(name, category, defaultValue, minValue, maxValue, comment, langKey);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public boolean hasChanged() {
		return configuration.hasChanged();
	}

	public void save() {
		configuration.save();
	}

	@Override
	public String toString() {
		return configuration.toString();
	}
}
