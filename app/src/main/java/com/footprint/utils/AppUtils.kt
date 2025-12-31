package com.footprint.utils

import android.content.Context
import android.content.pm.PackageManager
import java.security.MessageDigest
import java.util.Locale

object AppUtils {
    @Suppress("DEPRECATION")
    fun getAppSignature(context: Context): String {
        try {
            val packageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
            val signatures = packageInfo.signatures
            val cert = signatures[0].toByteArray()
            val md = MessageDigest.getInstance("SHA1")
            val publicKey = md.digest(cert)
            val hexString = StringBuilder()
            for (i in publicKey.indices) {
                val appendString = Integer.toHexString(0xFF and publicKey[i].toInt()).uppercase(Locale.US)
                if (appendString.length == 1) hexString.append("0")
                hexString.append(appendString)
                if (i < publicKey.size - 1) hexString.append(":")
            }
            return hexString.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return "获取失败"
        }
    }
}
