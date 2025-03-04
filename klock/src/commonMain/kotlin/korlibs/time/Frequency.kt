package korlibs.time

import korlibs.time.hr.hr
import kotlin.jvm.JvmInline
import korlibs.time.internal.Serializable
import korlibs.time.internal.umod

val TimeSpan.hz get() = timesPerSecond
val Int.hz get() = timesPerSecond
val Double.hz get() = timesPerSecond

fun TimeSpan.toFrequency() = timesPerSecond

val TimeSpan.timesPerSecond get() = Frequency(1.0 / this.seconds)
val Int.timesPerSecond get() = Frequency(this.toDouble())
val Double.timesPerSecond get() = Frequency(this)

@JvmInline
value class Frequency(val hertz: Double) : Comparable<Frequency>, Serializable {
    companion object {
        fun from(timeSpan: TimeSpan) = timeSpan.toFrequency()
    }

    override fun compareTo(other: Frequency): Int = this.hertz.compareTo(other.hertz)

    operator fun unaryMinus() = Frequency(-this.hertz)
    operator fun unaryPlus() = this

    operator fun plus(other: Frequency): Frequency = Frequency(this.hertz + other.hertz)
    operator fun minus(other: Frequency): Frequency = Frequency(this.hertz - other.hertz)

    operator fun times(scale: Int): Frequency = Frequency(this.hertz * scale)
    operator fun times(scale: Float): Frequency = Frequency(this.hertz * scale)
    operator fun times(scale: Double): Frequency = Frequency(this.hertz * scale)

    operator fun div(scale: Int): Frequency = Frequency(this.hertz / scale)
    operator fun div(scale: Float): Frequency = Frequency(this.hertz / scale)
    operator fun div(scale: Double): Frequency = Frequency(this.hertz / scale)

    operator fun rem(other: Frequency): Frequency = Frequency(this.hertz % other.hertz)
    infix fun umod(other: Frequency): Frequency = Frequency(this.hertz umod other.hertz)

    val timeSpan get() = (1.0 / this.hertz).seconds
    val hrTimeSpan get() = (1.0 / this.hertz).seconds.hr
}
