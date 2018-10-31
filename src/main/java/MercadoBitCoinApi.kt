package main

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigDecimal

data class MercadoBitCoinOrderbookResult(
    val bids: List<List<BigDecimal>>,
    val asks: List<List<BigDecimal>>
)

data class MercadoBitCoinTicker(
    val high:   BigDecimal,
    val low:    BigDecimal,
    val vol:    BigDecimal,
    val last:   BigDecimal,
    val buy:    BigDecimal,
    val sell:   BigDecimal,
    val date:   Long
)

data class MercadoBitCoinTickerResult(val ticker: MercadoBitCoinTicker){
    fun printMe(){
        println(this)
    }
}

data class MercadoBitcoinTradesResult(
    val date: Int,
    val price: BigDecimal,
    val amount: BigDecimal,
    val tid: Long,
    val type: String
)

interface MercadoBitcoinApi {
    @GET("/api/{coin}/orderbook")
    fun orderbook(@Path("coin") coin: String = "BTC"): Call<MercadoBitCoinOrderbookResult>

    @GET("/api/{coin}/ticker")
    fun ticker(@Path("coin") coin: String = "BTC"): Call<MercadoBitCoinTickerResult>

    @GET("/api/{coin}/trades")
    fun trades(@Path("coin") coin: String = "BTC"): Call<List<MercadoBitcoinTradesResult>>
}