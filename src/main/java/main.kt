package main

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.concurrent.thread

//https://broker.negociecoins.com.br/api/v3/{PAR}/ticker
//https://api.blinktrade.com/api/v1/BRL/trades

fun main(args: Array<String>){
    thread() {
        cotarMercadoBitCoin()
    }

    thread(){
        cotarFoxbit()
    }
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