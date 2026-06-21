package com.resolver

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

inline fun <T, reified R> KSerializer<T>.map(
    name: String,
    crossinline onDeserialize: (T) -> R,
    crossinline onSerialize: (R) -> T,
): KSerializer<R> = object : DelegatedSerializer<R, T>(name, this) {
    override fun onDeserialize(value: T) = onDeserialize(value)
    override fun onSerialize(value: R) = onSerialize(value)
}

abstract class DelegatedSerializer<T, D>(name: String, private val delegate: KSerializer<D>) :
    KSerializer<T> {
    override val descriptor: SerialDescriptor = SerialDescriptor(name, delegate.descriptor)
    protected abstract fun onDeserialize(value: D): T
    protected abstract fun onSerialize(value: T): D
    override fun deserialize(decoder: Decoder): T = onDeserialize(delegate.deserialize(decoder))
    override fun serialize(encoder: Encoder, value: T) {
        delegate.serialize(encoder, onSerialize(value))
    }
}