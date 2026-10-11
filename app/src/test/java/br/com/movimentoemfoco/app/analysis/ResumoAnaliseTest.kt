package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.NivelQualidade
import br.com.movimentoemfoco.app.model.TipoMidia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumoAnaliseTest {
    @Test fun formataGraus() = assertEquals("95°", ResumoAnalise.graus(95.2))

    @Test fun imagemSemAnguloExplicaOMotivo() {
        val s = ResumoAnalise.gerar(TipoMidia.IMAGEM, 1, 1, 0, NivelQualidade.INSUFICIENTE, null, null, null)
        assertTrue(s.contains("não têm confiança suficiente"))
    }

    @Test fun videoDescreveIntervaloSemInterpretar() {
        val s = ResumoAnalise.gerar(TipoMidia.VIDEO_GALERIA, 12, 11, 10, NivelQualidade.REGULAR, Lado.ESQUERDO, 95.0, 168.0)
        assertTrue(s.contains("95°") && s.contains("168°") && s.contains("esquerdo"))
        assertTrue(s.contains("regular"))
    }
}
