package br.com.movimentoemfoco.app.analysis

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GeometriaTest {
    @Test fun angulo90() = assertEquals(90.0, Geometria.anguloGraus(1.0, 0.0, 0.0, 0.0, 0.0, 1.0)!!, 1e-9)
    @Test fun angulo180() = assertEquals(180.0, Geometria.anguloGraus(1.0, 0.0, 0.0, 0.0, -1.0, 0.0)!!, 1e-9)
    @Test fun angulo0() = assertEquals(0.0, Geometria.anguloGraus(1.0, 0.0, 0.0, 0.0, 2.0, 0.0)!!, 1e-9)
    @Test fun angulo45() = assertEquals(45.0, Geometria.anguloGraus(1.0, 0.0, 0.0, 0.0, 1.0, 1.0)!!, 1e-9)
    @Test fun segmentoDegeneradoRetornaNulo() = assertNull(Geometria.anguloGraus(0.0, 0.0, 0.0, 0.0, 1.0, 1.0))
    @Test fun valorNaoNumericoRetornaNulo() = assertNull(Geometria.anguloGraus(Double.NaN, 0.0, 0.0, 0.0, 1.0, 1.0))
}
