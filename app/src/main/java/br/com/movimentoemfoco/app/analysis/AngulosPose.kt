package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.Landmarks
import br.com.movimentoemfoco.app.model.ResultadoPose

/** Ângulo válido e a menor visibilidade entre os 3 pontos usados. */
data class AnguloMedido(val graus: Double, val confianca: Float)

object AngulosPose {

    /** Visibilidade mínima de cada ponto. Parâmetro EXPERIMENTAL, não validado. */
    const val CONFIANCA_MINIMA = 0.5f

    /**
     * Ângulo do joelho (quadril–joelho–tornozelo) no PLANO DA IMAGEM (2D), em graus.
     * Devolve null quando não é possível afirmar um valor válido: sem pose, pontos ausentes,
     * qualquer um dos 3 pontos com visibilidade abaixo de [CONFIANCA_MINIMA] ou geometria degenerada.
     * As coordenadas são convertidas para pixels antes do cálculo, para não distorcer o ângulo.
     */
    fun joelho(resultado: ResultadoPose?, lado: Lado): AnguloMedido? {
        if (resultado == null) return null
        val (iQuadril, iJoelho, iTornozelo) = when (lado) {
            Lado.ESQUERDO -> Triple(Landmarks.QUADRIL_ESQ, Landmarks.JOELHO_ESQ, Landmarks.TORNOZELO_ESQ)
            Lado.DIREITO -> Triple(Landmarks.QUADRIL_DIR, Landmarks.JOELHO_DIR, Landmarks.TORNOZELO_DIR)
        }
        val p = resultado.pontos
        if (p.size <= maxOf(iQuadril, iJoelho, iTornozelo)) return null
        val q = p[iQuadril]
        val j = p[iJoelho]
        val t = p[iTornozelo]
        val confianca = minOf(q.visibility, j.visibility, t.visibility)
        if (confianca.isNaN() || confianca < CONFIANCA_MINIMA) return null
        val w = resultado.larguraImagem.toDouble()
        val h = resultado.alturaImagem.toDouble()
        val graus = Geometria.anguloGraus(
            q.x * w, q.y * h,
            j.x * w, j.y * h,
            t.x * w, t.y * h,
        ) ?: return null
        return AnguloMedido(graus, confianca)
    }
}
