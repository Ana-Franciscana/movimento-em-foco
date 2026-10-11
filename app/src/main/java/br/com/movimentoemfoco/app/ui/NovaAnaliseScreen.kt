package br.com.movimentoemfoco.app.ui

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.movimentoemfoco.app.ui.theme.AzulClaro

@Composable
fun NovaAnaliseScreen(
    onVoltar: () -> Unit,
    onImagem: (Uri) -> Unit,
    onVideoGaleria: (Uri) -> Unit,
    onPrepararGravacao: () -> Uri?,
    onGravacaoConcluida: (Boolean) -> Unit,
) {
    var aviso by remember { mutableStateOf<String?>(null) }

    val seletorImagem = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onImagem(uri)
    }
    val seletorVideo = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onVideoGaleria(uri)
    }
    val gravador = rememberLauncherForActivityResult(ActivityResultContracts.CaptureVideo()) { sucesso ->
        onGravacaoConcluida(sucesso)
    }

    BackHandler(onBack = onVoltar)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nova análise", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

            Card(colors = CardDefaults.cardColors(containerColor = AzulClaro)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Antes de começar", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "• Corpo inteiro no enquadramento, da cabeça aos pés.\n" +
                            "• Iluminação suficiente, sem contraluz.\n" +
                            "• De preferência em vista lateral (perfil).\n" +
                            "• Celular parado na altura do quadril; vídeos de 5 a 15 segundos funcionam bem.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            Button(
                onClick = {
                    aviso = null
                    seletorImagem.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Escolher imagem da galeria") }

            Button(
                onClick = {
                    aviso = null
                    seletorVideo.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Escolher vídeo da galeria") }

            Button(
                onClick = {
                    aviso = null
                    val destino = onPrepararGravacao()
                    if (destino == null) {
                        aviso = "Não consegui preparar a gravação neste aparelho."
                    } else {
                        try {
                            gravador.launch(destino)
                        } catch (e: ActivityNotFoundException) {
                            aviso = "Nenhum aplicativo de câmera disponível para gravar vídeo neste aparelho."
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Gravar vídeo") }

            aviso?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            Text(
                "A gravação usa o app de câmera do seu aparelho. O vídeo gravado é apagado depois da análise; " +
                    "ficam salvos apenas os quadros com o esqueleto desenhado e o resultado.",
                style = MaterialTheme.typography.bodySmall,
            )

            OutlinedButton(onClick = onVoltar, modifier = Modifier.fillMaxWidth()) { Text("Voltar") }
        }
    }
}
