package main

import org.glassfish.tyrus.client.ClientManager
import org.json.JSONObject
import java.io.IOException
import java.net.URI
import javax.websocket.*

class MessageToYou: MessageHandler.Whole<String>{
    override fun onMessage(msg: String?) {
        println(msg)
    }
}

fun main(args: Array<String>){
    val cec = ClientEndpointConfig.Builder.create().build()
    val client = ClientManager.createClient()

    client.connectToServer(object : Endpoint() {
        override fun onOpen(session: Session, config: EndpointConfig) {
            println("conectando...")
            val messageFrame = JSONObject()
            val payload = JSONObject()

            messageFrame.put("n", "WebAuthenticateUser")
            payload.put("UserName", "UserName")
            payload.put("Password", "UserName")

            val s = messageFrame.toString()
            try {
                session.addMessageHandler(MessageToYou())
                session.getBasicRemote().sendText(s)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun onError(session: Session?, thr: Throwable?) {
            println(thr)
            super.onError(session, thr)
        }
    }, cec, URI("wss://apifoxbitprodlb.alphapoint.com/WSGateway/"))

    Thread.sleep(10000)
}