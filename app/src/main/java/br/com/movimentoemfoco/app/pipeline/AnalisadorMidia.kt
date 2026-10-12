package br.com.movimentoemfoco.app.pipeline

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import br.com.movimentoemfoco.app.analysis.AmostragemVideo
import br.com.movimentoemfoco.app.analysis.AngulosPose
import br.com.movimentoemfoco.app.analysis.AnguloMedido
import br.com.movimentoemfoco.app.analysis.LadoPrincipal
import br.com.movimentoemfoco.app.analysis.Qualidade
import br.com.movimentoemfoco.app.analysis.QuadroMedido
import br.com.movimentoemfoco.app.analysis.ResumoAnalise
import br.com.movimentoemfoco.app.analysis.SelecaoQuadros
import br.com.movimentoemfoco.app.media.ImageLoader
import br.com.movimentoemfoco.app.media.SkeletonRenderer
import br.com.movimentoemfoco.app.media.VideoFrameSource
import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.QuadroResultado
import br.com.movimentoemfoco.app.model.ResultadoPose
import br.com.movimentoemfoco.app.model.TipoMidia
import br.com.movimentoemfoco.app.pose.ModeloIndisponivelException
import br.com.movimentoemfoco.app.pose.PoseEstimator
import br.com.movimentoemfoco.app.storage.HistoricoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.util.UUID

/** Erro com mensagem já pensada para o usuário. */
class AnaliseException(mensagem: String) : Exception(mensagem)

/**
 * Pipeline: mídia → quadros → MediaPipe Pose Landmarker → keypoints → ângulos → quadros anotados → histórico.
 * Tudo roda em Dispatchers.Default (fora da thread principal) e é cancelável entre quadros.
 * Se algo falhar ou for cancelado, a pasta parcial da análise é removida.
 */
class AnalisadorMidia(
    private val context: Context,
    private val repositorio: HistoricoRepository,
) {

    suspend fun analisarImagem(
        uri: Uri,
        onProgresso: (String, Float?) -> Unit,
    ): AnaliseResultado = withContext(Dispatchers.Default) {
        val id = UUID.randomUUID().toString()
        val pasta = repositorio.criarPasta(id)
        try {
            onProgresso("Carregando a imagem…", null)
            val original = ImageLoader.carregar(context, uri)
                ?: throw AnaliseException("Não consegui abrir a imagem escolhida. Tente outra imagem (JPG ou PNG).")
            ensureActive()

            onProgresso("Estimando a pose no aparelho…", null)
            val pose = criarEstimador(PoseEstimator.Modo.IMAGEM).let { estimador ->
                try {
                    estimador.estimarImagem(original)
                } finally {
                    estimador.close()
                }
            }
            ensureActive()
            if (pose == null) {
                throw AnaliseException(
                    "Nenhuma pessoa foi detectada na imagem. Use uma foto com o corpo inteiro visível, " +
                        "boa iluminação e, de preferência, de lado.",
                )
            }

            val esq = AngulosPose.joelho(pose, Lado.ESQUERDO)
            val dir = AngulosPose.joelho(pose, Lado.DIREITO)
            val lado = LadoPrincipal.escolher(listOf(esq), listOf(dir))
            val principal = if (lado == Lado.ESQUERDO) esq else if (lado == Lado.DIREITO) dir else null
            val validos = if (lado != null) 1 else 0
            val extrasImagem: Map<String, Double> =
                if (lado != null) AngulosPose.superior(pose, lado) else emptyMap<String, Double>()

            onProgresso("Desenhando o esqueleto…", null)
            val anotada = SkeletonRenderer.anotar(original, pose)
            repositorio.salvarJpeg(pasta, "quadro_0.jpg", anotada)
            repositorio.salvarMiniatura(pasta, anotada)

            val nivel = Qualidade.nivel(1, validos)
            val resultado = AnaliseResultado(
                id = id,
                criadaEmMs = System.currentTimeMillis(),
                tipo = TipoMidia.IMAGEM,
                nomeMidia = nomeExibicao(uri, "Imagem da galeria"),
                duracaoMs = null,
                quadrosAnalisados = 1,
                quadrosComPessoa = 1,
                quadrosValidos = validos,
                qualidade = nivel,
                ladoPrincipal = lado,
                quadros = listOf(QuadroResultado(0, "quadro_0.jpg", true, esq?.graus, dir?.graus, extrasImagem)),
                resumo = ResumoAnalise.gerar(
                    TipoMidia.IMAGEM, 1, 1, validos, nivel, lado, principal?.graus, principal?.graus,
                    extrasImagem.mapValues { it.value to it.value },
                ),
            )
            repositorio.salvar(resultado)
            resultado
        } catch (e: Throwable) {
            repositorio.remover(id)
            throw e
        }
    }

    suspend fun analisarVideo(
        uri: Uri,
        tipo: TipoMidia,
        onProgresso: (String, Float?) -> Unit,
    ): AnaliseResultado = withContext(Dispatchers.Default) {
        val id = UUID.randomUUID().toString()
        val pasta = repositorio.criarPasta(id)
        var fonte: VideoFrameSource? = null
        var estimador: PoseEstimator? = null
        try {
            onProgresso("Abrindo o vídeo…", null)
            fonte = VideoFrameSource.abrir(context, uri)
                ?: throw AnaliseException("Não consegui abrir esse vídeo. O arquivo pode estar corrompido ou em formato não suportado.")
            val duracao = fonte.duracaoMs
            val instantes = AmostragemVideo.instantesMs(duracao)
            if (instantes.isEmpty()) {
                throw AnaliseException("Não consegui ler a duração do vídeo; ele parece vazio ou inválido.")
            }

            onProgresso("Carregando o modelo de pose…", null)
            estimador = criarEstimador(PoseEstimator.Modo.VIDEO)

            // 1ª passada: inferência em quadros amostrados (guardamos só as poses, não os bitmaps)
            val poses = ArrayList<ResultadoPose?>(instantes.size)
            for ((i, t) in instantes.withIndex()) {
                ensureActive()
                onProgresso("Analisando quadro ${i + 1} de ${instantes.size}…", i.toFloat() / instantes.size)
                val quadro = fonte.quadroEm(t)
                if (quadro == null) {
                    poses += null
                } else {
                    poses += estimador.estimarVideo(quadro, t)   // timestamps estritamente crescentes
                    quadro.recycle()
                }
            }
            estimador.close(); estimador = null

            val comPessoa = poses.count { it != null }
            if (comPessoa == 0) {
                throw AnaliseException(
                    "Nenhuma pessoa foi detectada no vídeo. Grave o corpo inteiro, com boa iluminação, " +
                        "de preferência de lado e com o celular parado.",
                )
            }

            val esq = poses.map { AngulosPose.joelho(it, Lado.ESQUERDO) }
            val dir = poses.map { AngulosPose.joelho(it, Lado.DIREITO) }
            val lado = LadoPrincipal.escolher(esq, dir)
            val serie: List<AnguloMedido?> = when (lado) {
                Lado.ESQUERDO -> esq
                Lado.DIREITO -> dir
                null -> List(poses.size) { null }
            }
            val validos = serie.count { it != null }
            val angulos = serie.mapNotNull { it?.graus }
            val extrasPorQuadro: List<Map<String, Double>> = poses.map { p ->
                if (lado != null) AngulosPose.superior(p, lado) else emptyMap<String, Double>()
            }
            val faixasExtras: Map<String, Pair<Double, Double>> = extrasPorQuadro
                .flatMap { it.entries }
                .groupBy({ it.key }, { it.value })
                .mapValues { (_, valores) -> valores.min() to valores.max() }

            val medidos = instantes.indices.map { i ->
                QuadroMedido(i, instantes[i], poses[i] != null, serie[i]?.graus)
            }
            val escolhidos = SelecaoQuadros.selecionar(medidos)

            // 2ª passada: reextrair só os quadros escolhidos e desenhar o esqueleto
            val quadros = ArrayList<QuadroResultado>()
            var miniaturaSalva = false
            for ((k, i) in escolhidos.withIndex()) {
                ensureActive()
                onProgresso("Desenhando o esqueleto…", 1f)
                val bitmap = fonte.quadroEm(instantes[i]) ?: continue
                val anotada = SkeletonRenderer.anotar(bitmap, poses[i])
                val nome = "quadro_$k.jpg"
                repositorio.salvarJpeg(pasta, nome, anotada)
                if (!miniaturaSalva) {
                    repositorio.salvarMiniatura(pasta, anotada)
                    miniaturaSalva = true
                }
                quadros += QuadroResultado(instantes[i], nome, poses[i] != null, esq[i]?.graus, dir[i]?.graus, extrasPorQuadro[i])
                bitmap.recycle()
                anotada.recycle()
            }
            if (quadros.isEmpty()) {
                throw AnaliseException("Detectei a pessoa, mas não consegui reler os quadros para exibir o resultado. Tente novamente.")
            }

            val nivel = Qualidade.nivel(instantes.size, validos)
            val resultado = AnaliseResultado(
                id = id,
                criadaEmMs = System.currentTimeMillis(),
                tipo = tipo,
                nomeMidia = if (tipo == TipoMidia.VIDEO_GRAVADO) "Vídeo gravado no app"
                else nomeExibicao(uri, "Vídeo da galeria"),
                duracaoMs = duracao,
                quadrosAnalisados = instantes.size,
                quadrosComPessoa = comPessoa,
                quadrosValidos = validos,
                qualidade = nivel,
                ladoPrincipal = lado,
                quadros = quadros,
                resumo = ResumoAnalise.gerar(
                    tipo, instantes.size, comPessoa, validos, nivel, lado, angulos.minOrNull(), angulos.maxOrNull(),
                    faixasExtras,
                ),
            )
            repositorio.salvar(resultado)
            resultado
        } catch (e: Throwable) {
            repositorio.remover(id)
            throw e
        } finally {
            estimador?.close()
            fonte?.close()
        }
    }

    private fun criarEstimador(modo: PoseEstimator.Modo): PoseEstimator =
        try {
            PoseEstimator(context, modo)
        } catch (e: ModeloIndisponivelException) {
            throw AnaliseException(e.message ?: "Modelo de pose indisponível.")
        }

    private fun nomeExibicao(uri: Uri, padrao: String): String {
        val nome: String? = try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) c.getString(0) else null
            }
        } catch (e: Exception) {
            null
        }
        return nome ?: padrao
    }
}
