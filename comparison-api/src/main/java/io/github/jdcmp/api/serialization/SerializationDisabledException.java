package io.github.jdcmp.api.serialization;

import java.io.ObjectStreamException;

/**
 * Indicates that an attempt was made to serialize an instance that does not permit serialization.
 */
public final class SerializationDisabledException extends ObjectStreamException {

	/**
	 * Constructs an exception with the default message.
	 */
	public SerializationDisabledException() {
		super("Serialization is disabled.");
	}

}
