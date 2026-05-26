package com.example.bookingcinema.UserScreen;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.bookingcinema.AdminScreen.AdminMainActivity;
import com.example.bookingcinema.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword, edtPhone, edtOtp;
    private Button btnLogin, btnGoogle, btnPhoneLogin, btnVerifyPhone, btnBiometric;
    private ProgressBar progressAuth;
    private TextView tvSignUp, tvForgot;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {
            checkRoleAndRoute(auth.getCurrentUser());
            return;
        }

        setContentView(R.layout.activity_login);
        bindViews();
        setupListeners();
    }

    private void bindViews() {
        edtEmail = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtPhone = findViewById(R.id.edtPhone);
        edtOtp = findViewById(R.id.edtOtp);
        progressAuth = findViewById(R.id.progressAuth);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnPhoneLogin = findViewById(R.id.btnPhoneLogin);
        btnVerifyPhone = findViewById(R.id.btnVerifyPhone);
        btnBiometric = findViewById(R.id.btnBiometric);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgot = findViewById(R.id.tvForgot);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogle.setOnClickListener(v -> loginWithGoogleProvider());
        btnPhoneLogin.setOnClickListener(v -> sendPhoneOtp());
        btnVerifyPhone.setOnClickListener(v -> verifyPhoneOtp());
        btnBiometric.setOnClickListener(v -> openBiometricPrompt());
        tvSignUp.setOnClickListener(v -> startActivity(new Intent(this, RegistersActivity.class)));
        tvForgot.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void loginWithEmail() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            toast("Vui lòng nhập email và mật khẩu");
            return;
        }
        setLoading(true);
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> handleAuthSuccess(result.getUser(), "email"))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Đăng nhập thất bại: " + safeMessage(e));
                });
    }

    private void loginWithGoogleProvider() {
        setLoading(true);
        OAuthProvider.Builder provider = OAuthProvider.newBuilder(GoogleAuthProvider.PROVIDER_ID);
        provider.addCustomParameter("prompt", "select_account");
        Task<AuthResult> pendingResult = auth.getPendingAuthResult();
        if (pendingResult != null) {
            handleAuthTask(pendingResult, "google");
        } else {
            handleAuthTask(auth.startActivityForSignInWithProvider(this, provider.build()), "google");
        }
    }

    private void handleAuthTask(Task<AuthResult> task, String provider) {
        task.addOnSuccessListener(result -> handleAuthSuccess(result.getUser(), provider))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Không thể đăng nhập: " + safeMessage(e));
                });
    }

    private void sendPhoneOtp() {
        String phone = edtPhone.getText().toString().trim();
        if (TextUtils.isEmpty(phone) || !phone.startsWith("+")) {
            toast("Số điện thoại cần có mã quốc gia, ví dụ +84901234567");
            return;
        }
        setLoading(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        signInWithPhoneCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        setLoading(false);
                        toast("Không gửi được OTP: " + safeMessage(e));
                    }

                    @Override
                    public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        verificationId = id;
                        setLoading(false);
                        toast("Đã gửi OTP. Vui lòng kiểm tra tin nhắn");
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyPhoneOtp() {
        String otp = edtOtp.getText().toString().trim();
        if (TextUtils.isEmpty(verificationId)) {
            toast("Vui lòng gửi OTP trước");
            return;
        }
        if (TextUtils.isEmpty(otp)) {
            toast("Vui lòng nhập mã OTP");
            return;
        }
        setLoading(true);
        signInWithPhoneCredential(PhoneAuthProvider.getCredential(verificationId, otp));
    }

    private void signInWithPhoneCredential(PhoneAuthCredential credential) {
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> handleAuthSuccess(result.getUser(), "phone"))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("Xác minh OTP thất bại: " + safeMessage(e));
                });
    }

    private void openBiometricPrompt() {
        int result = BiometricManager.from(this).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        if (result != BiometricManager.BIOMETRIC_SUCCESS) {
            toast("Thiết bị chưa sẵn sàng cho FaceID");
            return;
        }
        if (auth.getCurrentUser() == null) {
            toast("Hãy đăng nhập một lần trước khi dùng FaceID");
            return;
        }
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                checkRoleAndRoute(auth.getCurrentUser());
            }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực khuôn mặt CINE-LUXE")
                .setSubtitle("Mở khóa tài khoản khách hàng")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        prompt.authenticate(info);
    }

    private void handleAuthSuccess(FirebaseUser user, String provider) {
        syncUserProfile(user, provider);
        checkRoleAndRoute(user);
    }

    private void checkRoleAndRoute(FirebaseUser user) {
        if (user == null) {
            setLoading(false);
            toast("Không tìm thấy phiên đăng nhập");
            return;
        }
        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    String role = snapshot.getString("role");
                    if (role == null || role.trim().isEmpty()) {
                        readFallbackRole(user.getUid());
                    } else {
                        routeByRole(role);
                    }
                })
                .addOnFailureListener(e -> readFallbackRole(user.getUid()));
    }

    private void readFallbackRole(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(snapshot -> routeByRole(snapshot.getString("role")))
                .addOnFailureListener(e -> rejectUnauthorized());
    }

    private void routeByRole(String role) {
        setLoading(false);
        if ("admin".equalsIgnoreCase(role)) {
            toast("Đăng nhập quản trị thành công");
            openScreen(AdminMainActivity.class);
        } else if ("user".equalsIgnoreCase(role)) {
            toast("Đăng nhập thành công");
            openScreen(MainActivity.class);
        } else {
            rejectUnauthorized();
        }
    }

    private void rejectUnauthorized() {
        auth.signOut();
        setLoading(false);
        toast("Tài khoản chưa được phân quyền trong hệ thống CINE-LUXE");
    }

    private void syncUserProfile(FirebaseUser user, String provider) {
        if (user == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("uid", user.getUid());
        data.put("name", firstNonEmpty(user.getDisplayName(), "Khách CINE-LUXE"));
        data.put("email", firstNonEmpty(user.getEmail(), ""));
        data.put("phone", firstNonEmpty(user.getPhoneNumber(), edtPhone == null ? "" : edtPhone.getText().toString().trim()));
        data.put("provider", provider);
        data.put("vipPoint", 0);
        data.put("rank", "Ruby");
        db.collection("users").document(user.getUid()).set(data, SetOptions.merge());
    }

    private void setLoading(boolean loading) {
        if (progressAuth == null) return;
        progressAuth.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnGoogle.setEnabled(!loading);
        btnPhoneLogin.setEnabled(!loading);
        btnVerifyPhone.setEnabled(!loading);
        btnBiometric.setEnabled(!loading);
    }

    private void openScreen(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? "Lỗi không xác định" : e.getMessage();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
