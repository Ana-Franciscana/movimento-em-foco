package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.NivelQualidade
import org.junit.Assert.assertEquals
import org.junit.Test

class QualidadeTest {
    @Test fun semQuadros() = assertEquals(NivelQualidade.INSUFICIENTE, Qualidade.nivel(0, 0))
    @Test fun nenhumValido() = assertEquals(NivelQualidade.INSUFICIENTE, Qualidade.nivel(12, 0))
    @Test fun boa() = assertEquals(NivelQualidade.BOA, Qualidade.nivel(10, 9))
    @Test fun regular() = assertEquals(NivelQualidade.REGULAR, Qualidade.nivel(10, 7))
    @Test fun baixa() = assertEquals(NivelQualidade.BAIXA, Qualidade.nivel(10, 3))
    @Test fun imagemUnicaValida() = assertEquals(NivelQualidade.BOA, Qualidade.nivel(1, 1))
}
