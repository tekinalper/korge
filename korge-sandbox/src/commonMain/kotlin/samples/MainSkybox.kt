package samples

import com.soywiz.korev.*
import com.soywiz.korge.input.*
import com.soywiz.korge.scene.*
import com.soywiz.korge.view.*
import com.soywiz.korge3d.*
import com.soywiz.korio.file.std.*
import com.soywiz.korma.geom.*

@OptIn(Korge3DExperimental::class)
class MainSkybox : Scene() {
    override suspend fun SContainer.sceneMain() {
        scene3D {
            val stage3D = this
            skyBox(resourcesVfs["skybox"].readCubeMap("jpg"))

            val angleSpeed = 1.degrees

            onMouseDrag {
                stage3D.camera.yawRight(angleSpeed * it.deltaDx * 0.1, 1f)
                stage3D.camera.pitchDown(angleSpeed * it.deltaDy * 0.1, 1f)
            }

            keys {
                downFrame(Key.UP) { stage3D.camera.pitchDown(angleSpeed * it.speed, 1f) }
                downFrame(Key.DOWN) { stage3D.camera.pitchUp(angleSpeed * it.speed, 1f) }
                downFrame(Key.RIGHT) { stage3D.camera.yawRight(angleSpeed * it.speed, 1f) }
                downFrame(Key.LEFT) { stage3D.camera.yawLeft(angleSpeed * it.speed, 1f) }
            }
        }
    }

    private val KeyEvent.speed: Double get() = if (shift) 5.0 else 1.0
}
