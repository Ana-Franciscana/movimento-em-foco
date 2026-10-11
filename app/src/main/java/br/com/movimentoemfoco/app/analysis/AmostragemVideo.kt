package br.com.movimentoemfoco.app.analysis

/**
 * Estratégia de amostragem: divide a duração em N trechos iguais e pega o quadro no CENTRO de cada um.
 * Assim a amostra cobre o vídeo todo com um custo fixo (no máximo [MAX_AMOSTRAS] inferências),
 * independentemente do FPS ou da duração. Não é análise temporal fina: não detecta fases nem repetições.
 */
object AmostragemVideo {
    const val MAX_AMOSTRAS = 12

    /** Instantes em ms, ESTRITAMENTE crescentes (exigência dos timestamps do modo VIDEO do MediaPipe). */
    fun instantesMs(duracaoMs: Long, maxAmostras: Int = MAX_AMOSTRAS): List<Long> {
        if (duracaoMs <= 0L || maxAmostras <= 0) return emptyList()
        val n = minOf(maxAmostras.toLong(), duracaoMs).toInt()
        val lista = ArrayList<Long>(n)
        for (i in 0 until n) {
            val t = ((i + 0.5) * duracaoMs / n).toLong()
            lista += if (lista.isEmpty() || t > lista.last()) t else lista.last() + 1
        }
        return lista
    }
}
