package org.ed.track.student;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.ed.track.DashBoardActivity;
import org.ed.track.R;
import org.ed.track.databinding.ActivityStudentDashboardBinding;
import org.ed.track.fragments.CalendarFragment;
import org.ed.track.fragments.HomeFragment;
import org.ed.track.fragments.MessagesFragment;
import org.ed.track.fragments.ProfileFragment;
import org.ed.track.model.UserProfile;
import org.ed.track.student.fragment.StudentHomeFragment;
import org.ed.track.utils.App;

public class StudentDashboard extends AppCompatActivity {

    private ActivityStudentDashboardBinding binding;

    private Fragment currentFragment;
    private long backPressedTime;
    private Toast backToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fetchUserProfile();

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            currentFragment = new StudentHomeFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, currentFragment).commit();
            binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        binding.bottomNavigation.setOnItemSelectedListener(item -> {

            Fragment selectedFragment = getFragment(item);

            if (selectedFragment != null && !selectedFragment.getClass().equals(currentFragment.getClass())) {
                currentFragment = selectedFragment;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, currentFragment).commit();
            }
            return true;
        });

        // backpress functionality
        // Back pressed handling using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!(currentFragment instanceof StudentHomeFragment)) {
                    // Navigate to home if not already on home
                    currentFragment = new StudentHomeFragment();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, currentFragment).commit();
                    binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                } else {
                    // Exit app if already on home
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        if (backToast != null) backToast.cancel();
                        finish(); // Or requireActivity().finish()
                    } else {
                        backToast = Toast.makeText(StudentDashboard.this, "Press back again to exit", Toast.LENGTH_SHORT);
                        backToast.show();
                        backPressedTime = System.currentTimeMillis();
                    }
                }
            }
        });

    }

    @Nullable
    private static Fragment getFragment(MenuItem item) {
        Fragment selectedFragment = null;

        if (item.getItemId() == R.id.nav_home) {
            selectedFragment = new StudentHomeFragment();
        } else if (item.getItemId() == R.id.nav_messages) {
            selectedFragment = new MessagesFragment();
        } else if (item.getItemId() == R.id.nav_calendar) {
            selectedFragment = new CalendarFragment();
        } else if (item.getItemId() == R.id.nav_profile) {
            selectedFragment = new ProfileFragment();
        }
        return selectedFragment;
    }

    private void fetchUserProfile() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance().collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.e("student profile data", documentSnapshot.toString());
                    if (documentSnapshot.exists()) {
                        UserProfile user = documentSnapshot.toObject(UserProfile.class);

                        if (user != null) {
                            // Display the data in your UI
                            App.saveString("name", user.getName());
                            App.saveString("email", user.getEmail());
                            App.saveString("location", user.getLocation());
//                            App.saveString("qualification", user.getQualification());
//                            App.saveString("bio", user.getBio());
//                            App.saveString("phone", user.getPhone());
                            App.saveString("subject", user.getSubject());
                            App.saveString("budget", user.getBudget());
                            App.saveString("profileImageUrl", user.getImageUrl());
                        }
                    } else {
                        Toast.makeText(App.getContext(), "User profile not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(App.getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserProfile();
    }
}