package com.aemerse.iap

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper

abstract class IBillingService {

    private val purchaseServiceListeners: MutableList<PurchaseServiceListener> = mutableListOf()
    private val subscriptionServiceListeners: MutableList<SubscriptionServiceListener> =
        mutableListOf()
    private val billingClientConnectedListeners: MutableList<BillingClientConnectionListener> =
        mutableListOf()

    fun addBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
        billingClientConnectedListeners.add(billingClientConnectionListener)
    }

    fun removeBillingClientConnectionListener(billingClientConnectionListener: BillingClientConnectionListener) {
        billingClientConnectedListeners.remove(billingClientConnectionListener)
    }

    fun addPurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.add(purchaseServiceListener)
    }

    fun removePurchaseListener(purchaseServiceListener: PurchaseServiceListener) {
        purchaseServiceListeners.remove(purchaseServiceListener)
    }

    fun addSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.add(subscriptionServiceListener)
    }

    fun removeSubscriptionListener(subscriptionServiceListener: SubscriptionServiceListener) {
        subscriptionServiceListeners.remove(subscriptionServiceListener)
    }

    /**
     * @param purchaseInfo       - product specifier
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored product
     */
    fun productOwned(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            productOwnedInternal(purchaseInfo, isRestore)
        }
    }

    private fun productOwnedInternal(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        for (purchaseServiceListener in purchaseServiceListeners) {
            if (isRestore) {
                purchaseServiceListener.onProductRestored(purchaseInfo)
            } else {
                purchaseServiceListener.onProductPurchased(purchaseInfo)
            }
        }
    }

    /**
     * @param purchaseInfo       - subscription specifier
     * @param isRestore - a flag indicating whether it's a fresh purchase or restored subscription
     */
    fun subscriptionOwned(purchaseInfo: DataWrappers.PurchaseInfo, isRestore: Boolean) {
        findUiHandler().post {
            subscriptionOwnedInternal(purchaseInfo, isRestore)
        }
    }

    private fun subscriptionOwnedInternal(
        purchaseInfo: DataWrappers.PurchaseInfo,
        isRestore: Boolean
    ) {
        for (subscriptionServiceListener in subscriptionServiceListeners) {
            if (isRestore) {
                subscriptionServiceListener.onSubscriptionRestored(purchaseInfo)
            } else {
                subscriptionServiceListener.onSubscriptionPurchased(purchaseInfo)
            }
        }
    }

    fun isBillingClientConnected(status: Boolean, responseCode: Int) {
        findUiHandler().post {
            for (billingServiceListener in billingClientConnectedListeners) {
                billingServiceListener.onConnected(status, responseCode)
            }
        }
    }

    fun updatePrices(iapkeyPrices: Map<String, DataWrappers.SkuDetails>) {
        findUiHandler().post {
            updatePricesInternal(iapkeyPrices)
        }
    }

    private fun updatePricesInternal(iapkeyPrices: Map<String, DataWrappers.SkuDetails>) {
        for (billingServiceListener in purchaseServiceListeners) {
            billingServiceListener.onPricesUpdated(iapkeyPrices)
        }
        for (billingServiceListener in subscriptionServiceListeners) {
            billingServiceListener.onPricesUpdated(iapkeyPrices)
        }
    }

    abstract fun init(key: String?)
    abstract fun buy(activity: Activity, sku: String)
    abstract fun subscribe(activity: Activity, sku: String)
    abstract fun unsubscribe(activity: Activity, sku: String)
    abstract fun enableDebugLogging(enable: Boolean)

    @CallSuper
    open fun close() {
        subscriptionServiceListeners.clear()
        purchaseServiceListeners.clear()
        billingClientConnectedListeners.clear()
    }
}

fun findUiHandler(): Handler {
    return Handler(Looper.getMainLooper())
}