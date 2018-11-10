package main

import com.jayway.jsonpath.JsonPath
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import main.jdbc.SqlTemplate
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.concurrent.thread

fun main(args: Array<String>){
    cotacaoAtual()
    /*val maximo = 100
    val intervalo: Long = 1000 * 60 // 1 minuto
    var i = 0
    while (i < 100) {
        cotar()
        Thread.sleep(intervalo)
        i =+ 1
    }*/
}

fun cotacaoAtual(){
    val db = SqlTemplate(dataSource())

    val cotacoes = db.list(cotacoesSql)
    val taxas = db.list(taxasSql)
    val limites = db.list(limitesSql)

    val taxasGroup = taxas.groupBy {
        it.get("corretora_id")
    }
    val limitesGroup = limites.groupByTo(mutableMapOf()){
        it.get("corretora_id")
    }

    for (i in 1..cotacoes.size-1) {
        var g = cotacoes.get(i)
        g.put("taxas", mutableListOf<Map<String, Any>>())
        var gl = g.get("taxas") as MutableList<Map<String, Any>>
        val tx = taxasGroup.get(g.get("id"))
        if (tx != null && tx.size > 0)
            gl.addAll(tx)
    }

    println(cotacoes)
    println(taxasGroup)
    println(limitesGroup)

}

fun cotar(){
    val db = SqlTemplate(dataSource())
    val cotacaoId = db.insertAndReturnId(insertCotacoesSql)

    db.list(corretorasSql).forEach { corretora ->
        thread {
            val client = OkHttpClient()
            val url = corretora["url"] as String
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response: Response = client.newCall(request).execute()
            val jsonString = response.body()?.string()

            db.list(camposSql, corretora["id"]).forEach { campo ->
                val value: BigDecimal = JsonPath
                    .parse(jsonString)
                    .read(campo.get("campo") as String , BigDecimal::class.java)
                println("${corretora.get("nome")} -> ${campo.get("transacao")} -> ${campo.get("campo")} \t= ${value}")
                db.executeUpdate(insertCotacoesItensSQL,
                    cotacaoId, corretora["id"], campo.get("transacao"), value)
            }
        }
    }
}

fun dataSource(): DataSource {
    val config = HikariConfig()
    config.jdbcUrl = "jdbc:postgresql://localhost:5432/portal"
    config.username = "postgres"
    config.password = "12345678"
    return HikariDataSource(config)
}

val insertCotacoesSql = "INSERT INTO arbitragem.cotacoes (moeda) VALUES ('BTC') RETURNING id";
val insertCotacoesItensSQL = "INSERT INTO arbitragem.cotacoes_itens(\n" +
        "cotacao_id, corretora_id, tipo, valor)\n" +
        "    VALUES (?, ?, ?, ?)\n"

val corretorasSql = "SELECT DISTINCT c.id, c.nome, a.url\n" +
        "FROM arbitragem.corretoras c\n" +
        "INNER JOIN arbitragem.api a ON a.corretora_id=c.id AND a.tipo = 'BTC'\n" +
        "INNER JOIN arbitragem.api_campos ac ON ac.api_id=a.id\n" +
        "WHERE a.descricao = 'Ticker' AND a.url IS NOT NULL AND c.ativa"

val camposSql = "SELECT a.id, c.nome, ac.campo, ac.tipo_dado, ac.transacao\n" +
        "FROM arbitragem.corretoras c\n" +
        "INNER JOIN arbitragem.api a ON a.corretora_id=c.id AND a.tipo = 'BTC'\n" +
        "INNER JOIN arbitragem.api_campos ac ON ac.api_id=a.id\n" +
        "WHERE a.descricao = 'Ticker' AND a.id = ? AND c.ativa"

val cotacoesSql = "-- Cotações\n" +
        "SELECT c.id, c.nome, co.moeda, co.cotado_em\n" +
        "\t, SUM(CASE WHEN ci.tipo = 'Comprar' THEN ci.valor END) Comprar\n" +
        "\t, SUM(CASE WHEN ci.tipo = 'Vender' THEN ci.valor END) Vender\n" +
        "\t, SUM(CASE WHEN ci.tipo = 'Volume' THEN ci.valor END) Volume\n" +
        "FROM (SELECT * FROM arbitragem.cotacoes ORDER BY id DESC LIMIT 1) co\n" +
        "INNER JOIN arbitragem.cotacoes_itens ci ON ci.cotacao_id=co.id\n" +
        "INNER JOIN arbitragem.corretoras c ON c.id=ci.corretora_id\n" +
        "WHERE c.ativa\n" +
        "GROUP BY 1, 2, 3, 4"

val taxasSql = "SELECT t.corretora_id\n" +
        "\t, t.moeda\n" +
        "\t, t.taxa\n" +
        "\t, t.metrica\n" +
        "\t, t.valor\n" +
        "FROM arbitragem.taxas t\n" +
        "INNER JOIN arbitragem.corretoras c ON c.id=t.corretora_id AND c.ativa\n" +
        "ORDER BY t.ordem"

val limitesSql = "SELECT l.corretora_id\n" +
        "\t, l.moeda\n" +
        "\t, l.limite\n" +
        "\t, l.metrica\n" +
        "\t, l.limite_minimo\n" +
        "\t, l.limite_diario\n" +
        "\t, l.limite_mensal\n" +
        "FROM arbitragem.limites l\n" +
        "INNER JOIN arbitragem.corretoras c ON c.id=l.corretora_id AND c.ativa"