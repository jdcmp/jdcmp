package io.github.jdcmp.api.serialization;

import java.io.InvalidObjectException;

/**
 * Indicates that an attempt was made to circumvent the serialization proxy.
 */
public final class SerializationProxyRequiredException extends InvalidObjectException {

	/**
	 * Creates an exception with the default message.
	 */
	public SerializationProxyRequiredException() {
		super("Serialization is only possible via some serialization proxy.");
	}

}
