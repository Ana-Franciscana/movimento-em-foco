package br.com.movimentoemfoco.app.storage

import android.content.Context
import android.graphics.Bitmap
import br.com.movimentoemfoco.app.media.ImageLoader
import br.com.movimentoemfoco.app.model.AnaliseResultado
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream

/**
 * Histórico local: uma pasta por análise em filesDir/analises/<id>/ com resultado.json,
 * quadros anotados (JPEG) e miniatura. Nenhum vídeo é guardado.
 */
class HistoricoRepository(private val raiz: File) {

    constructor(context: Context) : this(File(context.filesDir, "analises"))

    private fun idValido(id: String) = id.isNotEmpty() && id.all { it.isLetterOrDigit() || it == '-' }

    fun pasta(id: String): File {
        require(idValido(id)) { "id inválido" }
        return File(raiz, id)
    }

    fun criarPasta(id: String): File = pasta(id).also { it.mkdirs() }

    fun arquivo(id: String, nome: String): File = File(pasta(id), File(nome).name)

    fun salvarJpeg(pasta: File, nome: String, bitmap: Bitmap, qualidade: Int = 85) {
        FileOutputStream(File(pasta, nome)).use { bitmap.compress(Bitmap.CompressFormat.JPEG, qualidade, it) }
    }

    fun salvarMiniatura(pasta: File, bitmap: Bitmap) {
        val menor = ImageLoader.reduzir(bitmap.copy(Bitmap.Config.ARGB_8888, false), MINIATURA_PX)
        salvarJpeg(pasta, ARQUIVO_MINIATURA, menor, 80)
        menor.recycle()
    }

    /** Escrita em arquivo temporário seguida de rename, para não deixar JSON pela metade. */
    fun salvar(resultado: AnaliseResultado) {
        val pasta = criarPasta(resultado.id)
        val tmp = File(pasta, "resultado.json.tmp")
        tmp.writeText(AnaliseJson.paraJson(resultado).toString(), Charsets.UTF_8)
        val destino = File(pasta, ARQUIVO_JSON)
        if (!tmp.renameTo(destino)) {
            destino.delete()
            check(tmp.renameTo(destino)) { "Não consegui gravar o resultado" }
        }
    }

    fun carregar(id: String): AnaliseResultado? =
        try {
            val f = File(pasta(id), ARQUIVO_JSON)
            if (f.exists()) AnaliseJson.deJson(JSONObject(f.readText(Charsets.UTF_8))) else null
        } catch (e: Exception) {
            null
        }

    /** Mais recentes primeiro. Itens corrompidos ou sem JSON são ignorados. */
    fun listar(): List<AnaliseResultado> {
        val pastas = raiz.listFiles { f -> f.isDirectory } ?: return emptyList()
        return pastas.mapNotNull { carregar(it.name) }.sortedByDescending { it.criadaEmMs }
    }

    fun remover(id: String): Boolean =
        try {
            val p = pasta(id)
            !p.exists() || p.deleteRecursively()
        } catch (e: Exception) {
            false
        }

    companion object {
        const val ARQUIVO_JSON = "resultado.json"
        const val ARQUIVO_MINIATURA = "miniatura.jpg"
        private const val MINIATURA_PX = 256
    }
}
