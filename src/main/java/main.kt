package main

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal

//https://broker.negociecoins.com.br/api/v3/{PAR}/ticker
//https://api.blinktrade.com/api/v1/BRL/trades
//ticker -> vol

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
    val f1 = listOf(
        Field("vol", "bigDecimal", "Volume"),
        Field("buy", "bigDecimal", "Comprar"),
        Field("sell", "bigDecimal", "Vender")
    )
    val f2 = listOf(
        Field("ticker->vol", "bigDecimal", "Volume"),
        Field("ticker->buy", "bigDecimal", "Comprar"),
        Field("ticker->sell", "bigDecimal", "Vender")
    )
    val f3 = listOf(
        Field("baseVolume24", "bigDecimal", "Volume"),
        Field("highestBid", "bigDecimal", "Comprar"),
        Field("lowestAsk", "bigDecimal", "Vender")
    )

    val list = listOf(
        Corretora("NegocieCoins", "https://broker.negociecoins.com.br/api/v3/BTC/ticker", f1, null),
        Corretora("MercadoBitcoin", "https://www.mercadobitcoin.net/api/BTC/ticker/", f2, null),
        Corretora("Foxbit","https://api.blinktrade.com/api/v1/BRL/ticker", f1, null),
        Corretora("Braziliex","https://braziliex.com/api/v1/public/ticker/btc_brl", f3, null),
        Corretora("BitcoinToYou", "https://www.bitcointoyou.com/api/ticker.aspx", f2, null)
    )

    for (l in list) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(l.url)
            .get()
            .build();
        val response: Response = client.newCall(request).execute()
        val jsonString = response.body()?.string()

        val o = JSONObject(jsonString)

        println("===============${l.name}==================")
        for (f in l.fields){
            val value = geto(o, f)
            println("${f.name} \t= ${value}")
        }
    }

    //val body = RequestBody.create()

    /*thread() {
        cotarMercadoBitCoin()
    }

    thread(){
        cotarFoxbit()
    }*/
}

fun cotarFoxbit(){
    val foxbit = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://api.blinktrade.com/")
        .build()
    val foxbitService = foxbit.create(FoxbitApi::class.java)

    val cotacoes = foxbitService.orderbook()
    val tickers = foxbitService.tickers()
    val trades = foxbitService.trades()
    println(trades.execute().body())
    println(tickers.execute().body())
    println(cotacoes.execute().body())
}

fun cotarMercadoBitCoin(){
    val mercadoBitCoin = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://www.mercadobitcoin.com.br/")
        .build()
    val mercadoBitcoinService = mercadoBitCoin.create(MercadoBitcoinApi::class.java)

    val cotacoes = mercadoBitcoinService.orderbook()
    val tickers = mercadoBitcoinService.ticker()
    val trades = mercadoBitcoinService.trades()

    println(trades.execute().body())
    println(tickers.execute().body())
    println(cotacoes.execute().body())
}