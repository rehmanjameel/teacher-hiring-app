package org.ed.track.register;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import org.ed.track.R;
import org.ed.track.databinding.ActivityMainBinding;
import org.ed.track.utils.App;

public class RegistrationActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Get reference to the string array defined in res/values/strings.xml
        String[] roles = getResources().getStringArray(R.array.role);

        // Create ArrayAdapter using the dropdown layout and the roles array
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                this,
                R.layout.dropdown_item, // your custom dropdown item layout
                roles
        );

        // Set adapter to AutoCompleteTextView
        binding.autoCompleteTextView.setAdapter(arrayAdapter);
        binding.autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedRole = parent.getItemAtPosition(position).toString();
            if (selectedRole.equals("Teacher")) {
                App.saveBoolean("is_teacher", true);
//                binding.layoutCourse.setVisibility(View.VISIBLE);
            } else {
                binding.layoutCourse.setVisibility(View.GONE);
            }
        });


        // Handle Register button click
        binding.btnRegister.setOnClickListener(view -> {
            getDetails();
        });

        binding.back.setOnClickListener(view -> {
            onBackPressed();
        });

    }

    private void getDetails() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
//        String course = binding.etCourse.getText().toString().trim();
        String role = binding.autoCompleteTextView.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty() || phone.isEmpty()) {
            // Show error or toast
            binding.etName.setError(name.isEmpty() ? "Required" : null);
            binding.etEmail.setError(email.isEmpty() ? "Required" : null);
            binding.etPassword.setError(password.isEmpty() ? "Required" : null);
            binding.etPhone.setError(password.isEmpty() ? "Required" : null);
            return;
        }

//        if (role.equals("Teacher") && course.isEmpty()) {
//            binding.etCourse.setError("Course is required for teachers");
//        } else {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            intent.putExtra("role", role);
            intent.putExtra("phone", binding.ccp.getSelectedCountryCodeWithPlus() + phone);
            startActivity(intent);
            finish();
//        }

        // Proceed with registration logic (e.g., save to Firebase or send to server)
        // For now, just show a success message (you can replace with actual logic)
        // Toast.makeText(this, role + " registered successfully!", Toast.LENGTH_SHORT).show();
    }
}