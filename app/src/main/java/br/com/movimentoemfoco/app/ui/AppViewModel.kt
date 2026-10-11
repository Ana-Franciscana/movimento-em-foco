package br.com.movimentoemfoco.app.ui

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.model.TipoMidia
import br.com.movimentoemfoco.app.pipeline.AnaliseException
import br.com.movimentoemfoco.app.pipeline.AnalisadorMidia
import br.com.movimentoemfoco.app.pose.ModeloPose
import br.com.movimentoemfoco.app.storage.HistoricoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface Tela {
    data object Inicio : Tela
    data object NovaAnalise : Tela
    data object Processando : Tela
    data class Resultado(val analise: AnaliseResultado) : Tela
    data class Erro(val mensagem: String) : Tela
}

data class Progresso(val mensagem: String, val fracao: Float?)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repositorio = HistoricoRepository(app)
    private val analisador = AnalisadorMidia(app, repositorio)
    private val pastaCapturas = File(app.cacheDir, "capturas")

    var tela: Tela by mutableStateOf(Tela.Inicio)
        private set
    var historico: List<AnaliseResultado> by mutableStateOf(emptyList())
        private set
    var modeloDisponivel: Boolean by mutableStateOf(true)
        private set
    var progresso: Progresso by mutableStateOf(Progresso("Preparando…", null))
        private set

    private var trabalho: Job? = null
    private var gravacaoPendente: File? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            pastaCapturas.listFiles()?.forEach { it.delete() }   // sobras de gravações anteriores
        }
        atualizarHistorico()
    }

    fun atualizarHistorico() {
        viewModelScope.launch {
            val (lista, modelo) = withContext(Dispatchers.IO) {
                repositorio.listar() to ModeloPose.disponivel(getApplication())
            }
            historico = lista
            modeloDisponivel = modelo
        }
    }

    // ----- navegação -----
    fun irParaInicio() { tela = Tela.Inicio; atualizarHistorico() }
    fun irParaNovaAnalise() { tela = Tela.NovaAnalise }
    fun abrir(analise: AnaliseResultado) { tela = Tela.Resultado(analise) }

    fun arquivoDoQuadro(analise: AnaliseResultado, nome: String): File = repositorio.arquivo(analise.id, nome)
    fun miniatura(analise: AnaliseResultado): File = repositorio.arquivo(analise.id, HistoricoRepository.ARQUIVO_MINIATURA)

    // ----- histórico -----
    fun remover(analise: AnaliseResultado) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { repositorio.remover(analise.id) }
            atualizarHistorico()
        }
    }

    // ----- análise -----
    fun analisarImagem(uri: Uri) = executar(TipoMidia.IMAGEM, uri, null)

    fun analisarVideoDaGaleria(uri: Uri) = executar(TipoMidia.VIDEO_GALERIA, uri, null)

    /** Cria o arquivo temporário (cache) onde o app de câmera vai gravar. Null se não foi possível. */
    fun prepararGravacao(): Uri? =
        try {
            pastaCapturas.mkdirs()
            val arquivo = File(pastaCapturas, "gravacao_${System.currentTimeMillis()}.mp4")
            gravacaoPendente = arquivo
            FileProvider.getUriForFile(getApplication(), "${getApplication<Application>().packageName}.fileprovider", arquivo)
        } catch (e: Exception) {
            null
        }

    fun gravacaoConcluida(sucesso: Boolean) {
        val arquivo = gravacaoPendente
        gravacaoPendente = null
        if (!sucesso) {            // usuário cancelou no app de câmera: apenas volta
            arquivo?.delete()
            return
        }
        if (arquivo == null || !arquivo.exists() || arquivo.length() == 0L) {
            arquivo?.delete()
            tela = Tela.Erro("A gravação não foi salva. Tente gravar novamente.")
            return
        }
        executar(TipoMidia.VIDEO_GRAVADO, Uri.fromFile(arquivo), arquivo)
    }

    fun cancelar() {
        trabalho?.cancel()
    }

    private fun executar(tipo: TipoMidia, uri: Uri, temporario: File?) {
        if (trabalho?.isActive == true) return
        progresso = Progresso("Preparando…", null)
        tela = Tela.Processando
        trabalho = viewModelScope.launch {
            try {
                val callback = { msg: String, f: Float? -> progresso = Progresso(msg, f) }
                val resultado = if (tipo == TipoMidia.IMAGEM) analisador.analisarImagem(uri, callback)
                else analisador.analisarVideo(uri, tipo, callback)
                atualizarHistorico()
                tela = Tela.Resultado(resultado)
            } catch (e: CancellationException) {
                tela = Tela.NovaAnalise
                throw e
            } catch (e: AnaliseException) {
                tela = Tela.Erro(e.message ?: "Não foi possível analisar a mídia.")
            } catch (e: OutOfMemoryError) {
                tela = Tela.Erro("O aparelho ficou sem memória ao processar essa mídia. Tente um vídeo mais curto ou de menor resolução.")
            } catch (e: Exception) {
                tela = Tela.Erro("Falha inesperada ao processar: ${e.message ?: e.javaClass.simpleName}")
            } finally {
                temporario?.delete()        // o vídeo gravado não é mantido
            }
        }
    }
}
