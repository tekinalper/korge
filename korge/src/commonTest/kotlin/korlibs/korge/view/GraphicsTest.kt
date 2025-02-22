package korlibs.korge.view

import assertEqualsFloat
import korlibs.memory.*
import korlibs.graphics.log.AGLog
import korlibs.korge.render.RenderContext
import korlibs.image.color.Colors
import korlibs.io.async.suspendTest
import korlibs.io.util.niceStr
import korlibs.math.geom.*
import korlibs.math.geom.bezier.Bezier
import korlibs.math.geom.vector.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GraphicsTest {
    @Test
    fun test() = suspendTest({ !Platform.isAndroid }) {
        val g = CpuGraphics().updateShape(redrawNow = true) {
            fill(Colors.RED) {
                rect(-50, -50, 100, 100)
            }
        }
        val bmp = g.bitmap.base.toBMP32()
        assertEquals(SizeInt(101, 101), bmp.size)
        //assertEquals("#ff0000ff", bmp[0, 0].hexString)
        //assertEquals("#ff0000ff", bmp[99, 99].hexString)
        assertEquals("#ff0000ff", bmp[1, 1].hexString)
        assertEquals("#ff0000ff", bmp[98, 98].hexString)
        assertEquals(-50.0, g._sLeft)
        assertEquals(-50.0, g._sTop)
    }

    @Test
    fun testEmptyGraphics() = suspendTest({ !Platform.isAndroid }) {
        val g = CpuGraphics().apply {
        }
        val rc = TestRenderContext()
        g.render(rc)
        val bmp = g.bitmap.base.toBMP32()
        // This would fail in Android
        assertTrue(bmp.width > 0)
        assertTrue(bmp.height > 0)
    }

    @Test
    fun testGraphicsSize() {
        CpuGraphics().updateShape {
            fill(Colors.RED) {
                rect(0, 0, 100, 100)
            }
        }.also { g ->
            assertEquals(100.0, g.width)
            assertEquals(100.0, g.height)
        }
        CpuGraphics().updateShape {
            fill(Colors.RED) {
                rect(10, 10, 100, 100)
            }
        }.also { g ->
            assertEquals(100.0, g.width)
            assertEquals(100.0, g.height)
        }
    }

    //@Test
    //fun testPathPool() {
    //    val g = Graphics()
    //    g.updateShape {
    //        for (n in 0 until 10) {
    //            fill(Colors.RED) { rect(0, 0, 100, 100) }
    //            clear()
    //        }
    //    }
    //    assertEquals(1, g.vectorPathPool.itemsInPool)
    //    g.updateShape {
    //        for (n in 0 until 10) fill(Colors.RED) { rect(0, 0, 100, 100) }
    //    }
    //    assertEquals(10, g.vectorPathPool.itemsInPool)
    //    assertNotSame(g.vectorPathPool.alloc(), g.vectorPathPool.alloc())
    //}

    @Test
    fun testCollidesWithShape() {
        lateinit var circle1: Circle
        lateinit var circle2: Circle
        val container = Container().apply {
            scale(1.2)
            circle1 = circle(128.0, Colors.RED)
                .scale(0.75)
                .anchor(Anchor.MIDDLE_CENTER)
            circle2 = circle(64.0, Colors.BLUE)
                .scale(1.5)
                .anchor(Anchor.MIDDLE_CENTER)
        }
        assertEquals(true, circle1.xy(0, 0).collidesWithShape(circle2.xy(0, 0)))
        assertEquals(true, circle1.xy(180, 0).collidesWithShape(circle2.xy(0, 0)))
        assertEquals(false, circle1.xy(200, 0).collidesWithShape(circle2.xy(0, 0)))
        assertEquals(true, circle1.xy(160, 100).collidesWithShape(circle2.xy(0, 0)))
        assertEquals(false, circle1.xy(167, 100).collidesWithShape(circle2.xy(0, 0)))
    }

    @Test
    fun testMultiShapeHitTesting() {
        val graphics = CpuGraphics().updateShape {
            fill(Colors.RED) {
                circle(Point(0, 0), 32f)
            }
            fill(Colors.BLUE) {
                circle(Point(0, 0), 16f)
            }
        }
        //println(graphics.hitShape2d)
        assertNotNull(graphics.hitShape2d, "hitShape2d should be defined")
        assertEquals(graphics, graphics.hitTestLocal(Point(0, 0)))
        assertEquals(graphics, graphics.hitTestLocal(Point(20, 0)))
        assertEquals(null, graphics.hitTestLocal(Point(33, 0)))
    }

    @Test
    fun testGraphicsTextureSize() {
        val bitmap = CpuGraphics().updateShape(redrawNow = true) {
            stroke(Colors["#f0f0f0"], StrokeInfo(thickness = 2.0)) { rect(-75.0, -50.0, 150.0, 100.0) }
        }.bitmap
        assertEquals("153x103", "${bitmap.width}x${bitmap.height}")
    }

    @Test
    fun testUpdatingTheGraphicsBitmapAndRenderingRemovesThePreviousBitmapFromActiveTextures() {
        val g = CpuGraphics()
        assertEquals(0, g.bitmapsToRemove.size)
        g.updateShape {
            stroke(Colors["#f0f0f0"], StrokeInfo(thickness = 2.0)) { rect(-75.0, -50.0, 150.0, 100.0) }
        }
        assertEquals(0, g.bitmapsToRemove.size)
        assertEquals("1x1", "${g.bitmap.width}x${g.bitmap.height}")
        g.redrawIfRequired()
        assertEquals(1, g.bitmapsToRemove.size)
        assertEquals("153x103", "${g.bitmap.width}x${g.bitmap.height}")
        val rc = TestRenderContext()
        g.render(rc)
        assertEquals(0, g.bitmapsToRemove.size)
    }

    @Test
    fun testGraphicsBoundsWithOnlyStrokes() {
        val p0 = Point(109, 135)
        val p1 = Point(25, 190)
        val p2 = Point(210, 250)
        val p3 = Point(234, 49)
        val g = CpuGraphics()
        assertEquals(Rectangle(), g.getLocalBounds())
        g.updateShape {
            clear()
            stroke(Colors.DIMGREY, info = StrokeInfo(thickness = 1.0)) {
                moveTo(p0)
                lineTo(p1)
                lineTo(p2)
                lineTo(p3)
            }
            stroke(Colors.WHITE, info = StrokeInfo(thickness = 2.0)) {
                cubic(p0, p1, p2, p3)
            }
            val ratio = 0.3

            val cubic2 = Bezier(p0, p1, p2, p3).split(ratio).leftCurve
            val cubic3 = Bezier(p0, p1, p2, p3).split(ratio).rightCurve

            stroke(Colors.PURPLE, info = StrokeInfo(thickness = 4.0)) {
                curve(cubic2)
            }
            stroke(Colors.YELLOW, info = StrokeInfo(thickness = 4.0)) {
                curve(cubic3)
            }
        }
        // assertEquals(Rectangle(25, 49, 209, 201), g.getLocalBounds()) // strokes = false
        assertEqualsFloat(Rectangle(24.5, 47.0, 211.5, 203.5), g.getLocalBounds(), 0.1)
    }

    @Test
    fun testScale() {
        val container = Container()
        container.scale = 2.0
        val graphics = CpuGraphics(autoScaling = true).addTo(container)
        graphics.updateShape(redrawNow = true) { fill(Colors.RED) { rect(50, 50, 100, 100) } }
        graphics.anchor(Anchor(0.75, 0.5))
        assertEquals(
            """
                sLeft,sTop=-25.375,-0.25
                fillWidth,fillHeight=100,100
                frameWH=100.5,100.5
                frameOffsetXY=0,0
                anchorDispXY=75.375,50.25
                anchorDispXYNoOffset=75.375,50.25
                bounds=Rectangle(x=-25.375, y=-0.25, width=100, height=100)
                size=201x201
            """.trimIndent(),
            """
                sLeft,sTop=${graphics._sLeft.niceStr},${graphics._sTop.niceStr}
                fillWidth,fillHeight=${graphics.fillWidth.niceStr},${graphics.fillHeight.niceStr}
                frameWH=${graphics.frameWidth.niceStr},${graphics.frameHeight.niceStr}
                frameOffsetXY=${graphics.frameOffsetX.niceStr},${graphics.frameOffsetY.niceStr}
                anchorDispXY=${graphics.anchorDispX.niceStr},${graphics.anchorDispY.niceStr}
                anchorDispXYNoOffset=${graphics.anchorDispXNoOffset.niceStr},${graphics.anchorDispYNoOffset.niceStr}
                bounds=${graphics.getLocalBounds()}
                size=${graphics.bitmap.sizeString}
            """.trimIndent()
        )
    }

    @Test
    fun testStrokePositioning() {
        val container = Container()
        val graphics = container.graphics {
            it.renderer = GraphicsRenderer.SYSTEM
            it.antialiased = true
            this.stroke(Colors.YELLOW, info = StrokeInfo(50.0)) {
                line(Point(100, 100), Point(200, 200))
            }
            this.circle(Point(100, 100), 5f)
            fill(Colors.BLUE)
        }

        val posCircle = container.circle(25.0, fill = Colors.ORANGE) {
            anchor(Anchor.CENTER)
            position(graphics.pos)
        }

        val posCircle2 = container.circle(5.0, fill = Colors.RED) {
            anchor(Anchor.CENTER)
            position(100.0, 100.0)
        }

        val g = (graphics.rendererView as CpuGraphics)
        assertEquals(
            """
                leftTop=75, 75
                fillSize=150, 150
            """.trimIndent(),
            """
                leftTop=${g._sLeft.niceStr}, ${g._sTop.niceStr}
                fillSize=${g.fillWidth.niceStr}, ${g.fillHeight.niceStr}
            """.trimIndent()
        )
    }
}

fun TestRenderContext() = RenderContext(AGLog())
