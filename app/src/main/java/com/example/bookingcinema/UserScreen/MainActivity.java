package com.example.bookingcinema.UserScreen;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.bookingcinema.R;
import com.example.bookingcinema.UserScreen.Fragment.AccountFragment;
import com.example.bookingcinema.UserScreen.Fragment.ChatFragment;
import com.example.bookingcinema.UserScreen.Fragment.HistoryFragment;
import com.example.bookingcinema.UserScreen.Fragment.HomeFragment;
import com.example.bookingcinema.UserScreen.Fragment.NewsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final String KEY_CURRENT_TAB = "current_tab";
    private static final String TAB_HOME = "tab_home";
    private static final String TAB_HISTORY = "tab_history";
    private static final String TAB_CHAT = "tab_chat";
    private static final String TAB_NEWS = "tab_news";
    private static final String TAB_ACCOUNT = "tab_account";

    private String currentTab = TAB_HOME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            currentTab = savedInstanceState.getString(KEY_CURRENT_TAB, TAB_HOME);
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
        FragmentManager manager = getSupportFragmentManager();
        Fragment home = new HomeFragment();
        Fragment history = new HistoryFragment();
        Fragment chat = new ChatFragment();
        Fragment news = new NewsFragment();
        Fragment account = new AccountFragment();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.fragmentContainer, home, TAB_HOME);
        transaction.add(R.id.fragmentContainer, history, TAB_HISTORY).hide(history);
        transaction.add(R.id.fragmentContainer, chat, TAB_CHAT).hide(chat);
        transaction.add(R.id.fragmentContainer, news, TAB_NEWS).hide(news);
        transaction.add(R.id.fragmentContainer, account, TAB_ACCOUNT).hide(account);
        transaction.commitNow();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(menuIdForTab(currentTab));
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return showFragment(TAB_HOME);
            if (id == R.id.nav_history) return showFragment(TAB_HISTORY);
            if (id == R.id.nav_chat) return showFragment(TAB_CHAT);
            if (id == R.id.nav_notifications) return showFragment(TAB_NEWS);
            if (id == R.id.nav_account) return showFragment(TAB_ACCOUNT);
            return false;
        });
    }

    private boolean showFragment(String targetTab) {
        FragmentManager manager = getSupportFragmentManager();
        Fragment target = manager.findFragmentByTag(targetTab);
        if (target == null) {
            target = createFragment(targetTab);
            manager.beginTransaction()
                    .add(R.id.fragmentContainer, target, targetTab)
                    .commitNow();
        }

        FragmentTransaction transaction = manager.beginTransaction();
        hideIfPresent(transaction, TAB_HOME);
        hideIfPresent(transaction, TAB_HISTORY);
        hideIfPresent(transaction, TAB_CHAT);
        hideIfPresent(transaction, TAB_NEWS);
        hideIfPresent(transaction, TAB_ACCOUNT);
        transaction.show(target).commit();
        currentTab = targetTab;
        return true;
    }

    private void hideIfPresent(FragmentTransaction transaction, String tag) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) transaction.hide(fragment);
    }

    private Fragment createFragment(String tag) {
        if (TAB_HISTORY.equals(tag)) return new HistoryFragment();
        if (TAB_CHAT.equals(tag)) return new ChatFragment();
        if (TAB_NEWS.equals(tag)) return new NewsFragment();
        if (TAB_ACCOUNT.equals(tag)) return new AccountFragment();
        return new HomeFragment();
    }

    private int menuIdForTab(String tag) {
        if (TAB_HISTORY.equals(tag)) return R.id.nav_history;
        if (TAB_CHAT.equals(tag)) return R.id.nav_chat;
        if (TAB_NEWS.equals(tag)) return R.id.nav_notifications;
        if (TAB_ACCOUNT.equals(tag)) return R.id.nav_account;
        return R.id.nav_home;
    }
}
