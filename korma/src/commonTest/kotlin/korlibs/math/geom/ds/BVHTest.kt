package korlibs.math.geom.ds

import korlibs.math.geom.*
import kotlin.test.*

class BVHTest {
    @Test
    fun test() {
        val tree = BVH2D<String>()
        //for (n in 0 until 1_000_000) tree.insert(Rectangle(n * 5, 5, 10, 10), "$n")
        //for (n in 0 until 10_000_000) tree.insert(Rectangle(n * 5, 5, 10, 10), "$n")
        tree.insertOrUpdate(Rectangle(20, 15, 20, 20), "1")
        tree.insertOrUpdate(Rectangle(50, 50, 20, 20), "2")
        //tree.remove(Rectangle(20, 15, 20, 20), "1")

        val intersection = tree.intersect(Ray(Point(25, 100), Vector2(0, -1)))
        val rectSearch = tree.search(Rectangle(0, 0, 60, 60))

        //assertEquals(1, intersection.size)
        //assertEquals(2, rectSearch.size)

        assertEquals(listOf("1"), intersection.map { it.obj.value }.sortedBy { it })
        assertEqualsFloat(listOf(Float.NaN), intersection.map { it.intersect }.sorted())
        assertEquals(listOf("1", "2"), rectSearch.map { it.value }.sortedBy { it })

        //tree.debug()
    }
}
