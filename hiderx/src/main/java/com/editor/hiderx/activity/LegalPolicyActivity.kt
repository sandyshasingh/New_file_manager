package com.editor.hiderx.activity

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.editor.hiderx.PrivacyPolicyActivity
import com.editor.hiderx.R
import com.editor.hiderx.RateUs
import com.editor.hiderx.Utility
import kotlinx.android.synthetic.main.activity_legal_policy.*

class LegalPolicyActivity : AppCompatActivity() {

    private var homePressed: Boolean = true
    private var mIntent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_legal_policy)


        textHolder?.movementMethod = LinkMovementMethod.getInstance()
        textHolder?.setLinkTextColor(ResourcesCompat.getColor(resources,R.color.grey500,null))
        btn_back?.setOnClickListener()
        {
            onBackPressed()
        }

        tv_privacy_policy?.setOnClickListener()
        {
            startActivity(Intent(this,PrivacyPolicyActivity::class.java))
        }

        tv_feedback?.setOnClickListener()
        {
            RateUs.feedbackDialog(this)
        }
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