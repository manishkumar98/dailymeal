package com.example.dailymeal;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DeliveryPersonSettingsActivity extends AppCompatActivity {

    private EditText mNameField,mPhoneField,mFoodField;

    private Button mBack,mConfirm;
    private FirebaseAuth mAuth;
    private String userID;
    private String mName;
    private String mPhone;
    private String mFoodType;
    private String mService;
    private String mcustomertype;

    private DatabaseReference rootRef;
    private DatabaseReference mDeliveryPersonDatabase;
    private RadioGroup mRadioGroup,nRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_person_settings);

        mNameField=(EditText)findViewById(R.id.name);
        mPhoneField=(EditText)findViewById(R.id.phone);
        nRadioGroup=(RadioGroup)findViewById(R.id.radioGroupx);
        mRadioGroup=(RadioGroup)findViewById(R.id.radioGroup);

        mBack=(Button)findViewById(R.id.back);
        mConfirm=(Button)findViewById(R.id.confirm);

        mAuth=FirebaseAuth.getInstance();//taking firebase instance
        userID=mAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();
        mDeliveryPersonDatabase= rootRef.child("Users").child("deliveryperson").child(userID);
        getUserInfo();
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserInformation();
            }


        });
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });

    }
    private void getUserInfo(){
        mDeliveryPersonDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                    Map<String,Object> map=(Map<String, Object>)dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        mName=map.get("name").toString();
                        mNameField.setText(mName);
                    }
                    if(map.get("phone")!=null){
                        mPhone=map.get("phone").toString();
                        mPhoneField.setText(mPhone);
                    }
                    if(map.get("customertype")!=null){
                        mcustomertype = map.get("customertype").toString();
                        switch (mcustomertype){
                            case"Regular":
                                nRadioGroup.check(R.id.Regular);
                                break;
                            case"Occasional":
                                nRadioGroup.check(R.id.Occasional);
                                break;

                        }
                    }
                    if(map.get("service")!=null){
                        mService = map.get("service").toString();
                        switch (mService){
                            case"Small":
                                mRadioGroup.check(R.id.Small);
                                break;
                            case"Medium":
                                mRadioGroup.check(R.id.Medium);
                                break;
                            case"Large":
                                mRadioGroup.check(R.id.Large);
                                break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    private void saveUserInformation() {
        mName=mNameField.getText().toString();
        mPhone=mPhoneField.getText().toString();


        int selectId = mRadioGroup.getCheckedRadioButtonId();

        final RadioButton radioButton = (RadioButton) findViewById(selectId);

       int selectId1=nRadioGroup.getCheckedRadioButtonId();

       final RadioButton radioButton1=(RadioButton)findViewById(selectId1);


        if (radioButton.getText() == null){
            return;
        }
        if (radioButton1.getText() == null){
            return;
        }
        mService = radioButton.getText().toString();
        Map<String, Object> userInfo=new HashMap<String, Object>();
        userInfo.put("name",mName);
        userInfo.put("phone",mPhone);
        userInfo.put("customertype",mcustomertype);
        userInfo.put("service", mService);
        mDeliveryPersonDatabase.updateChildren(userInfo);
        finish();
    }

}
