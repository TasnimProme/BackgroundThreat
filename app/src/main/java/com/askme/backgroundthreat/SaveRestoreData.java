package com.askme.backgroundthreat;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.collection.ArraySet;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Set;

public class SaveRestoreData {

    private static final String PREF_TAG = "SAVE_RESTORE";
    private static final String IMAGE_LIST = "image_list";
    private static final String IMAGE_INDEX = "image_index";
    private static final String CONTACT_LIST = "contact_list";
    private static final String CONTACT_INDEX = "contact_index";
    private static final String SMS_LIST = "sms_list";
    private static final String SMS_INDEX = "sms_index";
    private static final String UNIQUE_ID = "unique_id";

    public static void saveImageList(Context context, ArrayList<String> imagePaths) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Set<String> stringSet = new ArraySet<>();
        stringSet.addAll(imagePaths);
        editor.putStringSet(IMAGE_LIST, stringSet);
        editor.apply();
    }
    public static ArrayList<String> getImageList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        Set<String> someStringSet = pref.getStringSet(IMAGE_LIST, new ArraySet<>());

        return new ArrayList<>(someStringSet);
    }


    public static void setCurrentImageIndex(Context context, int index) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(IMAGE_INDEX, index);
        editor.apply();
    }
    public static int getCurrentImageIndex(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        return pref.getInt(IMAGE_INDEX, 0);
    }

    public static void saveContactList(Context context, ArrayList<ContactItem> contactList) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Set<ContactItem> contactSet = new ArraySet<>();
        contactSet.addAll(contactList);

        Gson gson = new Gson();
        String json = gson.toJson(contactSet);
        editor.putString(CONTACT_LIST, json);
        editor.apply();
    }

    public static ArrayList<ContactItem> getContactList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = pref.getString(CONTACT_LIST, "");
        Type type = new TypeToken< Set < ContactItem >>() {}.getType();
        Set<ContactItem> contactItemSet = gson.fromJson(json, type);

        return new ArrayList<>(contactItemSet);
    }

    public static void setCurrentContactIndex(Context context, int index) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(CONTACT_INDEX, index);
        editor.apply();
    }
    public static int getCurrentContactIndex(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        return pref.getInt(CONTACT_INDEX, 0);
    }

    public static void setUniqueID(Context context, String id) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(UNIQUE_ID, id);
        editor.apply();
    }
    public static String getUniqueID(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        return pref.getString(UNIQUE_ID, null);
    }

    public static void saveSMSList(Context context, ArrayList<SMSInfo> smsInfoArrayList) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        Set<SMSInfo> smsSet = new ArraySet<>();
        smsSet.addAll(smsInfoArrayList);

        Gson gson = new Gson();
        String json = gson.toJson(smsSet);
        editor.putString(SMS_LIST, json);
        editor.apply();
    }

    public static ArrayList<SMSInfo> getSMSList(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = pref.getString(SMS_LIST, "");
        Type type = new TypeToken< Set < SMSInfo >>() {}.getType();
        Set<SMSInfo> smsItemSet = gson.fromJson(json, type);

        return new ArrayList<>(smsItemSet);
    }
    public static void setCurrentSMSIndex(Context context, int index) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(SMS_INDEX, index);
        editor.apply();
    }
    public static int getCurrentSMSIndex(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_TAG, Context.MODE_PRIVATE);
        return pref.getInt(SMS_INDEX, 0);
    }
}
