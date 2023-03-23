package com.soywiz.korge.view.debug

import com.soywiz.kds.iterators.fastForEach
import com.soywiz.korag.*
import com.soywiz.korag.shader.*
import com.soywiz.korge.render.RenderContext
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.View
import com.soywiz.korge.view.ViewDslMarker
import com.soywiz.korge.view.addTo
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korma.geom.BoundsBuilder
import com.soywiz.korma.geom.IVectorArrayList
import com.soywiz.korma.geom.fastForEachGeneric
import com.soywiz.korma.geom.toMatrix4

inline fun Container.debugVertexView(
    pointsList: List<IVectorArrayList> = listOf(),
    color: RGBA = Colors.WHITE,
    type: AGDrawType = AGDrawType.TRIANGLE_STRIP,
    callback: @ViewDslMarker DebugVertexView.() -> Unit = {}
): DebugVertexView = DebugVertexView(pointsList, color, type).addTo(this, callback)

class DebugVertexView(pointsList: List<IVectorArrayList>, color: RGBA = Colors.WHITE, type: AGDrawType = AGDrawType.TRIANGLE_STRIP) : View() {
    init {
        colorMul = color
    }
    var color: RGBA by this::colorMul

    object UB : UniformBlock(fixedLocation = 6) {
        val u_Col by vec4()
        val u_Matrix by mat4()
    }

    companion object {
        //val u_Col: Uniform get() = UB.u_Col.uniform
        //val u_Matrix: Uniform get() = UB.u_Matrix.uniform
        val PROGRAM = DefaultShaders.PROGRAM_DEBUG_WITH_PROJ.copy(
            vertex = VertexShaderDefault {
                SET(out, u_ProjMat * u_ViewMat * UB.u_Matrix * vec4(a_Pos, 0f.lit, 1f.lit))
            },
            fragment = FragmentShader {
                SET(out, UB.u_Col)
            }
        )
    }

    var pointsList: List<IVectorArrayList> = pointsList
        set(value) {
            if (field !== value) {
                field = value
                updatedPoints()
            }
        }

    var type: AGDrawType = type
    class Batch(val offset: Int, val count: Int)
    var buffer: FloatArray = floatArrayOf(0f, 0f, 100f, 0f, 0f, 100f, 100f, 100f)
    val batches = arrayListOf<Batch>()
    private val bb = BoundsBuilder()

    private fun updatedPoints() {
        this.buffer = FloatArray(pointsList.sumOf { it.size } * 2)
        val buffer = this.buffer
        var n = 0
        batches.clear()
        bb.reset()
        pointsList.fastForEach { points ->
            batches.add(Batch(n / 2, points.size))
            if (points.dimensions >= 5) {
                points.fastForEachGeneric {
                    val x = this[it, 0]
                    val y = this[it, 1]
                    val dx = this[it, 2]
                    val dy = this[it, 3]
                    val scale = this[it, 4]
                    val px = x + dx * scale
                    val py = y + dy * scale
                    buffer[n++] = px
                    buffer[n++] = py
                    bb.add(px, py)
                }
            } else {
                points.fastForEachGeneric {
                    val x = this[it, 0]
                    val y = this[it, 1]
                    buffer[n++] = x
                    buffer[n++] = y
                    bb.add(x, y)
                }
            }
        }
    }

    override fun getLocalBoundsInternal() = bb.getBounds().immutable
    //println("DebugVertexView.getLocalBoundsInternal:$out")

    override fun renderInternal(ctx: RenderContext) {
        ctx.flush()
        ctx.updateStandardUniforms()
        ctx[UB].push {
            it[u_Col] = renderColorMul
            it[u_Matrix] = globalMatrix.toMatrix4()
        }
        ctx.dynamicVertexBufferPool.alloc { vb ->
            vb.upload(this@DebugVertexView.buffer)
            val vData = AGVertexArrayObject(
                AGVertexData(DefaultShaders.LAYOUT_DEBUG, vb)
            )
            batches.fastForEach { batch ->
                ctx.ag.draw(
                    ctx.currentFrameBuffer,
                    vData,
                    drawType = type,
                    program = PROGRAM,
                    newUniformBlocks = ctx.createCurrentUniformsRef(PROGRAM),
                    vertexCount = batch.count,
                    drawOffset = batch.offset,
                    blending = renderBlendMode.factors
                )
            }
        }
    }

    init {
        updatedPoints()
    }
}
