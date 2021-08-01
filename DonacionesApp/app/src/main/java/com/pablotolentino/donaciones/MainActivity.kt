package com.pablotolentino.donaciones

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.get
import androidx.core.view.marginBottom
import com.android.billingclient.api.*
import com.pablotolentino.donaciones.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("ResourceType")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val activity: Activity = this;

    private lateinit var billingClient: BillingClient
    private lateinit var donacionSeleccionada: SkuDetails

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.habilitarBotonPagoDonacion(false)
        this.iniciarConexionGooglePlay()

    }


    public fun iniciarProcesoPago(view: View) {
        iniciarPagoDonacion()
    }

    private fun iniciarPagoDonacion() {
        // An activity reference from which the billing flow will be launched.
        val activity: Activity = this;
        if (!billingClient.isReady) {
            return
        }

        var skuDetails = donacionSeleccionada
        // Retrieve a value for "skuDetails" by calling querySkuDetailsAsync().
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        val responseCode = billingClient.launchBillingFlow(activity, flowParams).responseCode
    }

    private fun habilitarBotonPagoDonacion(habilitar: Boolean) {
        this.binding.botonIniciarPago.isClickable = habilitar
    }


    private fun informacionIncompleta() {
        this.mostrarMensaje("¡Faltan datos!");
        this.habilitarBotonPagoDonacion(true)
    }

    private fun pagoExitoso() {
        this.mostrarMensaje("¡Pago exitoso!")
        this.habilitarBotonPagoDonacion(true)
    }

    private fun mostrarMensaje(mensaje: String) {
        val duration = Toast.LENGTH_LONG
        Toast.makeText(applicationContext, mensaje, duration).show()
    }

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            // To be implemented in a later section.
        }

    private fun iniciarConexionGooglePlay() {

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    GlobalScope.launch(Dispatchers.Main) {
                        querySkuDetails() // fetch on IO thread
                    }


                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })


    }

    suspend fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add("donacionpablotolentino")
        skuList.add("donacionvoluntaria50mxn")
        skuList.add("donacionvoluntaria5mxn")
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        // leverage querySkuDetails Kotlin extension function
        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient.querySkuDetails(params.build())
        }

        // Process the result.
        if (skuDetailsResult.skuDetailsList == null) {
            return
        }
        agregarTiposDonaciones(skuDetailsResult.skuDetailsList)
        this.habilitarBotonPagoDonacion(true)
    }

    fun agregarTiposDonaciones(tiposDonaciones: List<SkuDetails>?) {
        val radioGroup = findViewById<RadioGroup>(R.id.opciones_donaciones)

        if (radioGroup === null) {
            return
        }

        tiposDonaciones?.forEach {
            val tipoDonacion = RadioButton(this)
            tipoDonacion.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            tipoDonacion.setText(it.title)
            tipoDonacion.tag = it.sku
            val param = tipoDonacion.layoutParams as ViewGroup.MarginLayoutParams
            param.setMargins(10,40,10,10)
            tipoDonacion.layoutParams =param


            radioGroup.addView(tipoDonacion)

            radioGroup.setOnCheckedChangeListener { group, checkedId ->

                val value = group.get(checkedId)

                donacionSeleccionada = tiposDonaciones.find { donacion ->  donacion?.sku === value?.tag }!!

               // this.mostrarMensaje("${donacionSeleccionada.title}")
            }
        }

    }

/*



	private  fun iniciarConeccionGooglePlay(){
		billingClient.startConnection(object : BillingClientStateListener {
			override fun onBillingSetupFinished(billingResult: BillingResult) {
				if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
					// The BillingClient is ready. You can query purchases here.
					mostrarMensaje("google play conectado");
				}
			}
			override fun onBillingServiceDisconnected() {
				// Try to restart the connection on the next request to
				// Google Play by calling the startConnection() method.
				mostrarMensaje("google play desconectado");
			}
		})
	}

 */
}