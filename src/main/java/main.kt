package main

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal


data class Repo(val id: Long, val name: String)
data class MercadoBitCoinOrderbook(val bids: List<List<BigDecimal>>, val asks: List<List<BigDecimal>>)

interface GitHubService {
    @GET("users/{user}/repos")
    fun listRepos(@Path("user") user: String): Call<List<Repo>>
}

interface MercadoBitCoin {
    @GET("/api/{coin}/orderbook")
    fun orderbook(@Path("coin") coin: String = "BTC"): Call<MercadoBitCoinOrderbook>
}

fun main(args: Array<String>){
    val mercadoBitCoin = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .baseUrl("https://www.mercadobitcoin.com.br/")
        .build()

    val coinService = mercadoBitCoin.create(MercadoBitCoin::class.java)
    val cotacoes = coinService.orderbook("BTC")

    var result : MercadoBitCoinOrderbook? = null

    val ctx = cotacoes.execute().body()

    cotacoes.enqueue(object: Callback<MercadoBitCoinOrderbook>{
        override fun onFailure(p0: Call<MercadoBitCoinOrderbook>, p1: Throwable) {
            println(p1)
        }

        override fun onResponse(p0: Call<MercadoBitCoinOrderbook>, response: Response<MercadoBitCoinOrderbook>) {
            response?.let{
                result = it.body()
                println(it.body())
            }
        }
    })

    println(result)
}