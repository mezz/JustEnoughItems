package mezz.jei.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import mezz.jei.core.collect.Table;

public final class ReflectionUtil {
	private final Table<Class<?>, Class<?>, List<Field>> cache = Table.hashBasedTable();

	public <T> Stream<T> getFieldWithClass(Object object, Class<? extends T> fieldClass) {
		return getFieldsCached(object, fieldClass)
			.flatMap(field -> getFieldValue(object, field, fieldClass).stream());
	}

	private static <T> Optional<T> getFieldValue(Object object, Field field, Class<? extends T> fieldClass) {
		Object fieldValue;
		try {
			fieldValue = field.get(object);
		} catch (IllegalAccessException ignored) {
			return Optional.empty();
		}
		if (fieldClass.isInstance(fieldValue)) {
			T cast = fieldClass.cast(fieldValue);
			return Optional.of(cast);
		}
		return Optional.empty();
	}

	private Stream<Field> getFieldsCached(Object object, Class<?> fieldClass) {
		return cache.computeIfAbsent(fieldClass, object.getClass(), () -> getFieldUncached(object, fieldClass))
			.stream();
	}

	private List<Field> getFieldUncached(Object object, Class<?> fieldClass) {
		return allFields(object)
			.filter(field -> fieldClass.isAssignableFrom(field.getType()))
			.<Field>mapMulti((field, mapper) -> {
				try {
					field.setAccessible(true);
					mapper.accept(field);
				} catch (InaccessibleObjectException | SecurityException ignored) {

				}
			})
			.toList();
	}

	private Stream<Field> allFields(Object object) {
		Class<?> objectClass = object.getClass();
		List<Class<?>> classes = new ArrayList<>();
		do {
			classes.add(objectClass);
			objectClass = objectClass.getSuperclass();
		} while (objectClass != Object.class);

		return classes.stream()
			.flatMap(c -> {
				try {
					Field[] fields = c.getDeclaredFields();
					return Arrays.stream(fields);
				} catch (SecurityException ignored) {
					return Stream.of();
				}
			});
	}
}
