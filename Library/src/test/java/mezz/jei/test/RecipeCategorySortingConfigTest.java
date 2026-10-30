package mezz.jei.test;

import mezz.jei.api.recipe.types.IRecipeType;
import mezz.jei.library.config.RecipeCategorySortingConfig;
import net.mezzdev.config.file.ConfigFileWatcherSettings;
import net.mezzdev.config.file.ConfigManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecipeCategorySortingConfigTest {
	@Test
	void preservesHiddenCategoriesWhenNewTypesAreRegistered(@TempDir Path directory) {
		var disabledWatcher = ConfigFileWatcherSettings.clientDefaults().withEnabled(false);
		var manager = new ConfigManager("Recipe category visibility test", disabledWatcher, disabledWatcher);
		Path path = directory.resolve("categories.ini");
		var config = manager.createSortingConfig(path, Comparator.<String>naturalOrder(), true);
		config.setSortedValues(List.of("test:first", "test:second"), List.of("test:first"));

		var reloadedManager = new ConfigManager("Reloaded category visibility test", disabledWatcher, disabledWatcher);
		var reloaded = reloadedManager.createSortingConfig(path, Comparator.<String>naturalOrder(), true);
		var sorting = new RecipeCategorySortingConfig(reloaded);
		IRecipeType<?> first = IRecipeType.create("test", "first", String.class);
		IRecipeType<?> second = IRecipeType.create("test", "second", String.class);
		IRecipeType<?> added = IRecipeType.create("test", "added", String.class);
		List<IRecipeType<?>> types = List.of(first, second, added);

		var before = sorting.getSnapshot(types);
		assertTrue(before.isVisible(first));
		assertTrue(before.isVisible(added));
		assertFalse(before.isVisible(second));
		assertEquals(List.of(first, added, second), types.stream().sorted(before.getComparator()).toList());

		reloaded.setSortedValues(List.of("test:first", "test:second", "test:added"), List.of("test:second"));
		var after = sorting.getSnapshot(types);
		assertFalse(after.isVisible(first));
		assertFalse(after.isVisible(added));
		assertTrue(after.isVisible(second));
		assertEquals(List.of(second, added, first), types.stream().sorted(after.getComparator()).toList());
		assertTrue(before.isVisible(first));
		assertFalse(before.isVisible(second));
		assertEquals(List.of(first, added, second), types.stream().sorted(before.getComparator()).toList());
	}
}
