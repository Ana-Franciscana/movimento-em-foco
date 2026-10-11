package br.com.movimentoemfoco.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ProcessandoScreen(progresso: Progresso, onCancelar: () -> Unit) {
    BackHandler(onBack = onCancelar)
    Scaffold { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val f = progresso.fracao
            if (f == null) CircularProgressIndicator()
            else LinearProgressIndicator(progress = { f.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            Text(progresso.mensagem, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                "A análise acontece no seu aparelho; nada é enviado para a internet.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            OutlinedButton(onClick = onCancelar) { Text("Cancelar") }
        }
    }
}
