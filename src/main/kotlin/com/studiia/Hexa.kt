package com.studiia

import com.github.jershell.kbson.BsonEncoder
import com.github.jershell.kbson.FlexibleDecoder
import io.ktor.server.application.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.security.SecureRandom
import java.util.*
import kotlin.random.asKotlinRandom

val secureRandom = SecureRandom().asKotlinRandom()

@Serializable(with = Hexa.Serializer::class)
class Hexa private constructor(private val data: ByteArray) : Comparable<Hexa> {
	constructor() : this(secureRandom.nextBytes(15))
	constructor(source: String) : this(convert(source))

	override fun compareTo(other: Hexa): Int {
		for (index in data.indices) {
			val result = data[index] - other.data[index]
			if (result != 0) return result
		}
		return 0
	}

	override fun equals(other: Any?): Boolean =
		other === this || (other is Hexa && data.contentEquals(other.data))

	override fun hashCode(): Int = data.contentHashCode()
	override fun toString(): String = Base64.getEncoder().encodeToString(data)

	object Serializer : KSerializer<Hexa> {
		override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Hexa", PrimitiveKind.STRING)

		override fun deserialize(decoder: Decoder): Hexa {
			if (decoder is FlexibleDecoder) {
				val byteArray = decoder.reader.readBinaryData().data
				require(byteArray.size == 15) { "Hexa must be 15 bytes long" }
				return Hexa(byteArray)
			}
			return Hexa(decoder.decodeString())
		}

		override fun serialize(encoder: Encoder, value: Hexa) {
			if (encoder is BsonEncoder) encoder.encodeByteArray(value.data)
			else encoder.encodeString(value.toString())
		}
	}
}

private fun convert(source: String): ByteArray {
	require(source.length == 20) { "Invalid length" }
	return Base64.getDecoder().decode(source)
}

fun ApplicationCall.id(parameterName: String = "id") = parameters[parameterName]?.let { Hexa(it) }
	?: throw IllegalArgumentException("Missing parameter $parameterName")

fun ApplicationCall.idOrNull(parameterName: String = "id") = parameters[parameterName]?.let { Hexa(it) }