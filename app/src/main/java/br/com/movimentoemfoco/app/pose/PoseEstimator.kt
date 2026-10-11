package br.com.movimentoemfoco.app.pose

import android.content.Context
import android.graphics.Bitmap
import br.com.movimentoemfoco.app.model.Keypoint
import br.com.movimentoemfoco.app.model.ResultadoPose
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

class ModeloIndisponivelException(mensagem: String, causa: Throwable? = null) : Exception(mensagem, causa)

object ModeloPose {
    const val ARQUIVO = "pose_landmarker_lite.task"

    fun disponivel(context: Context): Boolean =
        try {
            context.assets.list("")?.contains(ARQUIVO) == true
        } catch (e: Exception) {
            false
        }
}

/**
 * Envolve o MediaPipe Pose Landmarker. Um estimador por análise; chame close() ao terminar.
 * - Modo IMAGEM: estimarImagem(bitmap)
 * - Modo VIDEO: estimarVideo(bitmap, timestampMs) com timestamps ESTRITAMENTE crescentes
 * As chamadas bloqueiam a thread que as executa: use sempre fora da thread da interface.
 */
class PoseEstimator(context: Context, private val modo: Modo) : AutoCloseable {

    enum class Modo { IMAGEM, VIDEO }

    private val landmarker: PoseLandmarker

    init {
        landmarker = try {
            val base = BaseOptions.builder().setModelAssetPath(ModeloPose.ARQUIVO).build()
            val opcoes = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(base)
                .setRunningMode(if (modo == Modo.IMAGEM) RunningMode.IMAGE else RunningMode.VIDEO)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()
            PoseLandmarker.createFromOptions(context.applicationContext, opcoes)
        } catch (e: Throwable) {
            throw ModeloIndisponivelException(
                "Não foi possível carregar o modelo de pose. Confira se '${ModeloPose.ARQUIVO}' está em " +
                    "app/src/main/assets/ (veja o README) e reinstale o app.",
                e,
            )
        }
    }

    /** Devolve null se nenhuma pessoa foi detectada. */
    fun estimarImagem(bitmap: Bitmap): ResultadoPose? {
        check(modo == Modo.IMAGEM) { "Estimador criado para vídeo" }
        val argb = comoArgb(bitmap)
        return converter(landmarker.detect(BitmapImageBuilder(argb).build()), argb)
    }

    /** Devolve null se nenhuma pessoa foi detectada. [timestampMs] deve crescer a cada chamada. */
    fun estimarVideo(bitmap: Bitmap, timestampMs: Long): ResultadoPose? {
        check(modo == Modo.VIDEO) { "Estimador criado para imagem" }
        val argb = comoArgb(bitmap)
        return converter(landmarker.detectForVideo(BitmapImageBuilder(argb).build(), timestampMs), argb)
    }

    private fun comoArgb(bitmap: Bitmap): Bitmap =
        if (bitmap.config == Bitmap.Config.ARGB_8888) bitmap else bitmap.copy(Bitmap.Config.ARGB_8888, false)

    private fun converter(resultado: PoseLandmarkerResult, bitmap: Bitmap): ResultadoPose? {
        val pose = resultado.landmarks().firstOrNull() ?: return null
        val pontos = pose.map {
            Keypoint(
                x = it.x(),
                y = it.y(),
                visibility = it.visibility().orElse(0f),
                presence = it.presence().orElse(0f),
            )
        }
        return ResultadoPose(pontos, bitmap.width, bitmap.height)
    }

    override fun close() = landmarker.close()
}
