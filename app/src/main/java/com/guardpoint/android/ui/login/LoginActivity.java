package com.guardpoint.android.ui.login;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.guardpoint.android.R;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }
}
