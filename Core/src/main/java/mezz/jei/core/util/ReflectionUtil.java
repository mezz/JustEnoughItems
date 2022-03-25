package mezz.jei.core.util;

import java.lang.reflect.Field;
import java.util.Optional;

import mezz.jei.core.collect.Table;

public final class ReflectionUtil {
	private final Table<Class<?>, Class<?>, Optional<Field>> cache = Table.hashBasedTable();

	public <T> Optional<T> getFieldWithClass(Object object, Class<? extends T> fieldClass) {
		return getField(object, fieldClass)
			.flatMap(field -> getFieldValue(object, field, fieldClass));
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

	private Optional<Field> getField(final Object object, final Class<?> fieldClass) {
		Class<?> objectClass = object.getClass();
		return cache.computeIfAbsent(fieldClass, objectClass, () -> getFieldUncached(objectClass, fieldClass));
	}

	private Optional<Field> getFieldUncached(final Class<?> objectClass, final Class<?> fieldClass) {
		try {
			Field[] fields = objectClass.getDeclaredFields();
			for (Field field : fields) {
				if (fieldClass.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					return Optional.of(field);
				}
			}
		} catch (SecurityException ignored) {

		}
		return Optional.empty();
	}
}
