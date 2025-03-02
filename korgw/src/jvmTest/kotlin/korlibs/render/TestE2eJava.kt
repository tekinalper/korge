package korlibs.render

import korlibs.memory.*
import korlibs.graphics.*
import korlibs.event.*
import korlibs.image.bitmap.*
import korlibs.image.color.*
import korlibs.io.util.*
import kotlinx.coroutines.*
import java.awt.*
import kotlin.test.*

class TestE2eJava {
    @Test
    @Ignore
    fun test() {
        // @TODO: java.lang.IllegalStateException: Can't find opengl method glGenBuffers
        if (Platform.isWindows) return
        if (GraphicsEnvironment.isHeadless()) return

        val bmp = Bitmap32(64, 64, premultiplied = true)
        var step = 0
        var exception: Throwable? = null
        runBlocking {
            val WIDTH = 64
            val HEIGHT = 64
            val gameWindow = CreateDefaultGameWindow()
            //val gameWindow = Win32GameWindow()
            //val gameWindow = AwtGameWindow()
            gameWindow.onEvent(MouseEvent.Type.CLICK) {
                //println("MOUSE EVENT $it")
                gameWindow.toggleFullScreen()
            }
            //gameWindow.toggleFullScreen()
            gameWindow.setSize(WIDTH, HEIGHT)
            gameWindow.title = "HELLO WORLD"
            gameWindow.loop {
                val ag = gameWindow.ag
                gameWindow.onRenderEvent {
                    try {
                        ag.clear(ag.mainFrameBuffer, Colors.DARKGREY)
                        val vertices = AGBuffer().upload(floatArrayOf(
                            -1f, -1f,
                            -1f, +1f,
                            +1f, +1f
                        ))
                        ag.draw(
                            //ctx.rctx.currentFrameBuffer,
                            ag.mainFrameBuffer,
                            AGVertexArrayObject(AGVertexData(DefaultShaders.LAYOUT_DEBUG, vertices)),
                            program = DefaultShaders.PROGRAM_DEBUG,
                            drawType = AGDrawType.TRIANGLES,
                            vertexCount = 3,
                        )
                        // @TODO:     java.lang.UnsatisfiedLinkError: Error looking up function 'glDeleteBuffers': The specified procedure could not be found. on windows Github/actions
                        // vertices.close()
                        ag.readColor(ag.mainFrameBuffer, bmp)
                        step++
                    } catch (e: Throwable) {
                        exception = e
                    } finally {
                        gameWindow.close()
                    }
                }
                //println("HELLO")
            }
        }
        if (exception != null) {
            throw exception!!
        }

        assertTrue { step >= 1 }
        //assertEquals(1, step)

        // @TODO: Ignore colors for now. Just ensure that
        // @TODO: Do not check colors for now since seems to fail on linux on Github Actions

        //assertEquals(Colors.RED, bmp[0, 63])
        //assertEquals(Colors.DARKGREY, bmp[63, 0])
    }
}
