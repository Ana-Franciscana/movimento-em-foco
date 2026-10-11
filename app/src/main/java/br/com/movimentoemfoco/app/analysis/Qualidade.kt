package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.NivelQualidade

/**
 * Indicador simples: fração de quadros analisados com ângulo do joelho válido.
 * Os limiares são EXPERIMENTAIS (0,90 vem da meta do exercício de qualidade do curso) e não foram validados.
 */
object Qualidade {
    const val LIMIAR_BOA = 0.90
    const val LIMIAR_REGULAR = 0.60

    fun nivel(analisados: Int, validos: Int): NivelQualidade {
        if (analisados <= 0 || validos <= 0) return NivelQualidade.INSUFICIENTE
        val fracao = validos.toDouble() / analisados
        return when {
            fracao >= LIMIAR_BOA -> NivelQualidade.BOA
            fracao >= LIMIAR_REGULAR -> NivelQualidade.REGULAR
            else -> NivelQualidade.BAIXA
        }
    }
}
