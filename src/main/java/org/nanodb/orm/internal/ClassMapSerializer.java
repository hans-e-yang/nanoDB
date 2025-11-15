package org.nanodb.orm.internal;

import org.nanodb.utils.serializer.Serializer;
import org.nanodb.orm.BaseModel;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassMapSerializer<T extends BaseModel> implements Serializer<Map<String, Object>, T> {
    private final Class<T> type;

    public ClassMapSerializer(Class<T> type) {
        this.type = type;
    }

    @Override
    public Map<String, Object> deserialize(T value) {
        Map<String, Object> map = new HashMap<>();
        try {
            for (Field field : type.getDeclaredFields()) {
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                if (fieldValue instanceof BaseModel) {
                    // Recursively serialize nested BaseModel
                    Map<String, Object> nestedMap = new ClassMapSerializer<>((Class<BaseModel>) field.getType())
                            .deserialize((BaseModel) fieldValue);
                    map.put(field.getName(), nestedMap);
                } else {
                    map.put(field.getName(), fieldValue);
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public T serialize(Map<String, Object> value) {
        try {
            T instance = type.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : value.entrySet()) {
                Field field = type.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                Object fieldValue = entry.getValue();
                if (BaseModel.class.isAssignableFrom(field.getType())) {
                    Map<String, Object> nestedMap = (Map<String, Object>) fieldValue;
                    BaseModel nestedInstance = new ClassMapSerializer<>((Class<? extends BaseModel>) field.getType())
                            .serialize(nestedMap);
                    field.set(instance, nestedInstance);
                } else {
                    field.set(instance, fieldValue);
                }
            }
            return instance;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Error: BaseModel classes must have an empty constructor!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
