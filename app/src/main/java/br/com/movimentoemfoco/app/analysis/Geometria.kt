package br.com.movimentoemfoco.app.analysis

import kotlin.math.acos
import kotlin.math.hypot

/** Geometria 2D pura (sem Android). */
object Geometria {

    /**
     * Ângulo em GRAUS (0..180) no vértice B, entre os segmentos B→A e B→C.
     * Retorna null se algum segmento tiver comprimento zero ou se houver valor não numérico.
     */
    fun anguloGraus(
        ax: Double, ay: Double,
        bx: Double, by: Double,
        cx: Double, cy: Double,
    ): Double? {
        if (listOf(ax, ay, bx, by, cx, cy).any { it.isNaN() || it.isInfinite() }) return null
        val v1x = ax - bx
        val v1y = ay - by
        val v2x = cx - bx
        val v2y = cy - by
        val n1 = hypot(v1x, v1y)
        val n2 = hypot(v2x, v2y)
        if (n1 == 0.0 || n2 == 0.0) return null
        val cos = ((v1x * v2x + v1y * v2y) / (n1 * n2)).coerceIn(-1.0, 1.0)
        return Math.toDegrees(acos(cos))
    }
}
