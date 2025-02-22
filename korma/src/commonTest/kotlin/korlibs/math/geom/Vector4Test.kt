package korlibs.math.geom

import kotlin.test.*

class Vector4Test {
    @Test
    fun testAdd() = assertEquals(Vector4(11f, 22f, 33f, 44f), Vector4(1f, 2f, 3f, 4f) + Vector4(10f, 20f, 30f, 40f))
    @Test
    fun testTimes() = assertEquals(Vector4(-1f, -4f, -9f, -16f), Vector4(1f, 2f, 3f, 4f) * Vector4(-1f, -2f, -3f, -4f))
    @Test
    fun testToString() = assertEquals("Vector4(1, 2, 3, 4)", Vector4(1f, 2f, 3f, 4f).toString())
    @Test
    fun testNormalized() = assertEquals(1f, Vector4(1f, 2f, 4f, 8f).normalized().length, 0.00001f)
    @Test
    fun testEquals() {
        assertEquals(true, Vector4(1f, 2f, 3f, 4f) == Vector4(1f, 2f, 3f, 4f))
        assertEquals(false, Vector4(1f, 2f, 3f, 4f) == Vector4(1f, 2f, 3f, -4f))
    }
}
