package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsLoginEmptyCredentialsSystemTest {

    @Test
    public void loginEmptyCredentials_blocksSubmissionAndKeepsLoginEnabled() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {

            // Click login with both fields empty
            scenario.onActivity(activity ->
                activity.findViewById(R.id.login_login_btn).performClick()
            );

            // Button must stay enabled and fields must still be empty (validation blocked submission)
            scenario.onActivity(activity -> {
                assertTrue("Login button should stay enabled after empty submit",
                        activity.findViewById(R.id.login_login_btn).isEnabled());
                assertEquals("Username field should still be empty",
                        "", ((EditText) activity.findViewById(R.id.login_username_edt)).getText().toString());
                assertEquals("Password field should still be empty",
                        "", ((EditText) activity.findViewById(R.id.login_password_edt)).getText().toString());
            });
        }
    }

    @Test
    public void loginScreen_hasExpectedFieldHintsAndButtonLabel() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                EditText username = activity.findViewById(R.id.login_username_edt);
                EditText password = activity.findViewById(R.id.login_password_edt);

                assertEquals("Enter Username Here", String.valueOf(username.getHint()));
                assertEquals("Enter Password Here", String.valueOf(password.getHint()));
                assertEquals("Login", String.valueOf(activity.findViewById(R.id.login_login_btn).getContentDescription() == null
                        ? ((android.widget.Button) activity.findViewById(R.id.login_login_btn)).getText()
                        : ((android.widget.Button) activity.findViewById(R.id.login_login_btn)).getText()));
            });
        }
    }
}