package main

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import main.jdbc.SqlTemplate
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.math.BigDecimal
import javax.sql.DataSource
import kotlin.concurrent.thread

data class Field(val name: String, val datatype: String, val type: String)
data class Corretora(val name: String, val url: String, val fields: List<Field>, var taxas: Taxas?)
data class Transacao(val crypto: BigDecimal, val real: BigDecimal)
//com base na negocie coins
data class Taxas (
    val deposito: BigDecimal= BigDecimal("0.0"),
    val depositoCrypto: BigDecimal = BigDecimal("0.0"),
    val venda: BigDecimal   = BigDecimal("0.4"),
    val compra: BigDecimal  = BigDecimal("0.3"),
    val saque: BigDecimal   = BigDecimal("0.9"),
    val saqueExtra: BigDecimal = BigDecimal("8.9"),
    val limiteMinDiario: BigDecimal = BigDecimal("0.0001"),
    val limiteMaxDiario: BigDecimal = BigDecimal("0.5"),
    val saqueExtraTipo: String = "BRL",
    val vendaTipo: String = "%",
    val compraTipo: String = "%",
    val bancos: List<String> = listOf("CAIXA", "BB")
)

// operacao,

// corretora, operacao=DEPOSITO_DINHEIRO, moeda=BRL, unidade=R$, valor=0.0
// corretora, operacao=SAQUE, moeda=BRL, unidade=percentual, valor=0.9
// corretora, operacao=SAQUE, moeda=BRL, unidade=R$, valor=8.9
// corretora, operacao=COMPRA, moeda=BRL, unidade=R$, valor=0.3
// corretora, operacao=SALDO, moeda=BTC, valor=100%
// corretora, operacao=SALDO, moeda=BRL, valor=100%

// fluxo da operacao

// deposito
// corretora, operacao=DEPOSITO_DINHEIRO, BRL, sinal=-
// corretora, operacao=SALDO, BRL, sinal=+
// compra
// corretora, operacao=DEPOSITO_DINHEIRO, BRL, sinal=-
// corretora, operacao=COMPRA, BRL, sinal=-
// corretora, operacao=SALDO, BTC, sinal=+
// venda
// corretora, operacao=VENDA, BTC, sinal=-
// corretora, operacao=VENDA, BRL, sinal=-
// corretora, operacao=SALDO, BTC, sinal=-
// corretora, operacao=SALDO, BRL, sinal=+

fun geto(o: JSONObject, field: Field): Any {
    val keys = field.name.split("->")
    val size = keys.size
    if (size == 1) return getByType(o, keys[0], field.datatype)

    var k1 = o.getJSONObject(keys[0])

    for ((i, v) in keys.iterator().withIndex()) {
        if (i == 0) continue
        if (i == size -1)
            return getByType(k1, v, field.datatype)
    }

    return k1
}

fun getFromMap(o: JSONObject, field: Map<String, Any>): Any {
    val keys = field.get("campo").toString().split("->")
    val size = keys.size
    if (size == 1) return getByType(o, keys[0], field.get("tipo_dado").toString())

    var k1 = o.getJSONObject(keys[0])

    for ((i, v) in keys.iterator().withIndex()) {
        if (i == 0) continue
        if (i == size -1)
            return getByType(k1, v, field.get("tipo_dado").toString())
    }

    return k1
}

fun getByType(o: JSONObject, k: String, type: String): Any {
    return when (type) {
        "bigDecimal" -> o.getBigDecimal(k)
        "double"    -> o.getDouble(k)
        "text"      -> o.getString(k)
        "integer"   -> o.getInt(k)
        else        -> {
            o.getJSONObject(k)
        }
    }
}

fun main(args: Array<String>){
    val db = SqlTemplate(dataSource())

    val sql = "SELECT DISTINCT c.id, c.nome, a.url\n" +
            "FROM arbitragem.corretoras c\n" +
            "INNER JOIN arbitragem.api a ON a.corretora_id=c.id AND a.tipo = 'BTC'\n" +
            "INNER JOIN arbitragem.api_campos ac ON ac.api_id=a.id\n" +
            "WHERE a.descricao = 'Ticker' AND a.url IS NOT NULL"

    val sqlCampos = "SELECT a.id, c.nome, ac.campo, ac.tipo_dado, ac.transacao\n" +
            "FROM arbitragem.corretoras c\n" +
            "INNER JOIN arbitragem.api a ON a.corretora_id=c.id AND a.tipo = 'BTC'\n" +
            "INNER JOIN arbitragem.api_campos ac ON ac.api_id=a.id\n" +
            "WHERE a.descricao = 'Ticker' AND a.id = ?"

    db.list(sql).forEach { corretora ->
        thread {
            val client = OkHttpClient()
            val url = corretora["url"] as String
            val request = Request.Builder()
                .url(url)
                .get()
                .build();
            val response: Response = client.newCall(request).execute()
            val jsonString = response.body()?.string()

            val o = JSONObject(jsonString)
            println(o)

            db.list(sqlCampos, corretora["id"]).forEach { campo ->
                val value = getFromMap(o, campo)
                println("${campo.get("transacao")} -> ${campo.get("campo")} \t= ${value}")
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