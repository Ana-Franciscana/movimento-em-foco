package br.com.movimentoemfoco.app.analysis

import br.com.movimentoemfoco.app.model.Keypoint
import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.ResultadoPose
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AngulosPoseTest {

    private fun pose(w: Int = 200, h: Int = 100, bloco: (MutableList<Keypoint>) -> Unit): ResultadoPose {
        val pontos = MutableList(33) { Keypoint(0f, 0f, 1f, 1f) }
        bloco(pontos)
        return ResultadoPose(pontos, w, h)
    }

    /** Em 200x100: (0.5,0.5),(0,0),(0.5,0) viram pixels (100,50),(0,0),(100,0) => atan(0.5) = 26,565°. */
    @Test fun converteParaPixelsAntesDoAngulo() {
        val r = pose {
            it[23] = Keypoint(0.5f, 0.5f, 0.9f, 1f)
            it[25] = Keypoint(0f, 0f, 0.8f, 1f)
            it[27] = Keypoint(0.5f, 0f, 0.7f, 1f)
        }
        val a = AngulosPose.joelho(r, Lado.ESQUERDO)
        assertNotNull(a)
        assertEquals(26.565, a!!.graus, 0.01)       // em coordenadas normalizadas daria 45°
        assertEquals(0.7f, a.confianca, 1e-6f)      // menor visibilidade dos 3 pontos
    }

    @Test fun ladoDireitoUsaOsIndicesDireitos() {
        val r = pose(100, 100) {
            it[24] = Keypoint(1f, 0f, 0.9f, 1f)
            it[26] = Keypoint(0f, 0f, 0.9f, 1f)
            it[28] = Keypoint(0f, 1f, 0.9f, 1f)
        }
        assertEquals(90.0, AngulosPose.joelho(r, Lado.DIREITO)!!.graus, 1e-6)
    }

    @Test fun poseAusenteRetornaNulo() = assertNull(AngulosPose.joelho(null, Lado.ESQUERDO))

    @Test fun listaCurtaDePontosRetornaNulo() =
        assertNull(AngulosPose.joelho(ResultadoPose(List(20) { Keypoint(0f, 0f, 1f, 1f) }, 100, 100), Lado.ESQUERDO))

    @Test fun pontoComBaixaVisibilidadeNaoGeraAngulo() {
        val r = pose(100, 100) {
            it[23] = Keypoint(1f, 0f, 0.9f, 1f)
            it[25] = Keypoint(0f, 0f, 0.3f, 1f)      // joelho pouco visível
            it[27] = Keypoint(0f, 1f, 0.9f, 1f)
        }
        assertNull(AngulosPose.joelho(r, Lado.ESQUERDO))
    }

    @Test fun visibilidadeNaoNumericaNaoGeraAngulo() {
        val r = pose(100, 100) {
            it[23] = Keypoint(1f, 0f, Float.NaN, 1f)
            it[25] = Keypoint(0f, 0f, 0.9f, 1f)
            it[27] = Keypoint(0f, 1f, 0.9f, 1f)
        }
        assertNull(AngulosPose.joelho(r, Lado.ESQUERDO))
    }

    @Test fun pontosSobrepostosNaoGeramAngulo() {
        val r = pose(100, 100) {
            it[23] = Keypoint(0.5f, 0.5f, 0.9f, 1f)
            it[25] = Keypoint(0.5f, 0.5f, 0.9f, 1f)  // quadril == joelho
            it[27] = Keypoint(0f, 1f, 0.9f, 1f)
        }
        assertNull(AngulosPose.joelho(r, Lado.ESQUERDO))
    }
}
