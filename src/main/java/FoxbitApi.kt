package main

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.math.BigDecimal

data class FoxbitOrderbookResult(
    val pair: String,
    val bids: List<List<BigDecimal>>,
    val asks: List<List<BigDecimal>>
)

data class FoxbitTradeResult(
    val seller_id: Long,
    val price: BigDecimal,
    val date: Long,
    val amount: BigDecimal,
    val tid: Int,
    val side: String,
    val buyer_id: Long
)

data class FoxbitTickerResult(
    val high: BigDecimal,
    val vol: BigDecimal,
    val buy: BigDecimal,
    val last: BigDecimal,
    val low: BigDecimal,
    val pair: String,
    val sell: BigDecimal,
    val vol_brl: BigDecimal
)

interface FoxbitApi {
    @GET("/api/v1/{coin}/trades")
    fun trades(@Path("coin") coin: String = "BRL"): Call<List<FoxbitTradeResult>>

    @GET("/api/v1/{coin}/orderbook")
    fun orderbook(@Path("coin") coin: String = "BRL"): Call<FoxbitOrderbookResult>

    @GET("/api/v1/{coin}/ticker")
    fun tickers(@Path("coin") coin: String= "BRL"): Call<FoxbitTickerResult>
}