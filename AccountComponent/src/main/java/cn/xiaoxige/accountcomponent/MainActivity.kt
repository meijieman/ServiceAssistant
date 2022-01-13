package cn.xiaoxige.accountcomponent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.xiaoxige.loginapi.ILoginAbilityApi
import cn.xiaoxige.serviceassistantcore.Service

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account_activity_main)

        Service.getService(ILoginAbilityApi::class.java)?.toLogin(this)
    }
}