/*
    Unigrid Hedgehog
    Copyright © 2021-2022 The Unigrid Foundation, UGD Software AB

    This program is free software: you can redistribute it and/or modify it under the terms of the
    addended GNU Affero General Public License as published by the The Unigrid Foundation and
    the Free Software Foundation, version 3 of the License (see COPYING and COPYING.addendum).

    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
    even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Affero General Public License and the addendum for more details.

    You should have received an addended copy of the GNU Affero General Public License with this program.
    If not, see <http://www.gnu.org/licenses/> and <https://github.com/unigrid-project/hedgehog>.
*/

package org.unigrid.hedgehog.model.spork;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.experimental.Tolerate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.unigrid.hedgehog.model.Network;
import org.unigrid.hedgehog.model.Signable;
import org.unigrid.hedgehog.model.Signature;
import org.unigrid.hedgehog.model.network.chunk.ChunkData;

@Data
@Slf4j
public class GridSpork implements Serializable, Signable {
	private Instant timeStamp;
	private Instant previousTimeStamp;
	private short flags; /* Put Flag values in here */

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Type type;

	private ChunkData data;
	private ChunkData previousData; /* Flag.DELTA controls the content */

	@Getter private byte[] signature;

	@AllArgsConstructor
	public enum Flag {
		GOVERNED((short) 0x01),	/* Governed sporks have to be voted on to accept the change on the network */
		DELTA((short) 0x02);	/* Is either delta-data or a raw representation of the previous value */

		@Getter private final short value;
	}

	@AllArgsConstructor
	public enum Type {
		UNDEFINED((short) 0), MINT_STORAGE((short) 1000), MINT_SUPPLY((short) 1010), VESTING_STORAGE((short) 1020);

		@Getter private final short value;

		public static Type get(short value) {
			switch (value) {
				case 1000: return MINT_STORAGE;
				case 1010: return MINT_SUPPLY;
				case 1020: return VESTING_STORAGE;
				default: return UNDEFINED;
			}
		}
	}

	public <T extends ChunkData> T getData() {
		return (T) data;
	}

	public <T extends ChunkData> T getPreviousData() {
		return (T) previousData;
	}

	@Tolerate
	public void setType(short value) {
		type = Type.get(value);
	}

	public static GridSpork create(Type type) {
		switch (type) {
			case MINT_STORAGE: return new MintStorage();
			case MINT_SUPPLY: return new MintSupply();
			case VESTING_STORAGE: return new VestingStorage();
			default: throw new IllegalArgumentException("Unknown spork type supplied");
		}
	}

	@JsonIgnore
	public boolean isNewerThan(GridSpork otherSpork) {
		if (Objects.isNull(otherSpork) || Objects.isNull(otherSpork.timeStamp)) {
			return Objects.nonNull(timeStamp);
		}

		if (Objects.isNull(timeStamp)) {
			return !(Objects.nonNull(otherSpork) && Objects.nonNull(otherSpork.timeStamp));
		}

		return timeStamp.isAfter(otherSpork.timeStamp);
	}

	@Override
	@JsonIgnore
	public byte[] getSignable() {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();

		stream.writeBytes(SerializationUtils.serialize(timeStamp));
		stream.writeBytes(SerializationUtils.serialize(previousTimeStamp));
		stream.writeBytes(SerializationUtils.serialize(flags));
		stream.writeBytes(SerializationUtils.serialize(type));
		stream.writeBytes(SerializationUtils.serialize(data));
		stream.writeBytes(SerializationUtils.serialize(previousData));

		return stream.toByteArray();
	}

	@JsonIgnore
	public boolean isValidSignature() {
		try {
			for (String key : Network.KEYS) {
				if (Signature.verify(this, key)) {
					return true;
				}
			}
		} catch (InvalidAlgorithmParameterException | InvalidKeyException | InvalidKeySpecException
			| NoSuchAlgorithmException | SignatureException ex) {

			log.atTrace().log("{}:{}", ex.getMessage(), ExceptionUtils.getStackTrace(ex));
		}

		return false;
	}
}
