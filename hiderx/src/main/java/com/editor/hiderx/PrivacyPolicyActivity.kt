package com.editor.hiderx

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.editor.hiderx.activity.CalculatorActivity
import kotlinx.android.synthetic.main.activity_privacy_policy.*

class PrivacyPolicyActivity : AppCompatActivity() {

    private var homePressed: Boolean = true
    private var mIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_policy)
        btn_back?.setOnClickListener()
        {
            onBackPressed()
        }
        // Let's display the progress in the activity title bar, like the
        // browser app does.
        web_view?.settings?.javaScriptEnabled = true

        val activity: Activity = this
        web_view?.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, progress: Int) {
                // Activities and WebViews measure progress with different scales.
                // The progress meter will automatically disappear when we reach 100%
                activity.setProgress(progress * 1000)
            }
        }
        web_view?.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView,
                errorCode: Int,
                description: String,
                failingUrl: String
            ) {
                Toast.makeText(activity, "Oh no! $description", Toast.LENGTH_SHORT).show()
            }
        }

        web_view?.loadUrl(getString(R.string.privacy_policy_url))

    }

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