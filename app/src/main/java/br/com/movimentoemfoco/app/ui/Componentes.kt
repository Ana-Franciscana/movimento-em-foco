package br.com.movimentoemfoco.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import br.com.movimentoemfoco.app.model.NivelQualidade
import br.com.movimentoemfoco.app.ui.theme.AlertaFundo
import br.com.movimentoemfoco.app.ui.theme.AlertaTexto
import br.com.movimentoemfoco.app.ui.theme.ErroFundo
import br.com.movimentoemfoco.app.ui.theme.ErroTexto
import br.com.movimentoemfoco.app.ui.theme.Verde40
import br.com.movimentoemfoco.app.ui.theme.VerdeClaro
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val PT_BR: Locale = Locale.forLanguageTag("pt-BR")

fun formatarData(ms: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", PT_BR).format(Date(ms))

fun formatarSegundos(ms: Long): String = String.format(PT_BR, "%.1f s", ms / 1000.0)

/** Carrega a imagem do arquivo fora da thread principal; mostra aviso se o arquivo não existir ou falhar. */
@Composable
fun ImagemArquivo(
    arquivo: File,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val bitmap: ImageBitmap? by produceState<ImageBitmap?>(null, arquivo) {
        value = withContext(Dispatchers.IO) {
            try {
                if (arquivo.exists()) BitmapFactory.decodeFile(arquivo.path)?.asImageBitmap() else null
            } catch (e: Exception) {
                null
            }
        }
    }
    val atual = bitmap
    if (atual != null) {
        Image(bitmap = atual, contentDescription = "Quadro analisado", modifier = modifier, contentScale = contentScale)
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text(
                if (arquivo.exists()) "Carregando…" else "Imagem indisponível",
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun IndicadorQualidade(nivel: NivelQualidade, detalhe: String, modifier: Modifier = Modifier) {
    val (fundo, texto) = when (nivel) {
        NivelQualidade.BOA -> VerdeClaro to Verde40
        NivelQualidade.REGULAR -> AlertaFundo to AlertaTexto
        NivelQualidade.BAIXA, NivelQualidade.INSUFICIENTE -> ErroFundo to ErroTexto
    }
    Surface(color = fundo, shape = RoundedCornerShape(12.dp), modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text("Qualidade da detecção: ${nivel.rotulo}", color = texto, style = MaterialTheme.typography.titleSmall)
            Text(detalhe, color = texto, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/**
 * Vídeo de referência: toca res/raw/video_referencia.mp4 SE existir. Nenhum vídeo foi fornecido com o
 * projeto, então por padrão o componente mostra que o conteúdo não está disponível.
 */
@Composable
fun VideoReferencia(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val recurso = remember { contexto.resources.getIdentifier("video_referencia", "raw", contexto.packageName) }
    Card(modifier = modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F8FA), contentColor = br.com.movimentoemfoco.app.ui.theme.TextoEscuro)) {
        Column(Modifier.padding(12.dp)) {
            Text("Vídeo de referência", style = MaterialTheme.typography.titleSmall)
            if (recurso == 0) {
                Text(
                    "Conteúdo de referência não disponível nesta versão. Para incluir um, coloque um vídeo " +
                        "com direitos de uso verificados em app/src/main/res/raw/video_referencia.mp4.",
                    style = MaterialTheme.typography.bodySmall,
                )
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = 8.dp),
                    factory = { c ->
                        VideoView(c).apply {
                            setVideoURI(Uri.parse("android.resource://${c.packageName}/$recurso"))
                            setMediaController(MediaController(c))
                            seekTo(1)
                        }
                    },
                )
                Text("Toque no vídeo para reproduzir.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
