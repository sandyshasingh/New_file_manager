package com.simplemobiletools.filemanager.pro;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Toolbar;

import com.simplemobiletools.commons.AppProgressDialog;
import com.simplemobiletools.commons.AppThemePrefrences;
import com.simplemobiletools.commons.ThemeUtils;
import com.simplemobiletools.commons.activities.BaseSimpleActivity;

import java.util.HashMap;
import java.util.Map;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class Feedback extends BaseSimpleActivity {
/*
    private Toolbar toolbar;
  // private DatabaseReference mDatabase;
    private EditText emailEditText;
    private EditText userNameEditText;
    private EditText queryEditText;
    private AppProgressDialog appProgressDialog;
    //private FirebaseFirestore db;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setThemeForFeedback();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        emailEditText = findViewById(R.id.email_Edit);
        userNameEditText = findViewById(R.id.nameEditText);
        queryEditText    =  findViewById(R.id.query_Edit);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

       // mDatabase = FirebaseDatabase.getInstance().getReference();

        getSupportActionBar().setTitle(getResources().getString(R.string.feedback_suggestions));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.gradientShadow).setVisibility(View.GONE);
        }


        findViewById(R.id.feedback).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                validateInputs();


            }
        });
    }

    private void validateInputs() {
        removeErros();

        if (TextUtils.isEmpty(userNameEditText.getText().toString())){
            ThemeUtils.setErrorForEditText(this,getResources().getString(R.string.please_enter_email_id),userNameEditText );
            return;
        }

        if (!ThemeUtils.isValidEmail(emailEditText.getText().toString())){
            ThemeUtils.setErrorForEditText(this,getResources().getString(R.string.please_enter_valid_email),emailEditText );
            return;
        }

        if (TextUtils.isEmpty(queryEditText.getText().toString())){
            ThemeUtils.setErrorForEditText(this,"Please enter query",queryEditText );
            return;
        }

        String userId = AppThemePrefrences.getUserId(getApplicationContext());
        // showProgressDialog();
        writeNewUser(userId,userNameEditText.getText().toString(), emailEditText.getText().toString(), queryEditText.getText().toString() );
        Toast toast =  Toasty.success(getApplicationContext(), getResources().getString(R.string.feedback_submit_success), Toast.LENGTH_SHORT,true);//.show();
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
        finish();
    }

    private void removeErros() {
        ThemeUtils.removeError(userNameEditText);
        ThemeUtils.removeError(emailEditText);
        ThemeUtils.removeError(queryEditText);
    }

    private void writeNewUser(String userId, String name, String email, String query) {
        String appVersionCode = AppThemePrefrences.getAppVersionName(getApplicationContext());
        String commingFrom="";
        if (getIntent()!=null) {
            commingFrom = getIntent().getStringExtra("CF");
        }
        if (TextUtils.isEmpty(commingFrom)){
            commingFrom = "NA";
        }

        // UserFeedback user = new UserFeedback(userId,name, email, query,appVersionCode,commingFrom);
        Map<String, Object> userFeedback = new HashMap<>();
        userFeedback.put("userid", userId);
        userFeedback.put("username", name);
        userFeedback.put("email", email);
        userFeedback.put("appversionCode", appVersionCode);
        userFeedback.put("query", query);
        userFeedback.put("commingFrom", commingFrom);

        if (mDatabase!=null) {
            mDatabase.keepSynced(true);
            mDatabase.child("feedback").child(userId).setValue(userFeedback)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {

                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }

                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    //  dismissProgressDialog();
                    //Toast toast =  Toasty.success(getApplicationContext(), "Sorry, Error in sending feedback.!", Toast.LENGTH_SHORT,true);//.show();
                    //toast.setGravity(Gravity.TOP|Gravity.CENTER_VERTICAL,150,0);
                    //toast.show();
                }
            });



            // Toast.makeText(getApplicationContext(), "Feedback has been submitted successfully. Thank you!", Toast.LENGTH_LONG).show();


        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public String getAppVersionCode() {
        try {
            PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            return version;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void showProgressDialog() {
        if (ThemeUtils.getActivityIsAlive(this)) {
            appProgressDialog = new AppProgressDialog(this);
            appProgressDialog.setCancelable(true);
            appProgressDialog.show();
        }
    }


    private void dismissProgressDialog() {
        if (appProgressDialog!=null && appProgressDialog.isShowing()) {
            appProgressDialog.dismiss();
        }
    }

    private void setThemeForFeedback() {
        try {
            if (!ThemeUtils.checkThemeIsDark(ThemeUtils.indexOfDarkTheme, ThemeUtils.getSeletedTheme(this))) {
                ThemeUtils.onActivityCreateSetTheme(this);
            }

            if (ThemeUtils.checkThemeIsGradientDark(ThemeUtils.getSeletedTheme(this))) {
                ThemeUtils.onActivityCreateSetTheme(this);
            }
        }catch (Exception e){
            ///Theme has already set on style.xml for default case
        }

    }*/
}