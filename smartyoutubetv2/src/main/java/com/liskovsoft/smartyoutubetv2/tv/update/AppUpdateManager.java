package com.liskovsoft.smartyoutubetv2.tv.update;

import android.annotation.SuppressLint;
import android.content.Context;
import com.liskovsoft.appupdatechecker2.AppUpdateChecker;
import com.liskovsoft.appupdatechecker2.AppUpdateCheckerListener;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.OptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.models.playback.ui.UiOptionItem;
import com.liskovsoft.smartyoutubetv2.common.app.presenters.VideoSettingsPresenter;
import com.liskovsoft.smartyoutubetv2.tv.R;

import java.util.ArrayList;
import java.util.List;

public class AppUpdateManager implements AppUpdateCheckerListener {
    private static final String UPDATE_MANIFEST_URL = "https://github.com/yuliskov/SmartYouTubeTV/releases/download/stable/smartyoutubetv.json";
    @SuppressLint("StaticFieldLeak")
    private static AppUpdateManager sInstance;
    private final Context mContext;
    private final AppUpdateChecker mUpdateChecker;
    private final VideoSettingsPresenter mSettingsPresenter;
    private boolean mUpdateInstalled;

    public AppUpdateManager(Context context) {
        mContext = context;
        mUpdateChecker = new AppUpdateChecker(mContext, this);
        mSettingsPresenter = VideoSettingsPresenter.instance(context);
    }

    public static AppUpdateManager instance(Context context) {
        if (sInstance == null) {
            sInstance = new AppUpdateManager(context.getApplicationContext());
        }

        return sInstance;
    }

    public void unhold() {
        sInstance = null;
    }

    public void start() {
        mUpdateInstalled = false;
        mUpdateChecker.checkForUpdates(UPDATE_MANIFEST_URL);
    }

    @Override
    public void onUpdateFound(String versionName, List<String> changelog, String apkPath) {
        showUpdateDialog(versionName, changelog, apkPath);
    }

    private void showUpdateDialog(String versionName, List<String> changelog, String apkPath) {
        mSettingsPresenter.clear();

        mSettingsPresenter.appendStrings(mContext.getString(R.string.update_changelog), createChangelogOptions(changelog));
        mSettingsPresenter.appendButton(
                UiOptionItem.from(mContext.getString(R.string.install_update), optionItem -> {
                    mUpdateChecker.installUpdate();
                    mUpdateInstalled = true;
                }, false));

        mSettingsPresenter.showDialog(String.format("%s %s", mContext.getString(R.string.app_name), versionName), this::unhold);
    }

    private List<OptionItem> createChangelogOptions(List<String> changelog) {
        List<OptionItem> options = new ArrayList<>();

        for (String change : changelog) {
            options.add(UiOptionItem.from(change, null, false));
        }

        return options;
    }

    @Override
    public void onError(Exception error) {
        // NOP
    }
}