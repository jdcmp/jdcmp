package io.github.jdcmp.codegen;

import java.io.*;

public final class SerializationUtils {

	public static <T> T copy(Serializable serializable) throws IOException {
		return deserialize(serialize(serializable));
	}

	public static byte[] serialize(Serializable serializable) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(serializable);
			return baos.toByteArray();
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(byte[] bytes) throws IOException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes); ObjectInputStream ois = new ObjectInputStream(bais)) {
			return (T) ois.readObject();
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private SerializationUtils() {
		throw new AssertionError("No instances");
	}

}
