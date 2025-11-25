package org.nanodb.orm.internal;

import org.junit.jupiter.api.Test;
import org.nanodb.orm.BaseModel;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClassMapSerializerTest {

	// Simple model with primitive fields only
	public static class SimpleModel extends BaseModel {
		public String name;
		public int age;

		// empty constructor required by serializer
		public SimpleModel() {}
	}

	// Model that contains a nested BaseModel instance
	public static class ParentModel extends BaseModel {
		public String id;
		public SimpleModel child;

		public ParentModel() {}
	}

	@Test
	public void deserializeFlattensModelToMap() {
		SimpleModel model = new SimpleModel();
		model.name = "Alice";
		model.age = 30;

		ClassMapSerializer<SimpleModel> serializer = new ClassMapSerializer<>(SimpleModel.class);
		Map<String, Object> map = serializer.deserialize(model);

		assertEquals("Alice", map.get("name"));
		assertEquals(30, map.get("age"));
	}

	@Test
	public void serializeReconstructsModelFromMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("name", "Bob");
		map.put("age", 25);

		ClassMapSerializer<SimpleModel> serializer = new ClassMapSerializer<>(SimpleModel.class);
		SimpleModel model = serializer.serialize(map);

		assertEquals("Bob", model.name);
		assertEquals(25, model.age);
	}

	@Test
	public void nestedBaseModelIsSerializedAndDeserialized() {
		ParentModel parent = new ParentModel();
		parent.id = "p1";
		parent.child = new SimpleModel();
		parent.child.name = "Carol";
		parent.child.age = 40;

		// Serialize parent (including nested child)
		ClassMapSerializer<ParentModel> parentSerializer = new ClassMapSerializer<>(ParentModel.class);
		Map<String, Object> parentMap = parentSerializer.deserialize(parent);

		assertEquals("p1", parentMap.get("id"));
		assertTrue(parentMap.get("child") instanceof Map);

		@SuppressWarnings("unchecked")
		Map<String, Object> childMap = (Map<String, Object>) parentMap.get("child");
		assertEquals("Carol", childMap.get("name"));
		assertEquals(40, childMap.get("age"));

		// Now serialize back into a ParentModel and verify nested object
		ParentModel reconstructed = parentSerializer.serialize(parentMap);
		assertNotNull(reconstructed.child);
		assertEquals("p1", reconstructed.id);
		assertEquals("Carol", reconstructed.child.name);
		assertEquals(40, reconstructed.child.age);
	}

	// Note: error paths (e.g., missing empty constructor) are harder to hit cleanly
	// without creating deliberately invalid BaseModel types. The happy paths above
	// cover the reflective field access and nested BaseModel handling.
}

