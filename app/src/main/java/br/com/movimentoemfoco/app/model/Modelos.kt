package br.com.movimentoemfoco.app.model

/** Ponto de pose. x e y são NORMALIZADOS (0..1) pela largura/altura da imagem. */
data class Keypoint(
    val x: Float,
    val y: Float,
    val visibility: Float,
    val presence: Float,
)

/** Resultado de pose em UM quadro, com o tamanho dele para converter coordenadas para pixels. */
data class ResultadoPose(
    val pontos: List<Keypoint>,
    val larguraImagem: Int,
    val alturaImagem: Int,
)

enum class Lado(val rotulo: String) { ESQUERDO("esquerdo"), DIREITO("direito") }

/** Índices do MediaPipe Pose (33 landmarks). */
object Landmarks {
    const val QUADRIL_ESQ = 23
    const val QUADRIL_DIR = 24
    const val JOELHO_ESQ = 25
    const val JOELHO_DIR = 26
    const val TORNOZELO_ESQ = 27
    const val TORNOZELO_DIR = 28

    /** Primeiro índice do corpo (0..10 são pontos do rosto, que não desenhamos). */
    const val PRIMEIRO_CORPO = 11

    val CONEXOES: List<Pair<Int, Int>> = listOf(
        11 to 12, 11 to 13, 13 to 15, 12 to 14, 14 to 16,
        11 to 23, 12 to 24, 23 to 24,
        23 to 25, 25 to 27, 24 to 26, 26 to 28,
        27 to 29, 29 to 31, 27 to 31, 28 to 30, 30 to 32, 28 to 32,
    )
}

enum class TipoMidia(val rotulo: String) {
    IMAGEM("Imagem"),
    VIDEO_GALERIA("Vídeo da galeria"),
    VIDEO_GRAVADO("Vídeo gravado"),
}

enum class NivelQualidade(val rotulo: String) {
    BOA("Boa"),
    REGULAR("Regular"),
    BAIXA("Baixa"),
    INSUFICIENTE("Insuficiente"),
}

/** Um quadro apresentado no resultado (o JPEG já contém o esqueleto desenhado). */
data class QuadroResultado(
    val tempoMs: Long,
    val arquivo: String,
    val detectado: Boolean,
    val anguloEsquerdo: Double?,
    val anguloDireito: Double?,
)

data class AnaliseResultado(
    val id: String,
    val criadaEmMs: Long,
    val tipo: TipoMidia,
    val nomeMidia: String,
    val duracaoMs: Long?,
    val quadrosAnalisados: Int,
    val quadrosComPessoa: Int,
    val quadrosValidos: Int,
    val qualidade: NivelQualidade,
    val ladoPrincipal: Lado?,
    val quadros: List<QuadroResultado>,
    val resumo: String,
)
