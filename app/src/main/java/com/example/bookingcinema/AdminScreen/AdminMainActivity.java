package com.example.bookingcinema.AdminScreen;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bookingcinema.AdminScreen.Fragment.AdminPlaceholderFragment;
import com.example.bookingcinema.AdminScreen.Fragment.DashboardFragment;
import com.example.bookingcinema.AdminScreen.Fragment.FoodOrderFragment;
import com.example.bookingcinema.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.HashSet;
import java.util.Set;

public class AdminMainActivity extends AppCompatActivity {

    private static final String ADMIN_NOTIFICATION_CHANNEL_ID = "cine_luxe_admin_orders";
    private static final String ADMIN_NOTIFICATION_CHANNEL_NAME = "Thông báo đơn đặt vé";
    private static final int REQUEST_POST_NOTIFICATIONS = 6102;
    private static final String KEY_CURRENT_TAB = "admin_current_tab";
    private static final String TAB_DASHBOARD = "admin_dashboard";
    private static final String TAB_SCAN = "admin_scan";
    private static final String TAB_ORDERS = "admin_orders";
    private static final String TAB_SHOWTIME = "admin_showtime";

    private String currentTab = TAB_DASHBOARD;
    private ListenerRegistration adminNotificationRegistration;
    private final Set<String> notifiedNotificationIds = new HashSet<>();
    private boolean destroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString(KEY_CURRENT_TAB, TAB_DASHBOARD);
        } else {
            addInitialFragments();
        }
        setupBottomNavigation();
        showFragment(currentTab);
        createNotificationChannel();
        requestNotificationPermissionIfNeeded();
        listenForNewTickets();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENT_TAB, currentTab);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        destroyed = true; // FIX: Ngăn listener notification dùng Activity context sau khi đóng.
        if (adminNotificationRegistration != null) {
            adminNotificationRegistration.remove();
            adminNotificationRegistration = null;
        }
        super.onDestroy();
    }

    private void addInitialFragments() {
        Fragment dashboard = new DashboardFragment();
        Fragment scan = AdminPlaceholderFragment.newInstance("Soát vé", "Mô-đun máy quét QR sẽ kiểm tra trạng thái vé theo mã TicketID.");
        Fragment orders = new FoodOrderFragment();
        Fragment showtime = AdminPlaceholderFragment.newInstance("Lịch chiếu", "Mô-đun lịch chiếu quản lý suất chiếu theo phòng và ngày.");

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.adminFragmentContainer, dashboard, TAB_DASHBOARD);
        transaction.add(R.id.adminFragmentContainer, scan, TAB_SCAN).hide(scan);
        transaction.add(R.id.adminFragmentContainer, orders, TAB_ORDERS).hide(orders);
        transaction.add(R.id.adminFragmentContainer, showtime, TAB_SHOWTIME).hide(showtime);
        transaction.commitNow();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.adminBottomNav);
        bottomNav.setSelectedItemId(menuIdForTab(currentTab));
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.admin_nav_dashboard) return showFragment(TAB_DASHBOARD);
            if (id == R.id.admin_nav_scan) return showFragment(TAB_SCAN);
            if (id == R.id.admin_nav_orders) return showFragment(TAB_ORDERS);
            if (id == R.id.admin_nav_showtime) return showFragment(TAB_SHOWTIME);
            return false;
        });
    }

    private boolean showFragment(String targetTab) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment target = manager.findFragmentByTag(targetTab);
        if (target == null) {
            target = createFragment(targetTab);
            manager.beginTransaction().add(R.id.adminFragmentContainer, target, targetTab).commitNow();
        }

        FragmentTransaction transaction = manager.beginTransaction();
        hideIfPresent(transaction, TAB_DASHBOARD);
        hideIfPresent(transaction, TAB_SCAN);
        hideIfPresent(transaction, TAB_ORDERS);
        hideIfPresent(transaction, TAB_SHOWTIME);
        transaction.show(target).commit();
        currentTab = targetTab;
        return true;
    }

    private void hideIfPresent(FragmentTransaction transaction, String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) transaction.hide(fragment);
    }

    private Fragment createFragment(String tag) {
        if (TAB_SCAN.equals(tag)) return AdminPlaceholderFragment.newInstance("Soát vé", "Mô-đun máy quét QR sẽ kiểm tra trạng thái vé theo mã TicketID.");
        if (TAB_ORDERS.equals(tag)) return new FoodOrderFragment();
        if (TAB_SHOWTIME.equals(tag)) return AdminPlaceholderFragment.newInstance("Lịch chiếu", "Mô-đun lịch chiếu quản lý suất chiếu theo phòng và ngày.");
        return new DashboardFragment();
    }

    private void createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        ADMIN_NOTIFICATION_CHANNEL_ID,
                        ADMIN_NOTIFICATION_CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Thông báo tức thời khi khách hàng đặt vé thành công tại CINE-LUXE.");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{0, 450, 120, 450});
                channel.enableLights(true);
                channel.setLightColor(Color.parseColor("#FF0033"));

                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) {
                    manager.createNotificationChannel(channel);
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQUEST_POST_NOTIFICATIONS
                );
            }
        } catch (Exception ignored) {
        }
    }

    private void listenForNewTickets() {
        try {
            if (adminNotificationRegistration != null) {
                adminNotificationRegistration.remove(); // FIX: Tránh đăng ký trùng listener khi Activity được tạo lại.
            }
            adminNotificationRegistration = FirebaseFirestore.getInstance()
                    .collection("AdminNotifications")
                    .whereEqualTo("isRead", false)
                    .addSnapshotListener((snapshot, error) -> {
                        if (!isActivityAlive()) return; // FIX: Không xử lý callback nếu AdminMain đã destroy.
                        if (error != null || snapshot == null) {
                            return;
                        }

                        for (DocumentChange change : snapshot.getDocumentChanges()) {
                            if (change.getType() != DocumentChange.Type.ADDED) {
                                continue;
                            }
                            DocumentSnapshot document = change.getDocument();
                            String notificationId = document.getId();
                            if (!notifiedNotificationIds.add(notificationId)) {
                                continue;
                            }

                            String type = firstNonEmpty(document.getString("type"), "new_ticket");
                            if (!"new_ticket".equalsIgnoreCase(type)) {
                                continue;
                            }

                            String title = firstNonEmpty(document.getString("title"), "Có đơn đặt vé mới!");
                            String message = firstNonEmpty(document.getString("message"), "Một khách hàng vừa đặt vé thành công tại CINE-LUXE.");
                            if (showAdminNotification(notificationId, title, message)) {
                                markAdminNotificationAsRead(notificationId); // FIX: Chỉ đánh dấu đã đọc khi notification đã hiển thị thành công.
                            }
                        }
                    });
        } catch (Exception ignored) {
        }
    }

    private boolean showAdminNotification(String notificationId, String title, String message) {
        try {
            if (!isActivityAlive()) return false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }

            Intent intent = new Intent(this, AdminMainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
            }
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this,
                    Math.abs(notificationId.hashCode()),
                    intent,
                    pendingFlags
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, ADMIN_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_STATUS)
                    .setColor(ContextCompat.getColor(this, R.color.cinema_accent))
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
                    .setVibrate(new long[]{0, 450, 120, 450});

            NotificationManagerCompat.from(this).notify(Math.abs(notificationId.hashCode()), builder.build());
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private void markAdminNotificationAsRead(String notificationId) {
        try {
            FirebaseFirestore.getInstance()
                    .collection("AdminNotifications")
                    .document(notificationId)
                    .update("isRead", true, "readAt", FieldValue.serverTimestamp());
        } catch (Exception ignored) {
        }
    }

    private String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return "";
    }

    private boolean isActivityAlive() {
        return !destroyed && !isFinishing() && !isDestroyed();
    }

    private int menuIdForTab(String tag) {
        if (TAB_SCAN.equals(tag)) return R.id.admin_nav_scan;
        if (TAB_ORDERS.equals(tag)) return R.id.admin_nav_orders;
        if (TAB_SHOWTIME.equals(tag)) return R.id.admin_nav_showtime;
        return R.id.admin_nav_dashboard;
    }
}
