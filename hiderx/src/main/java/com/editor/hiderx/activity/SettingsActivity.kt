package com.editor.hiderx.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.editor.hiderx.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private var homePressed: Boolean = true
    private var mIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        ll_ratus_layer?.setOnClickListener()
        {
                homePressed = false
                RateUs.showRateUsLayer(this)
        }
        ll_change_password?.setOnClickListener()
        {
            homePressed = false
            val intent = Intent(this, CalculatorActivity::class.java)
            intent.putExtra(Utility.KEY_CHANGE_PASSWORD, true)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
        btn_back?.setOnClickListener(){
            onBackPressed()
        }
        ll_help?.setOnClickListener()
        {
            HiderUtils.showHelpDialog(this)
        }
        ll_legal?.setOnClickListener()
        {
            homePressed = false
            startActivity(Intent(this, LegalPolicyActivity::class.java))
        }
        ll_privacy?.setOnClickListener()
        {
            homePressed = false
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        try {
            RemotConfigUtils.setFirebaseRemoteConfig(this)
        } catch (e: java.lang.Exception) {
            FirebaseCrashlytics.getInstance().log(e.toString())
            FirebaseCrashlytics.getInstance().recordException(e)
        }}

    override fun onPause() {
        super.onPause()
        if(homePressed)
        {
            mIntent = Intent(this, CalculatorActivity::class.java)
            mIntent?.putExtra(Utility.IS_CALCULATOR, true)
            mIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onResume() {
        super.onResume()
        homePressed = true
        if(mIntent!=null)
        {
            startActivity(mIntent)
            mIntent = null
            finish()
        }
    }



}