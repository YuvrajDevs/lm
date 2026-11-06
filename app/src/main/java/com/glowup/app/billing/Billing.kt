package com.glowup.app.billing

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.glowup.app.ui.data.PlanType
import com.glowup.app.ui.data.ProPlan
import com.glowup.app.ui.data.ProStore

/** Google Play Billing: loads plan prices, runs purchases and keeps [ProStore] in sync with Play. */
object Billing : PurchasesUpdatedListener {
    enum class Status { CONNECTING, READY, UNAVAILABLE }

    var status by mutableStateOf(Status.CONNECTING)
        private set
    var lastError by mutableStateOf<String?>(null)
        private set
    private val details = mutableStateMapOf<String, ProductDetails>()

    private var client: BillingClient? = null
    private val productIds get() = ProStore.plans.map { it.productId }.toSet()

    fun start(context: Context) {
        if (client != null) return
        client = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        connect()
    }

    private fun connect() {
        status = Status.CONNECTING
        client?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    status = Status.READY
                    loadProducts()
                    refreshPurchases()
                } else {
                    status = Status.UNAVAILABLE
                }
            }

            override fun onBillingServiceDisconnected() {
                status = Status.UNAVAILABLE
            }
        })
    }

    fun retry() {
        if (status != Status.READY) connect() else refreshPurchases()
    }

    private fun loadProducts() {
        val billing = client ?: return
        listOf(BillingClient.ProductType.SUBS to PlanType.SUBSCRIPTION, BillingClient.ProductType.INAPP to PlanType.ONE_TIME).forEach { (playType, planType) ->
            val products = ProStore.plans.filter { it.type == planType }.map {
                QueryProductDetailsParams.Product.newBuilder().setProductId(it.productId).setProductType(playType).build()
            }
            if (products.isEmpty()) return@forEach
            billing.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(products).build()) { result, list ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) list.forEach { details[it.productId] = it }
            }
        }
    }

    /** The localised price from Google Play, once loaded. */
    fun price(plan: ProPlan): String? {
        val product = details[plan.productId] ?: return null
        return when (plan.type) {
            PlanType.ONE_TIME -> product.oneTimePurchaseOfferDetails?.formattedPrice
            PlanType.SUBSCRIPTION -> basePlan(product)?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice
        }
    }

    /** Days of free trial on this plan's best offer, if Play Console has one set up. */
    fun trialDays(plan: ProPlan): Int? {
        val offer = trialOffer(details[plan.productId] ?: return null) ?: return null
        val period = offer.pricingPhases.pricingPhaseList.first { it.priceAmountMicros == 0L }.billingPeriod
        return Regex("P(\\d+)([DW])").find(period)?.destructured?.let { (count, unit) -> count.toInt() * if (unit == "W") 7 else 1 }
    }

    fun isLoaded(plan: ProPlan) = plan.productId in details

    private fun basePlan(product: ProductDetails) = product.subscriptionOfferDetails?.firstOrNull { it.offerId == null }
        ?: product.subscriptionOfferDetails?.firstOrNull()

    private fun trialOffer(product: ProductDetails) = product.subscriptionOfferDetails?.firstOrNull { offer ->
        offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L }
    }

    /** Opens Google Play's purchase sheet. Returns false if the plan can't be bought right now. */
    fun purchase(activity: Activity, plan: ProPlan): Boolean {
        val billing = client ?: return false
        val product = details[plan.productId] ?: return false
        val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).apply {
            if (plan.type == PlanType.SUBSCRIPTION) {
                val offer = trialOffer(product) ?: basePlan(product) ?: return false
                setOfferToken(offer.offerToken)
            }
        }.build()
        val result = billing.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
        return result.responseCode == BillingClient.BillingResponseCode.OK
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach(::handle)
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refreshPurchases()
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> lastError = "The purchase didn't go through. Please try again."
        }
    }

    private fun handle(purchase: Purchase) {
        if (purchase.products.none { it in productIds }) return
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        ProStore.updatePurchased(true)
        ProStore.dismissPaywall()
        if (!purchase.isAcknowledged) {
            client?.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) { }
        }
    }

    /** Re-checks Play for active Pro purchases; also used by "Restore purchases". */
    fun refreshPurchases(onDone: ((Boolean) -> Unit)? = null) {
        val billing = client
        if (billing == null || status != Status.READY) {
            onDone?.invoke(false)
            return
        }
        var found = false
        var pending = 2
        var allOk = true
        listOf(BillingClient.ProductType.SUBS, BillingClient.ProductType.INAPP).forEach { type ->
            billing.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(type).build()) { result, purchases ->
                if (result.responseCode != BillingClient.BillingResponseCode.OK) allOk = false
                purchases.filter { p -> p.purchaseState == Purchase.PurchaseState.PURCHASED && p.products.any { it in productIds } }.forEach {
                    found = true
                    handle(it)
                }
                if (--pending == 0) {
                    if (allOk) ProStore.updatePurchased(found)
                    onDone?.invoke(found)
                }
            }
        }
    }

    fun clearError() {
        lastError = null
    }
}
