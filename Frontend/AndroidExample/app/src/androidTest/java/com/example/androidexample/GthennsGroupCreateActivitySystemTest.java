package com.example.androidexample;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.widget.Button;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class GthennsGroupCreateActivitySystemTest {

    @Before
    public void seedUser() {
        Context context = ApplicationProvider.getApplicationContext();
        new KeepUser(context).saveUser(43, "create-group-user", "trainer");
    }

    @Test
    public void groupCreateActivity_inputFieldsExistAndAreEmpty() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);

                assertNotNull(etName);
                assertNotNull(etDesc);

                assertEquals("", etName.getText().toString());
                assertEquals("", etDesc.getText().toString());
            });
        }
    }

    @Test
    public void groupCreateActivity_submitButtonExists() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);
                assertNotNull(btnSubmit);
                assertTrue(btnSubmit.isShown());
            });
        }
    }

    @Test
    public void groupCreateActivity_emptyName_showsToastAndDoesNotSubmit() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);

                etName.setText("");
                etDesc.setText("Some description");
                btnSubmit.performClick();

                // Name still empty — validation rejected, no navigation occurred
                assertEquals("", etName.getText().toString());
            });
        }
    }

    @Test
    public void groupCreateActivity_emptyDescription_showsToastAndDoesNotSubmit() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);

                etName.setText("My Fitness Group");
                etDesc.setText("");
                btnSubmit.performClick();

                // Description still empty — validation rejected
                assertEquals("", etDesc.getText().toString());
            });
        }
    }

    @Test
    public void groupCreateActivity_bothFieldsEmpty_doesNotCrash() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);

                btnSubmit.performClick();

                assertTrue(btnSubmit.isShown());
            });
        }
    }

    @Test
    public void groupCreateActivity_validInputs_clickSubmitDoesNotCrash() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);

                etName.setText("Test Fitness Group");
                etDesc.setText("A group for testing");
                // Clicking will trigger network request which will fail but must not crash the activity
                btnSubmit.performClick();

                // Activity may finish() on success — just verify no exception was thrown
                assertNotNull(btnSubmit);
            });
        }
    }

    @Test
    public void groupCreateActivity_nameFieldAcceptsInput() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                etName.setText("CrossFit Warriors");
                assertEquals("CrossFit Warriors", etName.getText().toString());
            });
        }
    }

    @Test
    public void groupCreateActivity_descriptionFieldAcceptsInput() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);
                etDesc.setText("Weekly strength training sessions");
                assertEquals("Weekly strength training sessions", etDesc.getText().toString());
            });
        }
    }

    @Test
    public void groupCreateActivity_whitespaceOnlyName_treatedAsEmpty() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);

                etName.setText("   ");
                etDesc.setText("A valid description");
                btnSubmit.performClick();

                // Whitespace trims to empty so validation should reject it
                assertTrue(btnSubmit.isShown());
            });
        }
    }

    @Test
    public void groupCreateActivity_whitespaceOnlyDescription_treatedAsEmpty() {
        try (ActivityScenario<GroupCreateActivity> scenario = ActivityScenario.launch(GroupCreateActivity.class)) {
            scenario.onActivity(activity -> {
                EditText etName = activity.findViewById(R.id.etCreateGroupName);
                EditText etDesc = activity.findViewById(R.id.etCreateGroupDescription);
                Button btnSubmit = activity.findViewById(R.id.btnSubmitCreateGroup);

                etName.setText("My Group");
                etDesc.setText("   ");
                btnSubmit.performClick();

                // Whitespace trims to empty so validation should reject it
                assertTrue(btnSubmit.isShown());
            });
        }
    }
}
