package korlibs.korge.view

import korlibs.graphics.*
import korlibs.graphics.shader.*
import korlibs.korge.internal.*
import korlibs.korge.render.*
import korlibs.image.bitmap.*
import korlibs.math.geom.*
import korlibs.math.geom.vector.*

/**
 * [RectBase] is an abstract [Container] [View] that represents something with a Rect-like shape: like a [SolidRect] or an [Image].
 * It supports anchoring [anchorX] and [anchorY] ratios [0..1] for anchoring this rectangle, and handles pre-computing of vertices for performance.
 */
@OptIn(KorgeInternal::class)
open class RectBase(
	anchorX: Double = 0.0,
	anchorY: Double = anchorX,
	hitShape: VectorPath? = null,
	var smoothing: Boolean = true
) : View(), Anchorable {
    init {
        this.hitShape = hitShape
    }

	//abstract val width: Double
	//abstract val height: Double

    protected var baseBitmap: BitmapCoords = Bitmaps.white
        set(v) {
            if (field !== v) {
                field = v
                dirtyVertices = true
                invalidateRender()
            }
        }

	override var anchorX: Double = anchorX; set(v) { if (field != v) { field = v; dirtyVertices = true; invalidateRender() } }
    override var anchorY: Double = anchorY; set(v) { if (field != v) { field = v; dirtyVertices = true; invalidateRender() } }

    protected open val bwidth: Double get() = 0.0
	protected open val bheight: Double get() = 0.0

    override val anchorDispX: Double get() = (anchorX * bwidth)
    override val anchorDispY: Double get() = (anchorY * bheight)

    protected open val sLeft: Double get() = -anchorDispX
	protected open val sTop: Double get() = -anchorDispY

	val sRight: Double get() = sLeft + bwidth
	val sBottom: Double get() = sTop + bheight

    protected val vertices = TexturedVertexArray(4, TexturedVertexArray.QUAD_INDICES)

	override fun renderInternal(ctx: RenderContext) {
		if (!visible) return
        if (baseBitmap === Bitmaps.transparent) return
        if (dirtyVertices) {
            dirtyVertices = false
            computeVertices()
        }
        //println("$name: ${vertices.str(0)}, ${vertices.str(1)}, ${vertices.str(2)}, ${vertices.str(3)}")
        drawVertices(ctx)
        //super.renderInternal(ctx)
	}

    var program: Program? = null
    //private var _programUniforms: AGUniformValues? = null
    var updateProgramUniforms: ((RenderContext) -> Unit)? = null
    //var programUniforms: AGUniformValues
    //    set(value) { _programUniforms = value }
    //    get()  {
    //        if (_programUniforms == null) _programUniforms = AGUniformValues()
    //        return _programUniforms!!
    //    }

    protected open fun drawVertices(ctx: RenderContext) {
        ctx.useBatcher { batch ->
            //batch.texture1212
            //batch.setTemporalUniforms(_programUniforms) {
            updateProgramUniforms?.invoke(ctx)
            batch.drawVertices(
                vertices, ctx.getTex(baseBitmap).base, smoothing, renderBlendMode,
                program = program,
            )
            //}
        }
    }

    protected open fun computeVertices() {
        vertices.quad(0, sLeft, sTop, bwidth, bheight, globalMatrix, baseBitmap, renderColorMul)
    }

	override fun getLocalBoundsInternal() = Rectangle(sLeft, sTop, bwidth, bheight)

    /*
	override fun hitTest(x: Double, y: Double): View? {
		val lres = if (checkGlobalBounds(x, y, sLeft, sTop, sRight, sBottom) &&
			(hitShape?.containsPoint(globalToLocalX(x, y), globalToLocalY(x, y)) != false)
		) this else null
		return lres ?: super.hitTestInternal(x, y)
	}
    */

	//override fun hitTestInternal(x: Double, y: Double): View? {
	//	return if (checkGlobalBounds(x, y, sLeft, sTop, sRight, sBottom) &&
	//		(hitShape?.containsPoint(globalToLocalX(x, y), globalToLocalY(x, y)) != false)
	//	) this else null
	//}

	override fun toString(): String {
		var out = super.toString()
		if (anchorX != 0.0 || anchorY != 0.0) out += ":anchor=(${anchorX.str}, ${anchorY.str})"
		return out
	}
}
