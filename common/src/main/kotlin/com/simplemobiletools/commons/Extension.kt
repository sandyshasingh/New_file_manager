package com.simplemobiletools.commons

import android.graphics.Typeface
import android.text.format.DateFormat
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.util.*

fun Throwable.logException(){
    try {
        FirebaseCrashlytics.getInstance().recordException(this)
    }catch (e: Exception){}
}

fun convertIntoDate(dateTaken: Long): String? {
    val smsTime: Calendar = Calendar.getInstance()
    smsTime.setTimeInMillis(dateTaken)
    val now: Calendar = Calendar.getInstance()
    return if (now.get(Calendar.DATE) === smsTime.get(Calendar.DATE) && now.get(Calendar.MONTH) === smsTime.get(
            Calendar.MONTH
        ) && now.get(Calendar.YEAR) === smsTime.get(Calendar.YEAR)
    ) {
        "Today " /*+ DateFormat.format(timeFormatString, smsTime);*/
    } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) === 1 && now.get(Calendar.MONTH) === smsTime.get(
            Calendar.MONTH
        ) && now.get(Calendar.YEAR) === smsTime.get(Calendar.YEAR)
    ) {
        "Yesterday" /*+ DateFormat.format(timeFormatString, smsTime);*/
    } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) > 1 && now.get(Calendar.DATE) - smsTime.get(
            Calendar.DATE
        ) < 5 && now.get(Calendar.MONTH) === smsTime.get(Calendar.MONTH)
    ) {
        DateFormat.format("EEEE", smsTime).toString()
    } else if (now.get(Calendar.YEAR) === smsTime.get(Calendar.YEAR)) {
        DateFormat.format("EEEE, dd MMM", smsTime).toString()
    } else {
        DateFormat.format("EEEE, dd MMM yyyy", smsTime).toString()
    }
}
fun TextView?.setTypeFaceOpenSensSmBold(){
    val typeface= Typeface.createFromAsset(this?.context?.assets, this?.context?.resources?.getString(R.string.text_2_path))
    this?.typeface = typeface
}

//fun TextView?.setTypeFaceJostBold(){
//    val typeface= Typeface.createFromAsset(this?.context?.assets, this?.context?.resources?.getString(R.string.text_2_path))
//    this?.typeface = typeface
//}

fun setTypeFaceOpenSensSmBold( vararg editText: EditText){
    editText.forEach {
        val typeface=Typeface.createFromAsset(it?.context?.assets, it?.context?.resources?.getString(R.string.text_2_path))
        it?.typeface = typeface
    }
}
