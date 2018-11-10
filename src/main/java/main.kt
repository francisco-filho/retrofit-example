package main

import com.google.gson.JsonElement
import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.json.GsonJsonProvider
import com.jayway.jsonpath.spi.json.JacksonJsonProvider
import com.jayway.jsonpath.spi.json.JsonOrgJsonProvider
import com.jayway.jsonpath.spi.json.JsonSmartJsonProvider
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import main.jdbc.SqlTemplate
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.glassfish.tyrus.client.ClientManager
import org.json.JSONObject
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.concurrent.thread
import java.io.IOException
import java.math.BigInteger
import java.net.URI
import javax.websocket.*

fun main(args: Array<String>){
    val db = SqlTemplate(dataSource())

    val corretorasSql = "SELECT DISTINCT c.id, c.nome, a.url\n" +
            "FROM arbitragem.corretoras c\n" +
            "INNER JOIN arbitragem.api a ON a.corretora_id=c.id AND a.tipo = 'BTC'\n" +
            "INNER JOIN arbitragem.api_campos ac ON ac.api_id=a.id\n" +
            "WHERE a.descricao = 'Ticker' AND a.url IS NOT NULL"

    val camposSql = "SELECT a.id, c.nome, ac.campo, ac.tipo_dado, ac.transacao\n" +
            "FROM arbitragem.corretoras c\n" +
            "INNER JOIN arbitragem.api a ON a.corretora_id=c.id AND a.tipo = 'BTC'\n" +
            "INNER JOIN arbitragem.api_campos ac ON ac.api_id=a.id\n" +
            "WHERE a.descricao = 'Ticker' AND a.id = ?"

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