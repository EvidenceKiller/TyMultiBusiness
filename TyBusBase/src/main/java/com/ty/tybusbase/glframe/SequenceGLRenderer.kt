package com.ty.tybusbase.glframe

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class SequenceGLRenderer() : GLSurfaceView.Renderer {

    private var textureId = 0
    private var program = 0
    private var aPosition = 0
    private var aTexCoord = 0
    private var uMVPMatrix = 0
    private var uTexMatrix = 0

    private val mvpMatrix = FloatArray(16)
    private val texMatrix = FloatArray(16)

    // 顶点坐标 (x, y)
    private val vertexBuffer: FloatBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
        put(floatArrayOf(-1f, -1f, 1f, -1f, -1f, 1f, 1f, 1f))
        position(0)
    }
    // 纹理坐标
    private val texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(4 * 2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().apply {
        put(floatArrayOf(0f, 1f, 1f, 1f, 0f, 0f, 1f, 0f))
        position(0)
    }

    var currentFrame = 0
    private var lastFrameTime = 0L

    // 帧数据回调（由外部提供解码后的 Bitmap）
    private var pendingBitmap: Bitmap? = null
    private val frameLock = Any()

    fun updateFrame(bitmap: Bitmap) {
        synchronized(frameLock) {
            pendingBitmap = bitmap
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        // 创建纹理
        val texIds = IntArray(1)
        GLES20.glGenTextures(1, texIds, 0)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        textureId = texIds[0]
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

        // 编译着色器
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        aPosition = GLES20.glGetAttribLocation(program, "aPosition")
        aTexCoord = GLES20.glGetAttribLocation(program, "aTexCoord")
        uMVPMatrix = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        uTexMatrix = GLES20.glGetUniformLocation(program, "uTexMatrix")

        // 初始化矩阵
        Matrix.setIdentityM(mvpMatrix, 0)
        Matrix.setIdentityM(texMatrix, 0)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        // 根据宽高比调整 MVP（如果需要保持图片比例，这里可以添加变换）
        // 此处不做额外处理，直接铺满
    }

    override fun onDrawFrame(gl: GL10?) {
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        // 处理新收到的 Bitmap
        var bitmapToUpload: Bitmap? = null
        synchronized(frameLock) {
            if (pendingBitmap != null) {
                bitmapToUpload = pendingBitmap
                pendingBitmap = null
            }
        }
        bitmapToUpload?.let {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            // 将 Bitmap 数据上传到纹理
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, it, 0)
            // 注意：这里不回收 Bitmap，因为外部解码器会复用池
        }

        // 渲染
        GLES20.glUseProgram(program)

        // 绑定顶点缓冲区
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(aPosition, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glEnableVertexAttribArray(aPosition)

        texCoordBuffer.position(0)
        GLES20.glVertexAttribPointer(aTexCoord, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        GLES20.glEnableVertexAttribArray(aTexCoord)

        GLES20.glUniformMatrix4fv(uMVPMatrix, 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(uTexMatrix, 1, false, texMatrix, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glDisableVertexAttribArray(aPosition)
        GLES20.glDisableVertexAttribArray(aTexCoord)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)
        return shader
    }

    companion object {
        private const val VERTEX_SHADER_CODE = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            uniform mat4 uMVPMatrix;
            uniform mat4 uTexMatrix;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * aPosition;
                vTexCoord = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """
    }
}