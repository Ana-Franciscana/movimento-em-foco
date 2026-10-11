package br.com.movimentoemfoco.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.ui.theme.AlertaFundo
import br.com.movimentoemfoco.app.ui.theme.AlertaTexto
import br.com.movimentoemfoco.app.ui.theme.AzulClaro
import br.com.movimentoemfoco.app.ui.theme.VerdeClaro
import java.io.File

@Composable
fun HomeScreen(
    historico: List<AnaliseResultado>,
    modeloDisponivel: Boolean,
    miniatura: (AnaliseResultado) -> File,
    onNovaAnalise: () -> Unit,
    onAbrir: (AnaliseResultado) -> Unit,
    onRemover: (AnaliseResultado) -> Unit,
) {
    var paraRemover by remember { mutableStateOf<AnaliseResultado?>(null) }

    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars).padding(16.dp)) {
                    Button(onClick = onNovaAnalise, modifier = Modifier.fillMaxWidth()) {
                        Text("Nova análise")
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text("Movimento em Foco", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "Protótipo educacional de visão computacional: estima a pose do corpo (keypoints) em uma " +
                            "imagem ou vídeo, no próprio aparelho, e mostra o ângulo do joelho.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            if (!modeloDisponivel) {
                item {
                    Surface(color = AlertaFundo, shape = RoundedCornerShape(12.dp)) {
                        Text(
                            "Modelo de pose não encontrado. Coloque pose_landmarker_lite.task em app/src/main/assets/ " +
                                "(veja o README) e reinstale o app.",
                            color = AlertaTexto,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp),
                        )
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = VerdeClaro)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Como funciona", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "1. Toque em “Nova análise” e escolha uma imagem, um vídeo ou grave um vídeo.\n" +
                                "2. O app localiza os pontos do corpo e calcula o ângulo do joelho.\n" +
                                "3. Você vê quadros com o esqueleto desenhado e a qualidade da detecção.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = AzulClaro)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Para um bom resultado", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "• Corpo inteiro no quadro, da cabeça aos pés.\n" +
                                "• Boa iluminação, sem contraluz.\n" +
                                "• De preferência de lado (perfil), com o celular parado.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item { Text("Histórico", style = MaterialTheme.typography.titleMedium) }
            if (historico.isEmpty()) {
                item {
                    Text(
                        "Nenhuma análise ainda. Suas análises aparecerão aqui (ficam só neste aparelho).",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                items(historico, key = { it.id }) { item ->
                    ItemHistorico(item, miniatura(item), onAbrir = { onAbrir(item) }, onRemover = { paraRemover = item })
                }
            }
        }
    }

    paraRemover?.let { alvo ->
        AlertDialog(
            onDismissRequest = { paraRemover = null },
            title = { Text("Remover análise?") },
            text = { Text("O resultado e as imagens salvas desta análise serão apagados deste aparelho.") },
            confirmButton = { TextButton(onClick = { onRemover(alvo); paraRemover = null }) { Text("Remover") } },
            dismissButton = { TextButton(onClick = { paraRemover = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun ItemHistorico(
    analise: AnaliseResultado,
    miniatura: File,
    onAbrir: () -> Unit,
    onRemover: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onAbrir)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            ImagemArquivo(
                arquivo = miniatura,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text("${analise.tipo.rotulo} · ${formatarData(analise.criadaEmMs)}", style = MaterialTheme.typography.titleSmall)
                Text(analise.resumo, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            TextButton(onClick = onRemover) { Text("Remover") }
        }
    }
}
