package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Lado

/** Escolhe o lado do corpo com mais ângulos válidos (desempate: maior confiança média). */
object LadoPrincipal {
    fun escolher(esquerdo: List<AnguloMedido?>, direito: List<AnguloMedido?>): Lado? {
        val e = esquerdo.filterNotNull()
        val d = direito.filterNotNull()
        if (e.isEmpty() && d.isEmpty()) return null
        if (e.size != d.size) return if (e.size > d.size) Lado.ESQUERDO else Lado.DIREITO
        val ce = e.map { it.confianca.toDouble() }.average()
        val cd = d.map { it.confianca.toDouble() }.average()
        return if (ce >= cd) Lado.ESQUERDO else Lado.DIREITO
    }
}
