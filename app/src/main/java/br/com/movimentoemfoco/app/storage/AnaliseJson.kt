package br.com.movimentoemfoco.app.storage

import br.com.movimentoemfoco.app.model.AnaliseResultado
import br.com.movimentoemfoco.app.model.Lado
import br.com.movimentoemfoco.app.model.NivelQualidade
import br.com.movimentoemfoco.app.model.QuadroResultado
import br.com.movimentoemfoco.app.model.TipoMidia
import org.json.JSONArray
import org.json.JSONObject

/** Serialização simples em JSON (sem banco de dados). Lança exceção se o JSON for inválido. */
object AnaliseJson {
    const val VERSAO = 1

    fun paraJson(r: AnaliseResultado): JSONObject = JSONObject().apply {
        put("versao", VERSAO)
        put("id", r.id)
        put("criadaEmMs", r.criadaEmMs)
        put("tipo", r.tipo.name)
        put("nomeMidia", r.nomeMidia)
        r.duracaoMs?.let { put("duracaoMs", it) }
        put("quadrosAnalisados", r.quadrosAnalisados)
        put("quadrosComPessoa", r.quadrosComPessoa)
        put("quadrosValidos", r.quadrosValidos)
        put("qualidade", r.qualidade.name)
        r.ladoPrincipal?.let { put("ladoPrincipal", it.name) }
        put("resumo", r.resumo)
        put("quadros", JSONArray().apply {
            r.quadros.forEach { q ->
                put(JSONObject().apply {
                    put("tempoMs", q.tempoMs)
                    put("arquivo", q.arquivo)
                    put("detectado", q.detectado)
                    q.anguloEsquerdo?.let { put("anguloEsquerdo", it) }
                    q.anguloDireito?.let { put("anguloDireito", it) }
                })
            }
        })
    }

    fun deJson(o: JSONObject): AnaliseResultado {
        val quadros = o.getJSONArray("quadros")
        return AnaliseResultado(
            id = o.getString("id"),
            criadaEmMs = o.getLong("criadaEmMs"),
            tipo = TipoMidia.valueOf(o.getString("tipo")),
            nomeMidia = o.getString("nomeMidia"),
            duracaoMs = if (o.has("duracaoMs")) o.getLong("duracaoMs") else null,
            quadrosAnalisados = o.getInt("quadrosAnalisados"),
            quadrosComPessoa = o.getInt("quadrosComPessoa"),
            quadrosValidos = o.getInt("quadrosValidos"),
            qualidade = NivelQualidade.valueOf(o.getString("qualidade")),
            ladoPrincipal = if (o.has("ladoPrincipal")) Lado.valueOf(o.getString("ladoPrincipal")) else null,
            resumo = o.getString("resumo"),
            quadros = (0 until quadros.length()).map { i ->
                val q = quadros.getJSONObject(i)
                QuadroResultado(
                    tempoMs = q.getLong("tempoMs"),
                    arquivo = q.getString("arquivo"),
                    detectado = q.getBoolean("detectado"),
                    anguloEsquerdo = if (q.has("anguloEsquerdo")) q.getDouble("anguloEsquerdo") else null,
                    anguloDireito = if (q.has("anguloDireito")) q.getDouble("anguloDireito") else null,
                )
            },
        )
    }
}
