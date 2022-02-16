package com.editor.hiderx.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.editor.hiderx.PASSWORD_FILE_NAME
import com.editor.hiderx.R
import com.editor.hiderx.StorageUtils
import com.editor.hiderx.Utility
import com.editor.hiderx.Utility.IS_CALCULATOR
import kotlinx.android.synthetic.main.activity_setup_password.*
import java.io.File


class ActivitySetupPasswordHelp : AppCompatActivity() {

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            startCalculator()
        }
        else
        {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_password)
        tv_skip?.setOnClickListener()
        {
            if(hasStoragePermission())
            {
                startCalculator()
            }
            else
            {
                startPermissionActivityForStorage()
            }
        }
        tv_got_it?.setOnClickListener()
        {
            if(hasStoragePermission())
            {
                startCalculator()
            }
            else
            {
                startPermissionActivityForStorage()
            }
        }
    }

    private fun startCalculator() {
        val intent = Intent(this, CalculatorActivity::class.java)
        intent.putExtra(IS_CALCULATOR,false)
        startActivity(intent)
        finish()
    }

    private fun hasStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )== PackageManager.PERMISSION_GRANTED
    }

    private fun startPermissionActivityForStorage() {
        val intent = Intent(this, PermissionActivity::class.java)
        intent.putExtra(Utility.KEY_STORAGE_PERMISSION, true)
        resultLauncher.launch(intent)
    }

}