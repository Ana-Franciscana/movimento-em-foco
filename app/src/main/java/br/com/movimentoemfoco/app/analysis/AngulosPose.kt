package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.Landmarks
import br.com.movimentoemfoco.app.model.ResultadoPose
import kotlin.math.abs
import kotlin.math.atan2

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

    fun tresPontos(resultado: ResultadoPose?, a: Int, b: Int, c: Int): AnguloMedido? {
        if (resultado == null) return null
        val p = resultado.pontos
        if (p.size <= maxOf(a, b, c)) return null
        val pa = p[a]
        val pb = p[b]
        val pc = p[c]
        val confianca = minOf(pa.visibility, pb.visibility, pc.visibility)
        if (confianca.isNaN() || confianca < CONFIANCA_MINIMA) return null
        val w = resultado.larguraImagem.toDouble()
        val h = resultado.alturaImagem.toDouble()
        val graus = Geometria.anguloGraus(
            pa.x * w, pa.y * h,
            pb.x * w, pb.y * h,
            pc.x * w, pc.y * h,
        ) ?: return null
        return AnguloMedido(graus, confianca)
    }

    fun inclinacaoTronco(resultado: ResultadoPose?, lado: Lado): Double? {
        if (resultado == null) return null
        val iQuadril = if (lado == Lado.ESQUERDO) Landmarks.QUADRIL_ESQ else Landmarks.QUADRIL_DIR
        val iOmbro = if (lado == Lado.ESQUERDO) Landmarks.OMBRO_ESQ else Landmarks.OMBRO_DIR
        val p = resultado.pontos
        if (p.size <= maxOf(iQuadril, iOmbro)) return null
        val q = p[iQuadril]
        val o = p[iOmbro]
        val confianca = minOf(q.visibility, o.visibility)
        if (confianca.isNaN() || confianca < CONFIANCA_MINIMA) return null
        val dx = (o.x - q.x) * resultado.larguraImagem.toDouble()
        val dy = (o.y - q.y) * resultado.alturaImagem.toDouble()
        if (dx == 0.0 && dy == 0.0) return null
        val graus = abs(Math.toDegrees(atan2(dy, dx)))
        return if (graus > 90.0) 180.0 - graus else graus
    }

    fun superior(resultado: ResultadoPose?, lado: Lado): Map<String, Double> {
        val esq = lado == Lado.ESQUERDO
        val iOmbro = if (esq) Landmarks.OMBRO_ESQ else Landmarks.OMBRO_DIR
        val iCotovelo = if (esq) Landmarks.COTOVELO_ESQ else Landmarks.COTOVELO_DIR
        val iPunho = if (esq) Landmarks.PUNHO_ESQ else Landmarks.PUNHO_DIR
        val iQuadril = if (esq) Landmarks.QUADRIL_ESQ else Landmarks.QUADRIL_DIR
        val iJoelho = if (esq) Landmarks.JOELHO_ESQ else Landmarks.JOELHO_DIR

        val mapa = LinkedHashMap<String, Double>()
        tresPontos(resultado, iOmbro, iQuadril, iJoelho)?.let { mapa["Quadril"] = it.graus }
        inclinacaoTronco(resultado, lado)?.let { mapa["Inclinação do tronco"] = it }
        tresPontos(resultado, iQuadril, iOmbro, iCotovelo)?.let { mapa["Ombro"] = it.graus }
        tresPontos(resultado, iOmbro, iCotovelo, iPunho)?.let { mapa["Cotovelo"] = it.graus }
        return mapa
    }

}
