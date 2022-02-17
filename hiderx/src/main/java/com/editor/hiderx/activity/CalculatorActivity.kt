package com.editor.hiderx.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.editor.hiderx.*
import com.editor.hiderx.HiderUtils.PASSWORD_KEY
import com.editor.hiderx.Utility.IS_CALCULATOR
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import java.io.File
import java.io.FileWriter

const val REQUEST_CODE_TO_MANAGE_EXTERNAL_STORAGE = 200

class CalculatorActivity : AppCompatActivity() {


    private var changePassword: Boolean? = false
    private  var operand: java.util.ArrayList<Double>? = null
    private var operators: ArrayList<Char>? = null
    private var isCalculator : Boolean? = false
    private var input_password : String? = ""
    var android_id : String? = null
    var password : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCalculator = intent?.getBooleanExtra(IS_CALCULATOR, false)!!
        changePassword = intent?.getBooleanExtra(Utility.KEY_CHANGE_PASSWORD, false)
        setContentView(R.layout.activity_login)
        when {
            isCalculator!! -> {
                password = HiderUtils.getSharedPreference(this, PASSWORD_KEY)
                setListeners()
            }
            changePassword!! -> {
                password = HiderUtils.getSharedPreference(this, PASSWORD_KEY)
                android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                instr?.text = getString(R.string.input_current_password)
                instr?.doVisible()
            }
            else -> {
                android_id = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                instr?.text = getString(R.string.input_four_digits)
                instr?.doVisible()
            }
        }
        setGeneralListener()
    }

    private fun setUpPassword()
    {
        if(tv_expression?.text?.length!! == 4) {
            if(input_password == "") {
                input_password = tv_expression?.text?.toString()
                tv_expression?.setText("")
                instr?.text = getString(R.string.confirm_password)
            } else {
                if(tv_expression?.text?.toString() == input_password) {
                    setPassword()
                   checkPermissionAndNavigate(true)
                } else {
                    input_password = ""
                    instr?.setText(R.string.input_four_digits)
                }
                tv_expression?.setText("")
            }
        }
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
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                startActivity(Intent(this, HomeScreen::class.java))
            }
            else
            {
                finish()
            }
        }
        intent.putExtra(Utility.KEY_STORAGE_PERMISSION, true)
        resultLauncher.launch(intent)
    }

    private fun setPassword() {
        HiderUtils.setSharedPreference(this, PASSWORD_KEY, input_password)
//        val database = FirebaseDatabase.getInstance()
//        val myRef = database.getReference("passwords")
//        val exists = myRef.child(android_id!!).key!=null
//        if(exists)
//            myRef.child(android_id!!).removeValue()
//        myRef.child(android_id!!).push().setValue(input_password)
        val file = File(StorageUtils.getHiderDirectory().path + "/$PASSWORD_FILE_NAME")
        if(file.exists())
        {
            file.delete()
        }
            file.createNewFile()
            val writer = FileWriter(file)
            writer.append(input_password)
            writer.flush()
            writer.close()
    }

    private fun setGeneralListener() {
        btn_equal?.setOnClickListener()
        {
            when {
                isCalculator!! -> {
                    setEqualOperation()
                }
                changePassword!! -> {
                    if(tv_expression?.text?.length!! == 4) {
                            if(tv_expression?.text?.toString() == password)
                            {
                                changePassword = false
                                instr?.text = getString(R.string.input_four_digits)
                                instr?.doVisible()
                            }
                            else
                            {
                                instr?.text = getString(R.string.wrong_password)
                            }
                    }
                    else
                    {
                        Toast.makeText(this, "enter 4 digit passwords", Toast.LENGTH_SHORT).show()
                    }
                    tv_expression?.setText("")
                }
                else -> {
                        setUpPassword()
                }
            }
        }
        btn_clear?.setOnClickListener()
        {
            val currentExpression = tv_expression?.text?.toString()
            if(!TextUtils.isEmpty(currentExpression))
            {
                tv_expression.setText(currentExpression?.subSequence(0, currentExpression.length - 1))
            }
        }

        btn_more?.setOnClickListener()
        {
            try {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(MORE_APPS_LINK)
                startActivity(i)
                FirebaseAnalyticsUtils.sendEvent(this,"MORE_APPS","MORE_APPS")
            } catch (e: Exception) {
                Toast.makeText(this, " This option can not open in your device",Toast.LENGTH_LONG).show()
                FirebaseCrashlytics.getInstance().log(e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }

        btn_all_clear?.setOnClickListener()
        {
            tv_expression?.setText("")
            tv_result?.setText("")
        }
        digit_0?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("0")
        }
        digit_1?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("1")
        }
        digit_2?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("2")
        }
        digit_3?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("3")
        }
        digit_4?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("4")
        }
        digit_5?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("5")
        }
        digit_6?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("6")
        }
        digit_7?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("7")
        }
        digit_8?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("8")
        }
        digit_9?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("9")
        }
        digit_0?.setOnClickListener()
        {
            if(isCalculator!! || tv_expression?.text?.length!!<4)
            setNumber("0")
        }
    }

    private fun setEqualOperation() {
        val currentExpression = tv_expression?.text?.toString()
        if(!TextUtils.isEmpty(currentExpression))
        {
            if(currentExpression == password)
            {
                val file = File(StorageUtils.getHiderDirectory().path + "/$PASSWORD_FILE_NAME")
                if(!file.exists())
                {
                    file.createNewFile()
                    val writer = FileWriter(file)
                    writer.append(password)
                    writer.flush()
                    writer.close()
                }
               checkPermissionAndNavigate(false)
            }
            else if(currentExpression == "11223344")
            {
                startActivity(Intent(this, CalculatorActivity::class.java))
                finish()
            }
            else
            {
                val lastChar = currentExpression?.get(currentExpression.length - 1)
                if(lastChar!='-' && lastChar!='+' && lastChar!='*' && lastChar!='/')
                    calculateExpression(currentExpression!!)
            }
        }
    }

    private fun checkPermissionAndNavigate(fromSetUpPassword: Boolean) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
        {
            if(Environment.isExternalStorageManager())
            {
                val intent = Intent(this, HomeScreen::class.java)
                intent.putExtra(HiderUtils.KEY_FROM_SETUP_PASSWORD, fromSetUpPassword)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }
            else
            {
                startPermissionActivityForStorage()
            }
        }
        else {
            if (hasStoragePermission()) {
                val intent = Intent(this, HomeScreen::class.java)
                intent.putExtra(HiderUtils.KEY_FROM_SETUP_PASSWORD, fromSetUpPassword)
                startActivity(intent)
            } else {
                startPermissionActivityForStorage()
            }
        }
        finish()
    }


    private fun setListeners() {
        btn_divide?.setOnClickListener()
        {
            setOperation("/")
        }
        plus?.setOnClickListener()
        {
                setOperation("+")
        }
        btn_minus?.setOnClickListener()
        {
            setOperation("-")
        }
        btn_multiply?.setOnClickListener()
        {
            setOperation("*")
        }
        btn_dot?.setOnClickListener()
        {
            setDecimal()
        }
    }

    private fun setOperation(operation: String) {
        val currentExpression = tv_expression?.text?.toString()
        var lastChar : Char = ' '
            var lastTwoCharacters : String = ""
            if(currentExpression?.length!!>0)
                lastChar = currentExpression[currentExpression.length - 1]
        if(lastChar != '.') {
            if(currentExpression.length>1)
                lastTwoCharacters = currentExpression.substring(currentExpression.length - 2, currentExpression.length)
            if(lastTwoCharacters == "*-" || lastTwoCharacters == "/-")
            {
                tv_expression?.setText(currentExpression.substring(0, currentExpression.length - 2) + operation)
            }
            else if(operation == "+")
            {
                if(!TextUtils.isEmpty(currentExpression))
                {
                    if(lastChar == '*' || lastChar == '/' || lastChar == '+' || lastChar == '-')
                    {
                        tv_expression?.setText(currentExpression.substring(0, currentExpression.length - 1) + operation)
                    }
                    else
                    {
                        tv_expression?.setText(currentExpression + operation)
                    }
                }
            }
            else if(operation == "/" ||  operation == "*")
            {
                if(!TextUtils.isEmpty(currentExpression))
                {
                    if(lastChar == '*' || lastChar == '/' || lastChar == '+' || lastChar == '-')
                    {
                        if(currentExpression.length>1)
                            tv_expression?.setText(currentExpression.substring(0, currentExpression.length - 1) + operation)
                    }
                    else
                    {
                        tv_expression?.setText(currentExpression + operation)
                    }
                }
            }
            else if(operation == "-" && lastChar!='-')
            {
                if(lastChar == '+')
                {
                    tv_expression?.setText(currentExpression.substring(0, currentExpression.length - 1) + operation)
                }
                else
                {
                    tv_expression?.setText(currentExpression + operation)
                }
            }
            tv_expression?.setSelection(tv_expression?.text?.toString()?.length!!)
        }

    }

   fun calculateExpression(expr: String)
   {
       operand  = parseOperands(expr)
       operators = parseOperators(expr)
       if(operators?.isEmpty()!!)
       {
            return
       }
       calculateDivide()
       calculateMultiply()
       calculateAddition()
   }

    private fun calculateAddition() {
        val addIndexes : ArrayList<Int> = ArrayList()
        addIndexes.add(0)
        val minusIndex : ArrayList<Int> = ArrayList()
        for(i in 0 until operators?.size!!)
        {
            if(operators?.get(i) == '-')
            {
                minusIndex.add(i + 1)
            }
            else
            {
                addIndexes.add(i + 1)
            }
        }
        var sum : Double = 0.0
        for(i in addIndexes)
        sum += operand?.get(i)!!
        var minusSum : Double = 0.0
        for(i in minusIndex)
        {
            minusSum += operand?.get(i)!!
        }
        tv_result?.setText((sum - minusSum).toString())
    }

    private fun calculateMinus() {
        var enterLoop  = false
        for(i in 0 until operators?.size!!)
        {
            if(operators?.get(i) == '-')
            {
                enterLoop = true
                val operand1 = operand?.get(i)
                val operand2 = operand?.get(i + 1)
                operand?.removeAt(i)
                operand?.removeAt(i)
                operand?.add(i, operand1!! - operand2!!)
                break
            }
        }
        operators?.remove('-')
        if(enterLoop)
            calculateMinus()
    }

    private fun calculateAdd() {
        var enterLoop : Boolean = false
        for(i in 0 until operators?.size!!)
        {
            if(operators?.get(i) == '+')
            {
                enterLoop = true
                val operand1 = operand?.get(i)
                val operand2 = operand?.get(i + 1)
                operand?.removeAt(i)
                operand?.removeAt(i)
                operand?.add(i, operand1!! + operand2!!)
                break
            }
        }
        operators?.remove('+')!!
        if(enterLoop)
            calculateAdd()
    }

    private fun calculateMultiply() {
        var enterLoop = false
        for(i in 0 until operators?.size!!)
        {
            if(operators?.get(i) == '*')
            {
                enterLoop = true
                val operand1 = operand?.get(i)
                val operand2 = operand?.get(i + 1)
                operand?.removeAt(i)
                operand?.removeAt(i)
                operand?.add(i, operand1!! * operand2!!)
                break
            }
        }
        operators?.remove('*')
        if(enterLoop)
            calculateMultiply()
    }

    private fun calculateDivide() {
        var enterLoop = false
            for(i in 0 until operators?.size!!)
            {
                if(operators?.get(i) == '/')
                {
                    enterLoop = true
                    val operand1 = operand?.get(i)
                    val operand2 = operand?.get(i + 1)
                    operand?.removeAt(i)
                    operand?.removeAt(i)
                    operand?.add(i, operand1!! / operand2!!)
                    break
                }
            }
       operators?.remove('/')
        if(enterLoop)
            calculateDivide()
    }

    private fun parseOperators(expr: String): java.util.ArrayList<Char> {
        val operators : ArrayList<Char> = ArrayList()
        var isOperatorLast = false
        for(i in 0 until expr?.length)
        {
            if((expr[i] == '+' || expr[i] == '-' || expr[i] == '*' || expr[i] == '/') && !isOperatorLast && i!=0)
            {
                isOperatorLast = true
                operators.add(expr[i])
                continue
            }
            isOperatorLast = false
        }
        return operators
    }

    private fun parseOperands(expr: String): java.util.ArrayList<Double> {
        val operands : ArrayList<Double> = ArrayList()
        var operand = ""
        var isOperatorLast = true
        for(i in expr)
        {
            if((i == '+' || i == '-' || i == '*' || i == '/') && !isOperatorLast)
            {
                isOperatorLast = true
                operands.add(operand.toDouble())
                operand = ""
            }
            else
            {
                isOperatorLast = false
                operand += i
            }
        }
        operands.add(operand.toDouble())
        return operands
    }

    private fun parseAdditionOperands(expr: String): java.util.ArrayList<Double> {
        val operands : ArrayList<Double> = ArrayList()
        var operand = ""
        for(i in expr)
        {
            if(i == '+')
            {

            }
            if(i == '/' || i == '*')
            {
                operands.add(operand.toDouble())
                operand = ""
            }
            else if(i == '-')
            {
                if(operand != "")
                {
                    operands.add(operand.toDouble())
                }
                operand = "-"
            }
            else
                operand += i
        }
        if(operand != "")
            operands.add(operand.toDouble())
        return operands
    }

    private fun setDecimal() {
        val currentExpression = tv_expression?.text?.toString()
        if(TextUtils.isEmpty(currentExpression))
            tv_expression?.setText(".")
        else
        {
            if(isValidDecimal(currentExpression))
            {
                tv_expression?.setText("$currentExpression.")
            }
        }
        tv_expression?.setSelection(tv_expression?.text?.toString()?.length!!)
    }

    private fun isValidDecimal(currentExpression: String?): Boolean {
        if(currentExpression?.contains(".")!!)
        {
            val index = currentExpression.lastIndexOf(".")
            val subString = currentExpression.substring(index, currentExpression.length)
            if(subString.isNotEmpty())
            return subString.contains("+") || subString.contains("-") || subString.contains("/") || subString.contains("*")
        }
        return true
    }

    private fun setNumber(digit: String) {
        if(TextUtils.isEmpty(tv_expression?.text))
            tv_expression?.setText(digit)
        else
            tv_expression?.setText(tv_expression?.text?.toString() + digit)
        tv_expression?.setSelection(tv_expression?.text?.toString()?.length!!)
    }


}