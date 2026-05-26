package com.example.bookingcinema.AdminScreen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bookingcinema.AdminScreen.Fragment.AdminPlaceholderFragment;
import com.example.bookingcinema.AdminScreen.Fragment.DashboardFragment;
import com.example.bookingcinema.AdminScreen.Fragment.FoodOrderFragment;
import com.example.bookingcinema.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminMainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_TAB = "admin_current_tab";
    private static final String TAB_DASHBOARD = "admin_dashboard";
    private static final String TAB_SCAN = "admin_scan";
    private static final String TAB_ORDERS = "admin_orders";
    private static final String TAB_SHOWTIME = "admin_showtime";

    private String currentTab = TAB_DASHBOARD;

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
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENT_TAB, currentTab);
        super.onSaveInstanceState(outState);
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

    private int menuIdForTab(String tag) {
        if (TAB_SCAN.equals(tag)) return R.id.admin_nav_scan;
        if (TAB_ORDERS.equals(tag)) return R.id.admin_nav_orders;
        if (TAB_SHOWTIME.equals(tag)) return R.id.admin_nav_showtime;
        return R.id.admin_nav_dashboard;
    }
}
