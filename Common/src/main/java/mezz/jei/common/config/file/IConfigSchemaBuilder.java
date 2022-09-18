package mezz.jei.common.config.file;

public interface IConfigSchemaBuilder {

    IConfigCategoryBuilder addCategory(String name);

    IConfigSchema build();
}
