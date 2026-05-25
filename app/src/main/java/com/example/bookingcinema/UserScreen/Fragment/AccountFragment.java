package com.example.bookingcinema.UserScreen.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.EditProfileActivity;
import com.example.bookingcinema.UserScreen.HistoryActivity;
import com.example.bookingcinema.UserScreen.LoginActivity;
import com.example.bookingcinema.UserScreen.NotificationActivity;
import com.example.bookingcinema.UserScreen.PrivacyPolicyActivity;
import com.example.bookingcinema.UserScreen.TermsActivity;
import com.example.bookingcinema.Util.QrCodeGenerator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.WriterException;

import java.util.concurrent.Executor;

public class AccountFragment extends Fragment {

    private TextView tvUserName, tvUserEmail, tvVipPoint, tvMemberRank;
    private ImageView imgAvatar, imgPersonalQr;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvVipPoint = view.findViewById(R.id.tvVipPoint);
        tvMemberRank = view.findViewById(R.id.tvMemberRank);
        imgAvatar = view.findViewById(R.id.imgAvatar);
        imgPersonalQr = view.findViewById(R.id.imgPersonalQr);
        setupListeners(view);
        loadProfile();
    }

    private void setupListeners(View view) {
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnBiometric = view.findViewById(R.id.btnBiometric);
        TextView rowEditProfile = view.findViewById(R.id.rowEditProfile);
        TextView rowVoucher = view.findViewById(R.id.rowVoucher);
        TextView rowHistory = view.findViewById(R.id.rowHistory);
        TextView rowTerms = view.findViewById(R.id.rowTerms);
        TextView rowPrivacy = view.findViewById(R.id.rowPrivacy);

        btnBiometric.setOnClickListener(v -> openBiometricPrompt());
        rowEditProfile.setOnClickListener(v -> startActivity(new Intent(requireContext(), EditProfileActivity.class)));
        rowVoucher.setOnClickListener(v -> startActivity(new Intent(requireContext(), NotificationActivity.class)));
        rowHistory.setOnClickListener(v -> startActivity(new Intent(requireContext(), HistoryActivity.class)));
        rowTerms.setOnClickListener(v -> startActivity(new Intent(requireContext(), TermsActivity.class)));
        rowPrivacy.setOnClickListener(v -> startActivity(new Intent(requireContext(), PrivacyPolicyActivity.class)));
        btnLogout.setOnClickListener(v -> {
            auth.signOut();
            toast("Đã đăng xuất");
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void loadProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvUserName.setText("Khách CINE-LUXE");
            tvUserEmail.setText("Chưa đăng nhập");
            tvVipPoint.setText("Điểm tích lũy: 0");
            tvMemberRank.setText("Hạng thành viên: Ruby");
            drawMemberQr("CINE-LUXE|GUEST");
            return;
        }
        tvUserName.setText(firstNonEmpty(user.getDisplayName(), "Khách CINE-LUXE"));
        tvUserEmail.setText(firstNonEmpty(user.getEmail(), user.getPhoneNumber(), "Tài khoản Firebase"));
        if (user.getPhotoUrl() != null) {
            Glide.with(this).load(user.getPhotoUrl()).placeholder(R.drawable.ic_profile).error(R.drawable.ic_profile).circleCrop().into(imgAvatar);
        }
        drawMemberQr("CINE-LUXE|MEMBER|" + user.getUid());
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("name");
                    String rank = snapshot.getString("rank");
                    Long vipPoint = snapshot.getLong("vipPoint");
                    if (name != null && !name.trim().isEmpty()) tvUserName.setText(name);
                    tvMemberRank.setText("Hạng thành viên: " + firstNonEmpty(rank, "Ruby"));
                    tvVipPoint.setText("Điểm tích lũy: " + (vipPoint == null ? 0 : vipPoint));
                })
                .addOnFailureListener(e -> toast("Không tải được hồ sơ"));
    }

    private void drawMemberQr(String payload) {
        try {
            Bitmap bitmap = QrCodeGenerator.create(payload, 360);
            imgPersonalQr.setImageBitmap(bitmap);
        } catch (WriterException e) {
            toast("Không tạo được QR thành viên");
        }
    }

    private void openBiometricPrompt() {
        int availability = BiometricManager.from(requireContext()).canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
        if (availability != BiometricManager.BIOMETRIC_SUCCESS) {
            toast("Thiết bị chưa bật sinh trắc học");
            return;
        }
        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                toast("Xác thực sinh trắc học thành công");
            }
        });
        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực hội viên")
                .setSubtitle("Dùng sinh trắc học để xác nhận tài khoản CINE-LUXE")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();
        prompt.authenticate(info);
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) if (value != null && !value.trim().isEmpty()) return value.trim();
        return "";
    }

    private void toast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}
