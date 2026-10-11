package br.com.movimentoemfoco.app.media

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import br.com.movimentoemfoco.app.analysis.AngulosPose
import br.com.movimentoemfoco.app.model.Landmarks
import br.com.movimentoemfoco.app.model.ResultadoPose
import kotlin.math.max

/**
 * Desenha o esqueleto SOBRE uma cópia do quadro (o original não é alterado).
 * Pontos e segmentos com visibilidade abaixo de [AngulosPose.CONFIANCA_MINIMA] não são desenhados.
 * O resultado é salvo como JPEG, então a imagem do histórico já mostra os keypoints.
 */
object SkeletonRenderer {

    fun anotar(origem: Bitmap, pose: ResultadoPose?): Bitmap {
        val saida = origem.copy(Bitmap.Config.ARGB_8888, true)
        if (pose == null) return saida

        val canvas = Canvas(saida)
        val espessura = max(4f, saida.width / 180f)
        val linha = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00C853")
            strokeWidth = espessura
            strokeCap = Paint.Cap.ROUND
        }
        val miolo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1565C0") }
        val borda = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }

        val pontos = pose.pontos
        fun visivel(i: Int) = i < pontos.size && pontos[i].visibility >= AngulosPose.CONFIANCA_MINIMA
        fun px(i: Int) = pontos[i].x * saida.width
        fun py(i: Int) = pontos[i].y * saida.height

        for ((a, b) in Landmarks.CONEXOES) {
            if (visivel(a) && visivel(b)) canvas.drawLine(px(a), py(a), px(b), py(b), linha)
        }
        for (i in Landmarks.PRIMEIRO_CORPO until pontos.size) {
            if (visivel(i)) {
                canvas.drawCircle(px(i), py(i), espessura * 1.9f, borda)
                canvas.drawCircle(px(i), py(i), espessura * 1.4f, miolo)
            }
        }
        return saida
    }
}
