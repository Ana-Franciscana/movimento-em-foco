package br.com.movimentoemfoco.app.storage

import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.model.NivelQualidade
import br.com.movimentoemfoco.app.model.TipoMidia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class HistoricoRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun analise(id: String, criada: Long) = AnaliseResultado(
        id, criada, TipoMidia.IMAGEM, "x.jpg", null, 1, 1, 1, NivelQualidade.BOA, null, emptyList(), "ok",
    )

    @Test fun salvaListaEOrdenaDoMaisRecenteParaOMaisAntigo() {
        val repo = HistoricoRepository(tmp.newFolder("a"))
        repo.salvar(analise("id-1", 100))
        repo.salvar(analise("id-2", 300))
        repo.salvar(analise("id-3", 200))
        assertEquals(listOf("id-2", "id-3", "id-1"), repo.listar().map { it.id })
    }

    @Test fun persisteEntreInstanciasDoRepositorio() {
        val pasta = tmp.newFolder("b")
        HistoricoRepository(pasta).salvar(analise("id-1", 100))
        assertEquals("id-1", HistoricoRepository(pasta).carregar("id-1")?.id)   // "reabrir o app"
    }

    @Test fun ignoraItemCorrompidoEPastaSemJson() {
        val pasta = tmp.newFolder("c")
        val repo = HistoricoRepository(pasta)
        repo.salvar(analise("bom", 100))
        File(pasta, "ruim").mkdirs(); File(pasta, "ruim/resultado.json").writeText("{ não é json")
        File(pasta, "vazia").mkdirs()
        assertEquals(listOf("bom"), repo.listar().map { it.id })
        assertNull(repo.carregar("ruim"))
    }

    @Test fun removeApagaAPasta() {
        val repo = HistoricoRepository(tmp.newFolder("d"))
        repo.salvar(analise("id-1", 100))
        assertTrue(repo.remover("id-1"))
        assertFalse(repo.pasta("id-1").exists())
        assertTrue(repo.remover("id-1"))     // remover algo inexistente não é erro
    }

    @Test fun idComCaminhoRelativoEhRejeitado() {
        val repo = HistoricoRepository(tmp.newFolder("e"))
        assertFalse(repo.remover("../fora"))
        assertNull(repo.carregar("../fora"))
    }

    @Test fun listarSemPastaRaizRetornaVazio() =
        assertTrue(HistoricoRepository(File(tmp.root, "inexistente")).listar().isEmpty())
}
