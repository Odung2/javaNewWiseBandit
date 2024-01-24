package com.example.javanewwisebandit.goldentime_v1.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.javanewwisebandit.R;
import com.example.javanewwisebandit.goldentime_v1.Utils.UtilitiesDateTimeProcess;

//import kr.ac.kaist.jypark.goldentime_v1.R;
//import kr.ac.kaist.jypark.goldentime_v1.Utils.UtilitiesDateTimeProcess;


public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnConfirm = findViewById(R.id.confirmBtn);
        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText = findViewById(R.id.emailValue);
                String emailStr = editText.getText().toString();
                if (checkEmailValidation(emailStr) && !emailStr.contains("\n")) {
                    changeEmailRegisterStatus(emailStr);
                    finish();
                } else  Toast.makeText(getApplicationContext(), "이메일 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkEmailValidation(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private void changeEmailRegisterStatus(String email) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = pref.edit();
        editor.putBoolean("emailRegisterStatus", true);
        editor.putString("userName", email);
        editor.putInt("lastAppUsageUpdateTime", UtilitiesDateTimeProcess.getCurrentTimeHour());
        editor.putInt("lastTimeSlot", UtilitiesDateTimeProcess.getCurrentTimeHour());
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Toast.makeText(this, "확인버튼을 눌러 이메일을 등록하세요.", Toast.LENGTH_SHORT).show();
    }
}