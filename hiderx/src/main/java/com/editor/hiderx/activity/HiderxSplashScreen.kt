package com.editor.hiderx.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.text.TextUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.editor.hiderx.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.*
import java.io.*
import java.lang.Runnable
import java.net.HttpURLConnection
import java.net.URL

class HiderxSplashScreen : AppCompatActivity(), CoroutineScope by MainScope() {

    var dialog : AlertDialog? = null
    private var handler: Handler? = null
    var android_id = ""
    var password: String? = ""

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            password = HiderUtils.getSharedPreference(this, HiderUtils.PASSWORD_KEY)
            if (TextUtils.isEmpty(password)) {
                val file = File(StorageUtils.getHiderDirectory().path + "/$PASSWORD_FILE_NAME")
                if(!file.exists())
                {
                    welcomeOrSetUpScreen()
                }
                else
                {
                    readPasswordAndNavigate(file)
                }
                // var hasConnection: Boolean = false
                /* launch {
                     val operation = async(Dispatchers.IO) {

                         //  hasConnection = try {
                        //     val urlc = URL("http://clients3.google.com/generate_204")
                         //            .openConnection() as HttpURLConnection
                        //     urlc.connectTimeout = 10000
                           //  (urlc.responseCode == 204 && urlc.contentLength == 0)
                      //   } catch (e: IOException) {
                           //  false
                       //  }

                     }
                     operation.await()
                     withContext(Dispatchers.Main)
                     {

                       *//*  if (hasConnection) {
                        getFirebaseDataAndNavigate()
                    } else {
                        showConnectivityDialog()
                    }*//*
                }
            }*/
            } else {
                startCalculator()
            }
        }
        else
        {
            finish()
        }
    }

    private fun readPasswordAndNavigate(file: File) {
        val bufferedReader: BufferedReader = file.bufferedReader()
        val inputString = bufferedReader.use { it.readText() }
        password = inputString.toString()
        if (TextUtils.isEmpty(password)) {
            welcomeOrSetUpScreen()
        } else {
            HiderUtils.setSharedPreference(
                    this@HiderxSplashScreen,
                    HiderUtils.PASSWORD_KEY,
                    password
            )
            startCalculator()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_splash_screen)
        Handler(Looper.getMainLooper()).postDelayed(Runnable {
            decideNavigation()
        }, 1000)
    }

    private fun decideNavigation() {
       // android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        if(hasStoragePermission())
        {
            password = HiderUtils.getSharedPreference(this, HiderUtils.PASSWORD_KEY)
            if (TextUtils.isEmpty(password)) {
                val file = File(StorageUtils.getHiderDirectory().path + "/$PASSWORD_FILE_NAME")
                if(!file.exists())
                {
                    welcomeOrSetUpScreen()
                }
                else
                {
                        readPasswordAndNavigate(file)
                }
                // var hasConnection: Boolean = false
                /* launch {
                     val operation = async(Dispatchers.IO) {

                         //  hasConnection = try {
                        //     val urlc = URL("http://clients3.google.com/generate_204")
                         //            .openConnection() as HttpURLConnection
                        //     urlc.connectTimeout = 10000
                           //  (urlc.responseCode == 204 && urlc.contentLength == 0)
                      //   } catch (e: IOException) {
                           //  false
                       //  }

                     }
                     operation.await()
                     withContext(Dispatchers.Main)
                     {

                       *//*  if (hasConnection) {
                        getFirebaseDataAndNavigate()
                    } else {
                        showConnectivityDialog()
                    }*//*
                }
            }*/
            } else {
                startCalculator()
            }
        }
        else
        {
            startPermissionActivityForStorage()
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            Environment.isExternalStorageManager()
        else
        ContextCompat.checkSelfPermission(
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

    private fun getFirebaseDataAndNavigate() {

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("passwords")
        var containsChild = false
        myRef.get().addOnSuccessListener { parentData ->
            val parkingsData: Iterable<DataSnapshot> = parentData.children
            for (parking in parkingsData) {
                if (parking.key == android_id) {
                    containsChild = true
                    myRef.child(parking.key!!).get().addOnSuccessListener {
                        password = it.children.elementAt(0).value?.toString()
                        if (TextUtils.isEmpty(password)) {
                            welcomeOrSetUpScreen()
                        } else {
                            HiderUtils.setSharedPreference(
                                    this@HiderxSplashScreen,
                                    HiderUtils.PASSWORD_KEY,
                                    password
                            )
                            startCalculator()
                        }
                    }
                }
            }
            if (!containsChild) {
                welcomeOrSetUpScreen()
            }
        }
    }

    var runnable = Runnable {
        var hasConnection : Boolean = false
        launch {
            val operation = async(Dispatchers.IO) {
                hasConnection = try {
                    val urlc = URL("http://clients3.google.com/generate_204")
                            .openConnection() as HttpURLConnection
                    (urlc.responseCode == 204 && urlc.contentLength == 0)
                } catch (e: IOException) {
                    false
                }
            }
            operation.await()
            if (hasConnection) {
                if(dialog!=null && dialog?.isShowing!!)
                {
                    dialog?.dismiss()
                    dialog = null
                }
                getFirebaseDataAndNavigate()
                runThread(false)
            } else {
                runThread(true)
            }
        }
    }

    private fun runThread(runAgain: Boolean) {
        if (runAgain)
            handler?.postDelayed(runnable, 1000)
        else
            handler?.removeCallbacks(runnable)
    }

    private fun startCalculator() {
        launch {
            withContext(Dispatchers.Main)
            {
                val intent = Intent(this@HiderxSplashScreen, CalculatorActivity::class.java)
                intent.putExtra(Utility.IS_CALCULATOR, true)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }
    }

    private fun showConnectivityDialog() {
        launch {
            withContext(Dispatchers.Main)
            {
                val dialog1 = AlertDialog.Builder(this@HiderxSplashScreen)
                dialog1.setTitle(getString(R.string.connect_to_internet))
                dialog1.setMessage(getString(R.string.internet_connection_description))
                dialog1.setPositiveButton(
                        R.string.ok, null)
                dialog1.setOnCancelListener()
                {
                    dialog = null
                }
                dialog = dialog1.show()
                handler = Handler(Looper.getMainLooper())
                handler?.postDelayed(runnable, 1000)
            }
        }
    }

    private fun welcomeOrSetUpScreen() {
        launch {
            withContext(Dispatchers.Main)
            {
                startActivity(Intent(this@HiderxSplashScreen, WelcomeActivity::class.java))
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                finish()
            }
        }
    }
}