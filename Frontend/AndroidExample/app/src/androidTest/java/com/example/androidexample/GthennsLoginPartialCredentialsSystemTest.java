package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.widget.Button;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public class GthennsLoginPartialCredentialsSystemTest {

    private static Object invokePrivate(Object target, String methodName, Class<?>[] parameterTypes, Object[] args) {
        try {
            Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new AssertionError("Unable to invoke method: " + methodName, e);
        }
    }

    private static JSONObject jsonWith(String key, Object value) {
        JSONObject json = new JSONObject();
        try {
            json.put(key, value);
        } catch (JSONException e) {
            throw new AssertionError("Unable to build JSON for test", e);
        }
        return json;
    }

    @Test
    public void loginWithWhitespaceUsername_treatsUsernameAsMissing() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                EditText username = activity.findViewById(R.id.login_username_edt);
                EditText password = activity.findViewById(R.id.login_password_edt);
                Button loginButton = activity.findViewById(R.id.login_login_btn);

                username.setText("   ");
                password.setText("pw123");
                loginButton.performClick();

                assertTrue("Login button should stay enabled when trimmed username is empty", loginButton.isEnabled());
                assertEquals("   ", username.getText().toString());
                assertEquals("pw123", password.getText().toString());
            });
        }
    }

    @Test
    public void loginWithOnlyUsername_keepsLoginEnabledAndPreservesInputs() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                EditText username = activity.findViewById(R.id.login_username_edt);
                EditText password = activity.findViewById(R.id.login_password_edt);
                Button loginButton = activity.findViewById(R.id.login_login_btn);

                username.setText("calendarUser");
                password.setText("");
                loginButton.performClick();

                assertTrue("Login button should stay enabled when password is missing", loginButton.isEnabled());
                assertEquals("calendarUser", username.getText().toString());
                assertEquals("", password.getText().toString());
            });
        }
    }

    @Test
    public void loginWithOnlyPassword_keepsLoginEnabledAndPreservesInputs() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                EditText username = activity.findViewById(R.id.login_username_edt);
                EditText password = activity.findViewById(R.id.login_password_edt);
                Button loginButton = activity.findViewById(R.id.login_login_btn);

                username.setText("");
                password.setText("hunter2");
                loginButton.performClick();

                assertTrue("Login button should stay enabled when username is missing", loginButton.isEnabled());
                assertEquals("", username.getText().toString());
                assertEquals("hunter2", password.getText().toString());
            });
        }
    }

    @Test
    public void loginDefaultState_inputsEmptyAndButtonEnabled() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                EditText username = activity.findViewById(R.id.login_username_edt);
                EditText password = activity.findViewById(R.id.login_password_edt);
                Button loginButton = activity.findViewById(R.id.login_login_btn);

                assertEquals("", username.getText().toString());
                assertEquals("", password.getText().toString());
                assertTrue(loginButton.isEnabled());
            });
        }
    }

    @Test
    public void extractLoginSuccess_handlesBooleanStatusAndNullResponse() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                JSONObject ok = jsonWith("status", true);

                boolean okResult = (boolean) invokePrivate(
                        activity,
                        "extractLoginSuccess",
                        new Class<?>[]{JSONObject.class},
                        new Object[]{ok}
                );

                boolean nullResult = (boolean) invokePrivate(
                        activity,
                        "extractLoginSuccess",
                        new Class<?>[]{JSONObject.class},
                        new Object[]{null}
                );

                assertTrue(okResult);
                assertTrue(!nullResult);
            });
        }
    }

    @Test
    public void extractLoginSuccess_acceptsMessageAndUserIdFallback() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                JSONObject byMessage = jsonWith("message", "login successful");
                JSONObject byUserId = jsonWith("userId", 123);

                boolean messageResult = (boolean) invokePrivate(
                        activity,
                        "extractLoginSuccess",
                        new Class<?>[]{JSONObject.class},
                        new Object[]{byMessage}
                );

                boolean userIdResult = (boolean) invokePrivate(
                        activity,
                        "extractLoginSuccess",
                        new Class<?>[]{JSONObject.class},
                        new Object[]{byUserId}
                );

                assertTrue(messageResult);
                assertTrue(userIdResult);
            });
        }
    }

    @Test
    public void extractUsername_prefersUsernameThenAccountNameThenFallback() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                JSONObject usernameJson = jsonWith("username", "api-user");
                JSONObject accountNameJson = jsonWith("accountName", "acct-user");

                JSONObject emptyJson = new JSONObject();

                String fromUsername = (String) invokePrivate(
                        activity,
                        "extractUsername",
                        new Class<?>[]{JSONObject.class, String.class},
                        new Object[]{usernameJson, "fallback"}
                );

                String fromAccountName = (String) invokePrivate(
                        activity,
                        "extractUsername",
                        new Class<?>[]{JSONObject.class, String.class},
                        new Object[]{accountNameJson, "fallback"}
                );

                String fromFallback = (String) invokePrivate(
                        activity,
                        "extractUsername",
                        new Class<?>[]{JSONObject.class, String.class},
                        new Object[]{emptyJson, "fallback"}
                );

                assertEquals("api-user", fromUsername);
                assertEquals("acct-user", fromAccountName);
                assertEquals("fallback", fromFallback);
            });
        }
    }

    @Test
    public void extractUserId_andReadableVolleyError_coverFallbackBranches() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                JSONObject withUserId = jsonWith("userId", "42");

                JSONObject withoutUserId = new JSONObject();

                String parsedId = (String) invokePrivate(
                        activity,
                        "extractUserId",
                        new Class<?>[]{JSONObject.class},
                        new Object[]{withUserId}
                );

                String emptyId = (String) invokePrivate(
                        activity,
                        "extractUserId",
                        new Class<?>[]{JSONObject.class},
                        new Object[]{withoutUserId}
                );

                String nullErrorMessage = (String) invokePrivate(
                        activity,
                        "readableVolleyError",
                        new Class<?>[]{com.android.volley.VolleyError.class},
                        new Object[]{null}
                );

                assertEquals("42", parsedId);
                assertEquals("", emptyId);
                assertEquals("Unknown error", nullErrorMessage);
            });
        }
    }
}
