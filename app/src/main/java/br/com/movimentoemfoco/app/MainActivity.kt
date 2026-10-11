package br.com.movimentoemfoco.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.movimentoemfoco.app.ui.AppViewModel
import br.com.movimentoemfoco.app.ui.ErroScreen
import br.com.movimentoemfoco.app.ui.HomeScreen
import br.com.movimentoemfoco.app.ui.NovaAnaliseScreen
import br.com.movimentoemfoco.app.ui.ProcessandoScreen
import br.com.movimentoemfoco.app.ui.ResultadoScreen
import br.com.movimentoemfoco.app.ui.Tela
import br.com.movimentoemfoco.app.ui.theme.MovimentoEmFocoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovimentoEmFocoTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot(vm: AppViewModel = viewModel()) {
    when (val tela = vm.tela) {
        Tela.Inicio -> HomeScreen(
            historico = vm.historico,
            modeloDisponivel = vm.modeloDisponivel,
            miniatura = vm::miniatura,
            onNovaAnalise = vm::irParaNovaAnalise,
            onAbrir = vm::abrir,
            onRemover = vm::remover,
        )

        Tela.NovaAnalise -> NovaAnaliseScreen(
            onVoltar = vm::irParaInicio,
            onImagem = vm::analisarImagem,
            onVideoGaleria = vm::analisarVideoDaGaleria,
            onPrepararGravacao = vm::prepararGravacao,
            onGravacaoConcluida = vm::gravacaoConcluida,
        )

        Tela.Processando -> ProcessandoScreen(progresso = vm.progresso, onCancelar = vm::cancelar)

        is Tela.Resultado -> ResultadoScreen(
            analise = tela.analise,
            arquivoDoQuadro = { q -> vm.arquivoDoQuadro(tela.analise, q.arquivo) },
            onInicio = vm::irParaInicio,
            onNovaAnalise = vm::irParaNovaAnalise,
        )

        is Tela.Erro -> ErroScreen(
            mensagem = tela.mensagem,
            onTentarNovamente = vm::irParaNovaAnalise,
            onInicio = vm::irParaInicio,
        )
    }
}
