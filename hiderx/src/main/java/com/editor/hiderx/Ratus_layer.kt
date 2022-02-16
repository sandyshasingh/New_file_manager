package com.editor.hiderx

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.editor.hiderx.fragments.APPLICATION_ID
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.iarcuschin.simpleratingbar.SimpleRatingBar


class RateUs(private val activity: Activity?) {

    private var ratingStar = 5

    fun showRateUsDialogue() {

        val dialogView: View = activity!!.layoutInflater.inflate(
            R.layout.custom_rating_bottom_seet,
            null
        )
        val alertDialog = AlertDialog.Builder(activity)
        alertDialog.setView(dialogView)
        val dialog = alertDialog.create()
        dialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        dialog.window!!.setGravity(Gravity.BOTTOM)
        dialog.setContentView(dialogView)
        dialog.show()
        dialog.setCanceledOnTouchOutside(false)
        if (RemotConfigUtils.getDisableBackOnRateUsFlag(activity)) {
            dialog.setCancelable(false)
        }
        val imageHeader = dialog.findViewById<ImageView>(R.id.smile)
        val txtHeadingTv = dialog.findViewById<TextView>(R.id.txtHeading)
        if (txtHeadingTv != null) {
            val rateUsmsg: String = RemotConfigUtils.getRateUsMessage(activity)!!
            if (!TextUtils.isEmpty(rateUsmsg)) {
                txtHeadingTv.text = rateUsmsg
            }
        }
        val ratingPositiveButton = dialog.findViewById<View>(R.id.rating_positive_button) as Button?
        val positiveButton: String = RemotConfigUtils.getRateUsPositiveButtonMessage(activity)
        if (!TextUtils.isEmpty(positiveButton)) {
            ratingPositiveButton!!.text = positiveButton
        }
        val materialRatingBar: SimpleRatingBar = dialogView.findViewById(R.id.rating)
        materialRatingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
            ratingStar = ratingBar.rating.toInt()
            if (!ratingPositiveButton!!.isEnabled) {
                ratingPositiveButton.isEnabled = true
                ratingPositiveButton.setBackgroundResource(R.drawable.rectangle_border_green_solid_corner)
                ratingPositiveButton.setTextColor(activity.resources.getColor(R.color.white))
                imageHeader!!.setImageResource(R.drawable.feedback_very_sad)
            }
            when {
                rating < 2 -> {
                    txtHeadingTv!!.text = activity.resources.getString(R.string.Hated_it)
                    imageHeader!!.setImageResource(R.drawable.feedback_very_sad)
                }
                rating < 3 -> {
                    txtHeadingTv!!.text = activity.resources.getString(R.string.Disliked_it)
                    imageHeader!!.setImageResource(R.drawable.feedback_very_sad)
                }
                rating < 4 -> {
                    txtHeadingTv!!.text = activity.resources.getString(R.string.it_ok)
                    imageHeader!!.setImageResource(R.drawable.feedback_fair)
                }
                rating < 5 -> {
                    txtHeadingTv!!.text = activity.resources.getString(R.string.Liked_it)
                    imageHeader!!.setImageResource(R.drawable.feedback_happy)
                }
                rating > 4 -> {
                    txtHeadingTv!!.text = activity.resources.getString(R.string.Loved_it)
                    imageHeader!!.setImageResource(R.drawable.feedback_very_happy)
                }
            }
        }
        ratingPositiveButton!!.setOnClickListener {
            dialog.dismiss()
            try {
                if (ratingStar < HiderUtils.RATING_THRESHOLD) {
                    if (HiderUtils.getActivityIsAlive(activity)) {
                        feedbackDialog(activity)
                        // FirebaseAnalyticsUtils.sendEvent(activity.applicationContext, "RATE_BELOW_4", "RATE_BELLOW_4")
                        //if (!fromSettingsActivity) setToBeShown(false)
                    }
                } else {
                    val url = "https://play.google.com/store/apps/details?id="
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri
                                .parse(url + "callock.hideit.videohider.photohider.vault")
                        )
                    )
                }
            } catch (e: Exception) {
                FirebaseCrashlytics.getInstance().log(e.toString())
                FirebaseCrashlytics.getInstance().recordException(e)
                try {
                    activity.startActivity(
                        Intent(
                            Intent.ACTION_VIEW, Uri
                                .parse("https://play.google.com/store/apps/details?id=" + activity.packageName)
                        )
                    )
                } catch (e5: ActivityNotFoundException) {
                    FirebaseCrashlytics.getInstance().log(e.toString())
                    FirebaseCrashlytics.getInstance().recordException(e)
                } catch (e2: Exception) {
                    FirebaseCrashlytics.getInstance().log(e.toString())
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
            }

        }

        val cancelLayerButton = dialog.findViewById<ImageView>(R.id.cancelLayerButton)
        cancelLayerButton!!.setOnClickListener { dialog?.dismiss() }
        val ratingDismissButton = dialog
                .findViewById<View>(R.id.rating_dismiss_button) as Button?
        ratingDismissButton!!.setOnClickListener {
            dialog.dismiss()
            //setIsLater(true)
            //FirebaseAnalyticsUtils.sendEvent(activity, "LATER", "RATEUS_LATER")
        }
        if (activity != null && !activity.isFinishing) {
            dialog.show()
            //FirebaseAnalyticsUtils.sendEvent(activity, "SHOW", "RATEUS_SHOW")
        }
        //increaseLayerCount()
        //setDailogueOnCancelListener(dialog)
    }

    companion object {

        fun showRateUsLayer(activity: Activity): Boolean {
            if (!HiderUtils.isDeviceOnline(activity.applicationContext)) {
                Toast.makeText(activity, "Device is offline", Toast.LENGTH_SHORT).show()
                return false
            }
            val rateUs = RateUs(activity)
            rateUs.showRateUsDialogue()
            try {
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

        fun feedbackDialog(activity: Activity?) {
            val alertDialog = AlertDialog.Builder(activity!!)
            val inflater = LayoutInflater.from(activity)
            val diagview: View = inflater.inflate(R.layout.feedback_dialog_screen, null)
            alertDialog.setView(diagview)
            val lp = WindowManager.LayoutParams()
            val dialog = alertDialog.create()
            dialog.window!!.setBackgroundDrawableResource(R.drawable.rectangle_border_semitranparent_bg_corner)
            dialog.show()
            val editText = diagview.findViewById<EditText>(R.id.feedbacktext)
            val notNow = diagview.findViewById<Button>(R.id.notnow)
            val feedbackButton = diagview.findViewById<Button>(R.id.feedback_btn)
            feedbackButton.isEnabled = false
            notNow.setOnClickListener { dialog?.dismiss() }
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                    if (charSequence != null && charSequence.length > 0) {
                        feedbackButton.isEnabled = true
                        feedbackButton.setTextColor(activity.resources.getColor(R.color.green_v1))
                    } else {
                        feedbackButton.isEnabled = false
                        feedbackButton.setTextColor(activity.resources.getColor(R.color.grey700))
                    }
                }

                override fun afterTextChanged(editable: Editable) {}
            })
            feedbackButton.setOnClickListener {
                val string = editText.text.toString()
                if (!TextUtils.isEmpty(string)) {
                    val text = """$string App version ${HiderUtils.getAppVersionName(activity.applicationContext)}${HiderUtils.getDeviceConfiguration()}"""
                    HiderUtils.sendFeedBack(
                        activity,
                        "Needs improvements- Hiderx Feedback",
                        HiderUtils.FEEDBACK_EMAIL,
                        text
                    )
                    dialog.dismiss()
                } else {
                    Toast.makeText(
                        activity.applicationContext,
                        "Feedback is blank.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


}