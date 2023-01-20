package mezz.jei.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import mezz.jei.core.collect.Table;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ReflectionUtil {
	private static final Logger LOGGER = LogManager.getLogger();

	private final Table<Class<?>, Class<?>, List<Field>> cache = Table.hashBasedTable();

	public <T> Stream<T> getFieldWithClass(Object object, Class<? extends T> fieldClass) {
		return getFieldsCached(object, fieldClass)
			.flatMap(field -> getFieldValue(object, field, fieldClass).stream());
	}

	private static <T> Optional<T> getFieldValue(Object object, Field field, Class<? extends T> fieldClass) {
		Object fieldValue;
		try {
			fieldValue = field.get(object);
		} catch (IllegalAccessException e) {
			LOGGER.error("Failed to access field '" + field.getName() + "' for class " + object.getClass(), e);
			return Optional.empty();
		}
		if (fieldClass.isInstance(fieldValue)) {
			T cast = fieldClass.cast(fieldValue);
			return Optional.of(cast);
		}
		return Optional.empty();
	}

	private Stream<Field> getFieldsCached(Object object, Class<?> fieldClass) {
		return cache.computeIfAbsent(fieldClass, object.getClass(), () -> getFieldUncached(object, fieldClass).toList())
			.stream();
	}

	private static Stream<Field> getFieldUncached(Object object, Class<?> fieldClass) {
		return getAllFields(object)
			.filter(field -> fieldClass.isAssignableFrom(field.getType()))
			.mapMulti((field, mapper) -> {
				try {
					field.setAccessible(true);
					mapper.accept(field);
				} catch (InaccessibleObjectException | SecurityException e) {
					LOGGER.error("Failed to access field '" + field.getName() + "' for class " + object.getClass(), e);
				}
			});
	}

	private static Stream<Field> getAllFields(Object object) {
		Class<?> objectClass = object.getClass();
		List<Class<?>> classes = new ArrayList<>();
		while (objectClass != Object.class) {
			classes.add(objectClass);
			objectClass = objectClass.getSuperclass();
		}

		return classes.stream()
			.flatMap(c -> {
				try {
					Field[] fields = c.getDeclaredFields();
					return Arrays.stream(fields);
				} catch (SecurityException e) {
					LOGGER.error("Failed to access fields for class " + object.getClass(), e);
					return Stream.of();
				}
			});
	}
}
