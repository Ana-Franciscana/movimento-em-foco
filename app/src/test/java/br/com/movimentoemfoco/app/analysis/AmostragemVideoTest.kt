package br.com.movimentoemfoco.app.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AmostragemVideoTest {
    @Test fun duracaoInvalidaNaoGeraInstantes() {
        assertTrue(AmostragemVideo.instantesMs(0).isEmpty())
        assertTrue(AmostragemVideo.instantesMs(-5).isEmpty())
    }

    @Test fun respeitaOMaximoDeAmostras() =
        assertEquals(AmostragemVideo.MAX_AMOSTRAS, AmostragemVideo.instantesMs(20_000).size)

    @Test fun estritamenteCrescenteDentroDaDuracao() {
        val t = AmostragemVideo.instantesMs(10_000)
        assertTrue(t.zipWithNext().all { (a, b) -> b > a })
        assertTrue(t.first() > 0 && t.last() < 10_000)
    }

    @Test fun videoMuitoCurtoAindaTemTimestampsCrescentes() {
        val t = AmostragemVideo.instantesMs(5)
        assertEquals(5, t.size)
        assertTrue(t.zipWithNext().all { (a, b) -> b > a })
    }
}
