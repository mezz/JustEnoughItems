package mezz.jei.core.config.file.serializers;

public interface IConfigValueSerializer<T> {
    String serialize(T value);
    DeserializeResult<T> deserialize(String string);
    String getValidValuesDescription();
}
