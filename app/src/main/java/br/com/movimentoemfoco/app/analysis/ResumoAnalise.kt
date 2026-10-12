package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.NivelQualidade
import br.com.movimentoemfoco.app.model.TipoMidia
import java.util.Locale

/** Texto descritivo e observável. Não interpreta, não avalia técnica e não faz alegações de saúde. */
object ResumoAnalise {
    private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

    fun graus(valor: Double): String = String.format(PT_BR, "%.0f°", valor)

    fun gerar(
        tipo: TipoMidia,
        analisados: Int,
        comPessoa: Int,
        validos: Int,
        nivel: NivelQualidade,
        lado: Lado?,
        anguloMin: Double?,
        anguloMax: Double?,
        extras: Map<String, Pair<Double, Double>> = emptyMap(),
    ): String {
        val partes = mutableListOf<String>()
        if (tipo == TipoMidia.IMAGEM) {
            when {
                comPessoa == 0 -> partes += "Nenhuma pessoa foi detectada."
                lado == null || anguloMin == null || validos == 0 ->
                    partes += "Pessoa detectada, mas quadril, joelho e tornozelo não têm confiança suficiente para calcular o ângulo do joelho."
                else -> {
                    partes += "Ângulo do joelho ${lado.rotulo}: ${graus(anguloMin)}."
                    for ((nome, faixa) in extras) partes += "$nome: ${graus(faixa.first)}."
                }
            }
        } else {
            partes += "$analisados quadros amostrados ao longo do vídeo; pessoa detectada em $comPessoa e ângulo do joelho válido em $validos."
            if (lado != null && anguloMin != null && anguloMax != null) {
                partes += "Joelho ${lado.rotulo}: ângulo entre ${graus(anguloMin)} e ${graus(anguloMax)} nos quadros amostrados."
                for ((nome, faixa) in extras) {
                    partes += "$nome: entre ${graus(faixa.first)} e ${graus(faixa.second)}."
                }
            }
        }
        partes += "Qualidade da detecção: ${nivel.rotulo.lowercase(PT_BR)}."
        return partes.joinToString(" ")
    }
}
