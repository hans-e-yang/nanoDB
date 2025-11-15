package org.nanodb.orm.internal;

import org.nanodb.internal.primitives.*;
import org.nanodb.orm.BaseModel;
import org.nanodb.orm.annotations.MaximumLength;
import org.nanodb.utils.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class ClassToDataType {
    // helper functions to manage the MaximumLengthAnnotation
    static int getLengthAnnotationOrDefault(Annotation[] annotations) {
        return getLengthAnnotationOrDefault(annotations, MaximumLength.DEFAULT_LENGTH);
    }
    static int getLengthAnnotationOrDefault(Annotation[] annotations, int otherwise) {
        MaximumLength annotation = (MaximumLength) Arrays.stream(annotations)
                .filter(a -> a instanceof MaximumLength)
                .findFirst()
                .orElse(null);
        return annotation != null ? annotation.maxLength() : otherwise;
    }


    public static DataType<?> getDataTypeFromField(Type type, Annotation[] annotations) {
        Class<?> rawType;
        if (type instanceof ParameterizedType) rawType = (Class<?>) ((ParameterizedType) type).getRawType();
        else rawType = (Class<?>) type;

        boolean isBooleanType = boolean.class.equals(rawType) || Boolean.class.isAssignableFrom(rawType);
        if (isBooleanType) return new BooleanType();
        boolean isIntType = int.class.equals(rawType) || Integer.class.isAssignableFrom(rawType);
        if (isIntType) return new Int32Type();
        if (String.class.isAssignableFrom(rawType)) return new VarCharType(getLengthAnnotationOrDefault(annotations));
        if (byte[].class.isAssignableFrom(rawType)) return new BytesType(getLengthAnnotationOrDefault(annotations));
        if (Date.class.isAssignableFrom(rawType)) return new DateType();
        if (BaseModel.class.isAssignableFrom(rawType)) return getDataType((Class<? extends BaseModel>)rawType);
        if (List.class.isAssignableFrom(rawType)) {
            Type tableType = ((ParameterizedType)type).getActualTypeArguments()[0];
            return new ArrayType<>(
                    getDataTypeFromField(tableType, new Annotation[]{}),
                    getLengthAnnotationOrDefault(annotations)
            );
        }

        throw new UnsupportedOperationException("This datatype is not supported by the ORM system: " + type.getTypeName());
    }

    // Class whose purpose is only to sort fields by their name
    static class FieldComparator implements Comparator<Field> {
        // Just a minor optimization, mimicking the singleton pattern
        // This function holds no state anyway, so it isn't a problem to expose it.
        public static final FieldComparator instance = new FieldComparator();

        @Override
        public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
    public static StructType getDataType(Class<? extends BaseModel> objClass) {
        List<Pair<String, DataType<?>>> fields = new ArrayList<>();

        // Sort the fields to have a predictable order
        Field[] objFields = (Field[]) Arrays.stream(objClass.getFields()).sorted(FieldComparator.instance).toArray();

        // If the type of the field is a subtype, or equal to String
        for (Field field : objFields) {
            fields.add(
                    new Pair<>(field.getName(), getDataTypeFromField(field.getType(), field.getAnnotations()))
            );
        }

        return new StructType(fields);
    }
}
