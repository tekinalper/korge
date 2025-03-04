package korlibs.graphics.gl

import korlibs.kgl.*
import korlibs.graphics.*

actual object AGOpenglFactory {
	actual fun create(nativeComponent: Any?): AGFactory = AGFactoryNative
	actual val isTouchDevice: Boolean = false
}

object AGFactoryNative : AGFactory {
	override val supportsNativeFrame: Boolean = false
	override fun create(nativeControl: Any?, config: AGConfig): AG = AGOpengl(KmlGlNative())
	override fun createFastWindow(title: String, width: Int, height: Int): AGWindow = TODO()
}

fun AGNative(gl: KmlGl = korlibs.kgl.KmlGlNative()): AGOpengl = AGOpengl(gl)
