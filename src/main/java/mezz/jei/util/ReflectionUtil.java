package mezz.jei.util;

import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Field;
import java.util.Optional;

import mezz.jei.collect.Table;

public final class ReflectionUtil {

	private static final Table<Class<?>, Class<?>, Optional<Field>> CACHE = Table.hashBasedTable();

	private ReflectionUtil() {
	}

	@Nullable
	public static <T> T getFieldWithClass(final Object object, final Class<? extends T> fieldClass) {
		Field field = getField(object, fieldClass);
		if (field != null) {
			try {
				Object fieldValue = field.get(object);
				if (fieldClass.isInstance(fieldValue)) {
					return fieldClass.cast(fieldValue);
				}
			} catch (IllegalAccessException ignored) {

			}
		}

		return null;
	}

	@Nullable
	private static Field getField(final Object object, final Class<?> fieldClass) {
		Class<?> objectClass = object.getClass();
		Optional<Field> cachedField = CACHE.get(fieldClass, objectClass);
		//noinspection OptionalAssignedToNull
		if (cachedField != null) {
			return cachedField.orElse(null);
		}

		try {
			Field[] fields = objectClass.getDeclaredFields();
			for (Field field : fields) {
				if (fieldClass.isAssignableFrom(field.getType())) {
					//noinspection deprecation
					if (!field.isAccessible()) {
						field.setAccessible(true);
					}
					CACHE.put(fieldClass, objectClass, Optional.of(field));
					return field;
				}
			}
		} catch (SecurityException ignored) {

		}
		CACHE.put(fieldClass, objectClass, Optional.empty());
		return null;
	}

}
