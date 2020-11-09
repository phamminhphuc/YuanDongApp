package net.huansi.equipment.equipmentapp;

import android.app.Application;
import android.content.Context;

import net.huansi.equipment.equipmentapp.helpers.LocaleHelper;

public class MainApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }
}