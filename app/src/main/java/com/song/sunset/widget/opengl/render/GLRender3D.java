package com.song.sunset.widget.opengl.render;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.song.sunset.R;
import com.song.sunset.widget.opengl.utils.ShaderHelper;
import com.song.sunset.widget.opengl.utils.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_LINES;
import static android.opengl.GLES20.GL_POINTS;
import static android.opengl.GLES20.GL_TRIANGLE_FAN;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glDrawArrays;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import static android.opengl.GLES20.glVertexAttribPointer;
import static android.opengl.GLES20.glViewport;
import static android.opengl.Matrix.multiplyMM;
import static android.opengl.Matrix.perspectiveM;
import static android.opengl.Matrix.rotateM;
import static android.opengl.Matrix.setIdentityM;
import static android.opengl.Matrix.translateM;

/**
 * @author songmingwen
 * @description
 * @since 2019/4/22
 */
public class GLRender3D implements GLSurfaceView.Renderer {

    private FloatBuffer verticalsBuffer;

    private static final int BYTES_PRE_FLOAT = 4;

    private static final int POINT_SIZE = 4;
    private static final int COLOR_SIZE = 3;
    private static final int STRIDE = (POINT_SIZE + COLOR_SIZE) * BYTES_PRE_FLOAT;

    private static final String A_POSITION = "a_Position";
    private static final String A_COLOR = "a_Color";
    private static final String U_MATRIX = "u_Matrix";

    private final float[] mProjectionMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    private int mPositionIndex;
    private int mColorIndex;

    private Context mContext;
    private int mProgramId;
    private int mMatrixLocation;

    private float[] circleArray = {

            0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            0.5f, -0.8f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, 0.8f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            -0.5f, 0.8f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            -0.5f, -0.8f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, -0.8f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,

            -0.5f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f,
            0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f,

            0.0f, -0.5f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f,
    };

    public GLRender3D(Context context) {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(0, 0, 0, 0);
        verticalsBuffer = ByteBuffer.allocateDirect(circleArray.length * BYTES_PRE_FLOAT)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(circleArray);

        int vertexShader = ShaderHelper.compileVertexShader(TextResourceReader.readTextFileFromResource(mContext, R.raw.vertex_shader_3d));

        int fragmentShader = ShaderHelper.compileFragmentShader(TextResourceReader.readTextFileFromResource(mContext, R.raw.fragment_shader_3d));

        mProgramId = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        mPositionIndex = glGetAttribLocation(mProgramId, A_POSITION);
        mColorIndex = glGetAttribLocation(mProgramId, A_COLOR);
        mMatrixLocation = glGetUniformLocation(mProgramId, U_MATRIX);

        glUseProgram(mProgramId);

        //读取属性 a_Position 的数据
        verticalsBuffer.position(0);
        glVertexAttribPointer(mPositionIndex, POINT_SIZE, GL_FLOAT, false, STRIDE, verticalsBuffer);
        glEnableVertexAttribArray(mPositionIndex);

        verticalsBuffer.position(POINT_SIZE);
        glVertexAttribPointer(mColorIndex, COLOR_SIZE, GL_FLOAT, false, STRIDE, verticalsBuffer);
        glEnableVertexAttribArray(mColorIndex);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
        //创建正交投影矩阵
//        final float aspectRation = width > height ? (float) width / height : (float) height / width;
//        if (width > height) {
//            orthoM(mProjectionMatrix, 0, -aspectRation, aspectRation, -1, 1, -1, 1);
//        } else {
//            orthoM(mProjectionMatrix, 0, -1, 1, -aspectRation, aspectRation, -1, 1);
//        }

        //投影矩阵：视椎体从 z 值为 -1 到 -10
        perspectiveM(mProjectionMatrix, 0, 45, (float) width / height, 1f, 10f);
        //设置模型矩阵
        setIdentityM(mModelMatrix, 0);
        //模型矩阵，移动物体:移动到视椎体之内
        translateM(mModelMatrix, 0, 0, 0, -3f);
        //旋转矩阵
        rotateM(mModelMatrix, 0, -45f, 1f, 0f, 0f);

        //矩阵相乘
        float[] temp = new float[16];
        multiplyMM(temp, 0, mProjectionMatrix, 0, mModelMatrix, 0);
        System.arraycopy(temp, 0, mProjectionMatrix, 0, temp.length);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT);
        //矩阵变换
        glUniformMatrix4fv(mMatrixLocation, 1, false, mProjectionMatrix, 0);
        //绘制矩形
        glDrawArrays(GL_TRIANGLE_FAN, 0, 6);
        glDrawArrays(GL_LINES, 6, 2);
        glDrawArrays(GL_POINTS, 8, 1);
        glDrawArrays(GL_POINTS, 9, 1);
    }
}
