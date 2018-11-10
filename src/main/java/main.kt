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
    val maximo = 100
    val intervalo: Long = 1000 * 60 // 1 minuto
    var i = 0
    while (i < 100) {
        cotar()
        Thread.sleep(intervalo)
        i =+ 1
    }
}

fun cotar(){
    val db = SqlTemplate(dataSource())
    val cotacaoId = db.executeUpdateReturnId(insertCotacoesSql)

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