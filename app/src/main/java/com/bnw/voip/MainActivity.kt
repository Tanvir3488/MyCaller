package com.bnw.voip

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.sip.SipManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.Constants
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val customeSipManager = CustomeSipManager(this)
        customeSipManager.start()

            customeSipManager.login()

        findViewById<Button>(R.id.call).setOnClickListener {
           val number = findViewById<TextView>(R.id.number)
            customeSipManager.call(number.text.toString())
        }

        findViewById<Button>(R.id.endCall).setOnClickListener {

            customeSipManager.hangup()
        }

       testUdpConnection()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        Log.e("PJSip", "Is network connected: $isConnected")
    }

    fun testUdpConnection() {
        Thread {
            try {
                Log.e("NetworkTest", "Testing UDP connection...")
                val socket = DatagramSocket()
                val address = InetAddress.getByName(Constants.DOMAIN)
                Log.e("NetworkTest", "Server IP: ${address.hostAddress}")

                // Try to bind
                socket.connect(address, 5060)
                Log.e("NetworkTest", "UDP connection successful!")
                socket.close()
            } catch (e: Exception) {
                Log.e("NetworkTest", "UDP connection failed: ${e.message}", e)
            }
        }.start()
    }
}
