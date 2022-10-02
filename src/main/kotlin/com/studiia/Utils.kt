package com.studiia

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import mu.KotlinLogging
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.time.Duration

val logger = KotlinLogging.logger {}
val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

private val passwordEncoder: PasswordEncoder = Argon2PasswordEncoder()

suspend fun String.encode(): String = withIO { passwordEncoder.encode(this@encode) }

suspend infix fun String.encodesTo(other: String): Boolean = withIO {
	passwordEncoder.matches(this@encodesTo, other)
}

suspend inline fun <T> withIO(crossinline function: suspend () -> T): T = withContext(Dispatchers.IO) { function() }

object DurationSerializer : KSerializer<Duration> {
	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Duration", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: Duration) {
		encoder.encodeString(value.toString())
	}

	override fun deserialize(decoder: Decoder): Duration {
		return Duration.parse(decoder.decodeString())
	}
}