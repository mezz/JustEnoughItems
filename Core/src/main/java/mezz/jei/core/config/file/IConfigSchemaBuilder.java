package mezz.jei.core.config.file;

public interface IConfigSchemaBuilder {

    IConfigCategoryBuilder addCategory(String name);

    IConfigSchema build();
}
