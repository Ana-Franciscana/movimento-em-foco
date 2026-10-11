package br.com.movimentoemfoco.app.analysis

data class QuadroMedido(
    val indice: Int,
    val tempoMs: Long,
    val detectado: Boolean,
    val angulo: Double?,
)

/**
 * Escolhe quadros representativos: o de menor ângulo (joelho mais flexionado), o de maior ângulo,
 * o primeiro e o último candidato. Se nenhum quadro tem ângulo válido, usa os quadros com pessoa
 * detectada (para ao menos mostrar os keypoints). Devolve os ÍNDICES em ordem de tempo.
 */
object SelecaoQuadros {
    const val MAXIMO_PADRAO = 4

    fun selecionar(quadros: List<QuadroMedido>, maximo: Int = MAXIMO_PADRAO): List<Int> {
        if (maximo <= 0) return emptyList()
        val comAngulo = quadros.filter { it.angulo != null }
        val candidatos = comAngulo.ifEmpty { quadros.filter { it.detectado } }
        if (candidatos.isEmpty()) return emptyList()

        val escolhidos = LinkedHashSet<Int>()
        if (comAngulo.isNotEmpty()) {
            escolhidos += comAngulo.minBy { it.angulo!! }.indice
            escolhidos += comAngulo.maxBy { it.angulo!! }.indice
        }
        escolhidos += candidatos.first().indice
        escolhidos += candidatos.last().indice

        if (maximo > 4) {
            for (p in 0 until maximo) {
                if (escolhidos.size >= maximo) break
                escolhidos += candidatos[p * (candidatos.size - 1) / (maximo - 1)].indice
            }
        }

        val porIndice = quadros.associateBy { it.indice }
        return escolhidos.take(maximo)
            .sortedBy { porIndice.getValue(it).tempoMs }
    }
}
