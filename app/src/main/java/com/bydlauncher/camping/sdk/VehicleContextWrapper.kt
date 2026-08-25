package com.bydlauncher.camping.sdk

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager

/**
 * BydAutoStart의 VehicleContextWrapper 이식.
 * BYD SDK의 권한 체크를 모두 GRANTED로 우회한다.
 */
class VehicleContextWrapper(ctx: Context) : ContextWrapper(ctx.applicationContext) {
    override fun getApplicationContext(): Context = this
    override fun checkPermission(permission: String, pid: Int, uid: Int) = PackageManager.PERMISSION_GRANTED
    override fun checkCallingPermission(permission: String) = PackageManager.PERMISSION_GRANTED
    override fun checkCallingOrSelfPermission(permission: String) = PackageManager.PERMISSION_GRANTED
    override fun checkSelfPermission(permission: String) = PackageManager.PERMISSION_GRANTED
    override fun enforcePermission(permission: String, pid: Int, uid: Int, message: String?) {}
    override fun enforceCallingPermission(permission: String, message: String?) {}
    override fun enforceCallingOrSelfPermission(permission: String, message: String?) {}
}
