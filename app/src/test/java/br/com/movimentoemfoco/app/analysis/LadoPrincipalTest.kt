package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Lado
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LadoPrincipalTest {
    private fun a(c: Float) = AnguloMedido(120.0, c)

    @Test fun semNenhumAnguloRetornaNulo() = assertNull(LadoPrincipal.escolher(listOf(null, null), listOf(null)))

    @Test fun vaiParaOLadoComMaisAngulosValidos() =
        assertEquals(Lado.DIREITO, LadoPrincipal.escolher(listOf(a(0.9f), null, null), listOf(a(0.6f), a(0.6f), null)))

    @Test fun empateDecidePelaConfiancaMedia() {
        assertEquals(Lado.ESQUERDO, LadoPrincipal.escolher(listOf(a(0.9f)), listOf(a(0.6f))))
        assertEquals(Lado.DIREITO, LadoPrincipal.escolher(listOf(a(0.6f)), listOf(a(0.9f))))
    }
}
