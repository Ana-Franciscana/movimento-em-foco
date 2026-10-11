package br.com.movimentoemfoco.app.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelecaoQuadrosTest {
    private fun q(i: Int, ang: Double?, det: Boolean = ang != null) = QuadroMedido(i, i * 1000L, det, ang)

    @Test fun escolheMinimoMaximoPrimeiroEUltimoEmOrdemDeTempo() {
        val quadros = listOf(q(0, 170.0), q(1, 150.0), q(2, 100.0), q(3, 120.0), q(4, 168.0), q(5, 165.0))
        // mínimo = 2, máximo = 0, primeiro = 0, último = 5
        assertEquals(listOf(0, 2, 5), SelecaoQuadros.selecionar(quadros))
    }

    @Test fun ignoraQuadrosSemAngulo() {
        val quadros = listOf(q(0, null), q(1, 140.0), q(2, null), q(3, 90.0))
        assertEquals(listOf(1, 3), SelecaoQuadros.selecionar(quadros))
    }

    @Test fun semAngulosUsaQuadrosComPessoaDetectada() {
        val quadros = listOf(q(0, null, false), q(1, null, true), q(2, null, true))
        assertEquals(listOf(1, 2), SelecaoQuadros.selecionar(quadros))
    }

    @Test fun semNadaDetectadoDevolveVazio() {
        assertTrue(SelecaoQuadros.selecionar(listOf(q(0, null, false), q(1, null, false))).isEmpty())
        assertTrue(SelecaoQuadros.selecionar(emptyList()).isEmpty())
    }

    @Test fun respeitaOMaximo() {
        val quadros = (0 until 12).map { q(it, 100.0 + it * 5) }
        assertTrue(SelecaoQuadros.selecionar(quadros, 2).size <= 2)
        assertTrue(SelecaoQuadros.selecionar(quadros).size <= SelecaoQuadros.MAXIMO_PADRAO)
    }
}
