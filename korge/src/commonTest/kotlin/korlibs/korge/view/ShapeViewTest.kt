package korlibs.korge.view

import korlibs.math.geom.*
import korlibs.math.geom.vector.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ShapeViewTest {
    @Test
    fun testShapeViewBounds() {
        for (renderer in GraphicsRenderer.values()) {
            val view = ShapeView(strokeThickness = 1.0)
            view.renderer = renderer
            view.updatePath {
                clear()
                line(Point(256, 256), Point(400, 256))
            }
            view.boundsIncludeStrokes = false
            assertEquals(Rectangle(Point(256, 256), Size(144, 0)), view.getLocalBounds(), message = "bounds without strokes $renderer")
            view.boundsIncludeStrokes = true
            assertEquals(Rectangle(Point(255.5, 255.5), Size(145, 1)), view.getLocalBounds(), message = "bounds with strokes $renderer")
        }
    }
}
