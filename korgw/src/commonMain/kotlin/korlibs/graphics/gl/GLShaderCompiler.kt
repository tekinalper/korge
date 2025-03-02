package korlibs.graphics.gl

import korlibs.datastructure.*
import korlibs.kgl.KmlGl
import korlibs.kgl.getProgramiv
import korlibs.kgl.getShaderInfoLog
import korlibs.kgl.getShaderiv
import korlibs.logger.Logger
import korlibs.graphics.shader.*
import korlibs.graphics.shader.gl.GlslConfig
import korlibs.graphics.shader.gl.GlslGenerator
import korlibs.graphics.shader.gl.toNewGlslString
import kotlin.native.concurrent.*

internal data class GLProgramInfo(var programId: Int, var vertexId: Int, var fragmentId: Int, val blocks: List<UniformBlock>) {
    private val blocksByFixedLocation = blocks.associateBy { it.fixedLocation }
    private val maxBlockId = (blocks.maxOfOrNull { it.fixedLocation } ?: -1) + 1
    val uniforms: Array<UniformsRef?> = Array(maxBlockId + 1) { blocksByFixedLocation[it]?.let { UniformsRef(it) } }
    //@Deprecated("This is the only place where AGUniformValues are still used")
    //val cache = AGUniformValues()

    val cachedAttribLocations = FastStringMap<Int>()
    val cachedUniformLocations = FastStringMap<Int>()

    fun getAttribLocation(gl: KmlGl, name: String): Int =
        cachedAttribLocations.getOrPut(name) { gl.getAttribLocation(programId, name) }

    fun getUniformLocation(gl: KmlGl, name: String): Int =
        cachedUniformLocations.getOrPut(name) { gl.getUniformLocation(programId, name) }

    fun use(gl: KmlGl) {
        gl.useProgram(programId)
    }

    fun delete(gl: KmlGl) {
        if (vertexId != 0) gl.deleteShader(vertexId)
        if (fragmentId != 0) gl.deleteShader(fragmentId)
        if (programId != 0) gl.deleteProgram(programId)
        vertexId = 0
        fragmentId = 0
        programId = 0
    }
}

internal object GLShaderCompiler {
    private val logger = Logger("GLShaderCompiler")

    private fun String.replaceVersion(version: Int) = this.replace("#version 100", "#version $version")

    // @TODO: Prevent leaks if we throw exceptions, we should free resources
    fun programCreate(gl: KmlGl, config: GlslConfig, program: Program, glSlVersion: Int? = null, debugName: String?): GLProgramInfo {
        val id = gl.createProgram()

        //println("GL_SHADING_LANGUAGE_VERSION: $glslVersionInt : $glslVersionString")

        val guessedGlSlVersion = glSlVersion ?: gl.versionInt
        val usedGlSlVersion = GlslGenerator.FORCE_GLSL_VERSION?.toIntOrNull()
            ?: when (guessedGlSlVersion) {
                460 -> 460
                in 300..450 -> 100
                else -> guessedGlSlVersion
            }

        if (GlslGenerator.DEBUG_GLSL) {
            logger.trace { "GLSL version: requested=$glSlVersion, guessed=$guessedGlSlVersion, forced=${GlslGenerator.FORCE_GLSL_VERSION}. used=$usedGlSlVersion" }
        }

        val fragmentShaderId = createShaderCompat(gl, gl.FRAGMENT_SHADER, debugName) { compatibility ->
            program.fragment.toNewGlslString(config.copy(version = usedGlSlVersion, compatibility = compatibility))
        }
        val vertexShaderId = createShaderCompat(gl, gl.VERTEX_SHADER, debugName) { compatibility ->
            program.vertex.toNewGlslString(config.copy(version = usedGlSlVersion, compatibility = compatibility))
        }
        for (attr in program.attributes) {
            val location = attr.fixedLocation
            gl.bindAttribLocation(id, location, attr.name)
        }
        gl.attachShader(id, fragmentShaderId)
        gl.attachShader(id, vertexShaderId)
        gl.linkProgram(id)
        val linkStatus = gl.getProgramiv(id, gl.LINK_STATUS)
        return GLProgramInfo(id, vertexShaderId, fragmentShaderId, program.uniformBlocks)
    }

    private fun createShaderCompat(gl: KmlGl, config: GlslConfig, type: Int, shader: Shader, debugName: String?): Int {
        return createShaderCompat(gl, type, debugName) { compatibility ->
            shader.toNewGlslString(config.copy(compatibility = compatibility))
        }
    }

    private inline fun createShaderCompat(gl: KmlGl, type: Int, debugName: String?, gen: (compat: Boolean) -> String): Int {
        return try {
            createShader(gl, type, gen(true), debugName)
        } catch (e: AGOpengl.ShaderException) {
            createShader(gl, type, gen(false), debugName)
        }
    }

    private fun createShader(gl: KmlGl, type: Int, str: String, debugName: String?): Int {
        val shaderId = gl.createShader(type)

        gl.shaderSourceWithExt(shaderId, str)
        gl.compileShader(shaderId)

        val out = gl.getShaderiv(shaderId, gl.COMPILE_STATUS)
        val errorInt = gl.getError()
        if (out != gl.GTRUE) {
            val error = gl.getShaderInfoLog(shaderId)
            throw AGOpengl.ShaderException(str, error, errorInt, gl, debugName, type, out)
        }
        return shaderId
    }
}

@SharedImmutable
val KmlGl.versionString by Extra.PropertyThis<KmlGl, String> {
    getString(SHADING_LANGUAGE_VERSION)
}

@SharedImmutable
val KmlGl.versionInt by Extra.PropertyThis<KmlGl, Int> {
    versionString.replace(".", "").trim().toIntOrNull() ?: 100
}
