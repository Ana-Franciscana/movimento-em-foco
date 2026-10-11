package br.com.movimentoemfoco.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErroScreen(mensagem: String, onTentarNovamente: () -> Unit, onInicio: () -> Unit) {
    BackHandler(onBack = onInicio)
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text("Não foi possível concluir a análise", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
            Text(mensagem, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onTentarNovamente, modifier = Modifier.fillMaxWidth()) { Text("Tentar novamente") }
            OutlinedButton(onClick = onInicio, modifier = Modifier.fillMaxWidth()) { Text("Voltar ao início") }
        }
    }
}
