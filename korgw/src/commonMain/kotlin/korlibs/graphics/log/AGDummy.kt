package korlibs.graphics.log

import korlibs.graphics.*

open class AGDummy(width: Int = 640, height: Int = 480) : AG() {
    init {
        mainFrameBuffer.setSize(width, height)
    }
    override val graphicExtensions: Set<String> get() = emptySet()
    override val isInstancedSupported: Boolean get() = true
    override val isFloatTextureSupported: Boolean get() = true
}
