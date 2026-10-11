package br.com.movimentoemfoco.app.storage

import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.NivelQualidade
import br.com.movimentoemfoco.app.model.QuadroResultado
import br.com.movimentoemfoco.app.model.TipoMidia
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AnaliseJsonTest {

    private fun exemplo(id: String = "abc-123", criada: Long = 1_000L) = AnaliseResultado(
        id = id, criadaEmMs = criada, tipo = TipoMidia.VIDEO_GRAVADO, nomeMidia = "Vídeo gravado no app",
        duracaoMs = 8_000, quadrosAnalisados = 12, quadrosComPessoa = 11, quadrosValidos = 9,
        qualidade = NivelQualidade.REGULAR, ladoPrincipal = Lado.DIREITO, resumo = "Resumo de teste com acentuação.",
        quadros = listOf(
            QuadroResultado(500, "quadro_0.jpg", true, 101.5, null),
            QuadroResultado(4_000, "quadro_1.jpg", false, null, null),
        ),
    )

    @Test fun idaEVoltaPreservaTudoIncluindoNulos() {
        val original = exemplo()
        val lido = AnaliseJson.deJson(JSONObject(AnaliseJson.paraJson(original).toString()))
        assertEquals(original, lido)
    }

    @Test fun camposOpcionaisAusentesViramNulo() {
        val img = exemplo().copy(tipo = TipoMidia.IMAGEM, duracaoMs = null, ladoPrincipal = null)
        val lido = AnaliseJson.deJson(JSONObject(AnaliseJson.paraJson(img).toString()))
        assertNull(lido.duracaoMs)
        assertNull(lido.ladoPrincipal)
    }

    @Test(expected = Exception::class) fun jsonIncompletoLancaExcecao() {
        AnaliseJson.deJson(JSONObject("""{"id":"x"}"""))
    }
}
