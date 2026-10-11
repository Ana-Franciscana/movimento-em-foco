package br.com.movimentoemfoco.app.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri

/**
 * Lê quadros de um vídeo com MediaMetadataRetriever (mecanismo nativo do Android; sem dependências extras).
 * Os quadros devolvidos já vêm na orientação de exibição; as dimensões usadas depois são as do próprio bitmap.
 * Chame close() para liberar o decodificador.
 */
class VideoFrameSource private constructor(
    private val retriever: MediaMetadataRetriever,
    val duracaoMs: Long,
) : AutoCloseable {

    /** Quadro mais próximo de [tempoMs], reduzido a no máximo [ladoMaximo] px e em ARGB_8888. Null se falhar. */
    fun quadroEm(tempoMs: Long, ladoMaximo: Int = 1280): Bitmap? {
        val bruto: Bitmap = (try {
            retriever.getFrameAtTime(tempoMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST)
        } catch (e: Exception) {
            null
        }) ?: return null
        val argb = if (bruto.config == Bitmap.Config.ARGB_8888) bruto else {
            bruto.copy(Bitmap.Config.ARGB_8888, false).also { bruto.recycle() }
        }
        return ImageLoader.reduzir(argb, ladoMaximo)
    }

    override fun close() {
        try {
            retriever.release()
        } catch (e: Exception) {
            // nada a fazer: já estamos encerrando
        }
    }

    companion object {
        /** Null se o arquivo não puder ser aberto como vídeo. */
        fun abrir(context: Context, uri: Uri): VideoFrameSource? {
            val retriever = MediaMetadataRetriever()
            return try {
                retriever.setDataSource(context, uri)
                val temVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                if (temVideo != "yes") {
                    retriever.release()
                    return null
                }
                val duracao = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLongOrNull() ?: 0L
                VideoFrameSource(retriever, duracao)
            } catch (e: Exception) {
                try { retriever.release() } catch (e2: Exception) { /* ignorado */ }
                null
            }
        }
    }
}
