package org.nanodb.orm.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.nanodb.internal.primitives.*;
import org.nanodb.orm.BaseModel;
import org.nanodb.orm.annotations.MaximumLength;
import org.nanodb.utils.Pair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ClassToDataType} covering primitive mapping, collections,
 * nested models, and the empty-model / unsupported-type edge cases.
 */
class ClassToDataTypeTest {

	// Simple model classes used for schema generation tests
	static class SimpleModel extends BaseModel {
		public int id;
		public String name;
	}

	static class WithVariousTypes extends BaseModel {
		public boolean active;
		public Integer count;
		@MaximumLength(50)
		public String shortText;
		public byte[] payload;
		public Date createdAt;
	}

	static class NestedChild extends BaseModel {
		public int childId;
	}

	static class WithNestedAndList extends BaseModel {
		public NestedChild child;
		public List<String> tags;
	}

	@Test
	@DisplayName("getDataTypeFromField maps primitive-like and core types correctly")
	void mapsPrimitiveAndCoreTypes() throws Exception {
		// boolean / Boolean
		Field boolField = WithVariousTypes.class.getDeclaredField("active");
		DataType<?> boolType = ClassToDataType.getDataTypeFromField(boolField.getGenericType(), boolField.getAnnotations());
		assertTrue(boolType instanceof BooleanType);

		// int / Integer
		Field countField = WithVariousTypes.class.getDeclaredField("count");
		DataType<?> intType = ClassToDataType.getDataTypeFromField(countField.getGenericType(), countField.getAnnotations());
		assertTrue(intType instanceof Int32Type);

		// String with @MaximumLength
		Field shortTextField = WithVariousTypes.class.getDeclaredField("shortText");
		DataType<?> shortTextType = ClassToDataType.getDataTypeFromField(shortTextField.getGenericType(), shortTextField.getAnnotations());
		assertTrue(shortTextType instanceof VarCharType);
		assertEquals(50, ((VarCharType) shortTextType).getLength());

		// byte[]
		Field payloadField = WithVariousTypes.class.getDeclaredField("payload");
		DataType<?> payloadType = ClassToDataType.getDataTypeFromField(payloadField.getGenericType(), payloadField.getAnnotations());
		assertTrue(payloadType instanceof BytesType);

		// Date
		Field createdAtField = WithVariousTypes.class.getDeclaredField("createdAt");
		DataType<?> dateType = ClassToDataType.getDataTypeFromField(createdAtField.getGenericType(), createdAtField.getAnnotations());
		assertTrue(dateType instanceof DateType);
	}

	@Test
	@DisplayName("getDataTypeFromField supports nested BaseModel and List types")
	void mapsNestedModelAndListTypes() throws Exception {
		// Nested BaseModel should delegate to getDataType(Class<? extends BaseModel>)
		Field childField = WithNestedAndList.class.getDeclaredField("child");
		DataType<?> childType = ClassToDataType.getDataTypeFromField(childField.getGenericType(), childField.getAnnotations());
		assertTrue(childType instanceof StructType);
		StructType childStruct = (StructType) childType;
		assertEquals(1, childStruct.getFields().size());
		assertEquals("childId", childStruct.getFields().get(0).getKey());

		// List<String> should produce an ArrayType of VarCharType
		Field tagsField = WithNestedAndList.class.getDeclaredField("tags");
		DataType<?> tagsType = ClassToDataType.getDataTypeFromField(tagsField.getGenericType(), tagsField.getAnnotations());
		assertTrue(tagsType instanceof ArrayType);
		ArrayType<?> arrayType = (ArrayType<?>) tagsType;
		assertTrue(arrayType.getElementType() instanceof VarCharType);
	}

	@Test
	@DisplayName("getDataType throws on unsupported field type")
	void throwsOnUnsupportedType() {
		class UnsupportedHolder {
			// Type not handled by ClassToDataType
			public Pair<String, Integer> unsupported;
		}

		Field unsupportedField;
		try {
			unsupportedField = UnsupportedHolder.class.getDeclaredField("unsupported");
		} catch (NoSuchFieldException e) {
			throw new RuntimeException(e);
		}

		Type type = unsupportedField.getGenericType();
		Annotation[] annotations = unsupportedField.getAnnotations();
		UnsupportedOperationException ex = assertThrows(
			UnsupportedOperationException.class,
			() -> ClassToDataType.getDataTypeFromField(type, annotations)
		);
		assertTrue(ex.getMessage().contains("This datatype is not supported"));
	}

	@Test
	@DisplayName("getDataType builds StructType with sorted fields and correct mappings")
	void getDataTypeBuildsStructType() {
		StructType struct = ClassToDataType.getDataType(SimpleModel.class);
		assertNotNull(struct);
		assertEquals(2, struct.getFields().size());

		// Fields should be sorted alphabetically by name
		assertEquals("id", struct.getFields().get(0).getKey());
		assertEquals("name", struct.getFields().get(1).getKey());

		assertTrue(struct.getFields().get(0).getValue() instanceof Int32Type);
		assertTrue(struct.getFields().get(1).getValue() instanceof VarCharType);
	}

	@Test
	@DisplayName("getDataType throws on BaseModel with no fields")
	void getDataTypeRejectsEmptyModel() {
		class EmptyModel extends BaseModel {
			// no fields
		}

		UnsupportedOperationException ex = assertThrows(
			UnsupportedOperationException.class,
			() -> ClassToDataType.getDataType(EmptyModel.class)
		);
		assertTrue(ex.getMessage().contains("Model must have at least one field"));
	}
}

