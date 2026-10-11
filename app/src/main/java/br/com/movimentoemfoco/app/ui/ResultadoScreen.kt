package br.com.movimentoemfoco.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import br.com.movimentoemfoco.app.analysis.ResumoAnalise
import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.model.QuadroResultado
import br.com.movimentoemfoco.app.model.TipoMidia
import br.com.movimentoemfoco.app.ui.theme.AzulClaro
import java.io.File

@Composable
fun ResultadoScreen(
    analise: AnaliseResultado,
    arquivoDoQuadro: (QuadroResultado) -> File,
    onInicio: () -> Unit,
    onNovaAnalise: () -> Unit,
) {
    BackHandler(onBack = onInicio)

    Scaffold { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column {
                    Text("Resultado", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "${analise.tipo.rotulo}: ${analise.nomeMidia}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(formatarData(analise.criadaEmMs), style = MaterialTheme.typography.bodySmall)
                }
            }
            item {
                IndicadorQualidade(
                    analise.qualidade,
                    "${analise.quadrosValidos} de ${analise.quadrosAnalisados} quadro(s) com ângulo do joelho válido; " +
                        "pessoa detectada em ${analise.quadrosComPessoa}.",
                )
            }
            item { Text(analise.resumo, style = MaterialTheme.typography.bodyMedium) }

            if (analise.tipo != TipoMidia.IMAGEM) {
                item {
                    Text(
                        "Quadros de momentos diferentes do vídeo (amostragem uniforme):",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            items(analise.quadros, key = { it.arquivo }) { q ->
                Card {
                    Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ImagemArquivo(
                            arquivo = arquivoDoQuadro(q),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 420.dp),
                        )
                        if (analise.tipo != TipoMidia.IMAGEM) {
                            Text("Instante ${formatarSegundos(q.tempoMs)}", style = MaterialTheme.typography.labelMedium)
                        }
                        Text(descricaoAngulos(q), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            item {
                Card(colors = CardDefaults.cardColors(containerColor = AzulClaro)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Como ler este resultado", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Os pontos verdes/azuis são os keypoints estimados pelo modelo; só aparecem os que têm " +
                                "confiança suficiente. O ângulo do joelho é calculado a partir de quadril, joelho e " +
                                "tornozelo no plano 2D da imagem, então depende do ângulo da câmera, de partes " +
                                "escondidas e do enquadramento. Quando um ponto não é confiável, nenhum ângulo é " +
                                "mostrado. São valores observados nos quadros, não uma avaliação de técnica.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                            "Protótipo educacional: não é ferramenta médica, não faz diagnóstico nem garante " +
                                "execução correta ou prevenção de lesões.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
            item { VideoReferencia() }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onInicio, modifier = Modifier.weight(1f)) { Text("Voltar ao início") }
                    Button(onClick = onNovaAnalise, modifier = Modifier.weight(1f)) { Text("Nova análise") }
                }
            }
        }
    }
}

private fun descricaoAngulos(q: QuadroResultado): String {
    if (!q.detectado) return "Pessoa não detectada neste quadro."
    val esq = q.anguloEsquerdo?.let { ResumoAnalise.graus(it) } ?: "sem dados confiáveis"
    val dir = q.anguloDireito?.let { ResumoAnalise.graus(it) } ?: "sem dados confiáveis"
    return "Joelho esquerdo: $esq · Joelho direito: $dir"
}
