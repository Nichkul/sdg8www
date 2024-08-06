package com.app.sdg8www

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.Serializable
class SettingsActivity : AppCompatActivity() {
    var uid: String? = null
    var database: FirebaseDatabase? = null
    var reference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val alert = AlertDialog.Builder(this@SettingsActivity)
                alert.setTitle("Please confirm to exit")
                alert.setPositiveButton("Exit") { dialogInterface: DialogInterface?, i: Int -> finish() }
                alert.setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int ->
                    dialogInterface?.dismiss()
                }
                alert.show()
            }
        })

        val logOutButton = findViewById<Button>(R.id.btn_logout)
        val icHome = findViewById<ImageView>(R.id.ic_home)
        val icInterview = findViewById<ImageView>(R.id.ic_interview)
        val icCommunity = findViewById<ImageView>(R.id.ic_community)
        val icSetting = findViewById<ImageView>(R.id.ic_setting)
        val txName = findViewById<TextView>(R.id.tx_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etName = findViewById<EditText>(R.id.et_name)
        val etLastName = findViewById<EditText>(R.id.et_last_name)
        val etBio = findViewById<EditText>(R.id.et_bio)
        val imgProfile = findViewById<ImageView>(R.id.img_profile)

        // Firebase 사용자 정보 가져오기
        val user = Firebase.auth.currentUser
        user?.let {
            uid = it.uid
            database = FirebaseDatabase.getInstance("https://sdg8-460da-default-rtdb.firebaseio.com/")
            reference = database!!.getReference("Users")
            reference!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val value = dataSnapshot.value as HashMap<String, Any>?
                        for ((id) in value!!) {
                            if (id == uid) {
                                val firstName = dataSnapshot.child(id).child("firstName").getValue(String::class.java)
                                val lastName = dataSnapshot.child(id).child("lastName").getValue(String::class.java)
                                val bio = dataSnapshot.child(id).child("bio").getValue(String::class.java)
                                val image = dataSnapshot.child(id).child("image").getValue(String::class.java)

                                // 사용자 정보 화면에 설정
                                txName.text = "$firstName $lastName"
                                etEmail.setText(it.email)
                                etName.setText(firstName)
                                etLastName.setText(lastName)
                                etBio.setText(bio)

                                // 프로필 이미지 로드
                                val ref: StorageReference = FirebaseStorage.getInstance().getReference("ProfilePictures").child(image!!)
                                val ONE_MEGABYTE = (1024 * 1024).toLong()
                                ref.getBytes(ONE_MEGABYTE)
                                    .addOnSuccessListener(OnSuccessListener<ByteArray> { bytes ->
                                        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                        imgProfile.setImageBitmap(bmp)
                                    }).addOnFailureListener(OnFailureListener {
                                        // 파일 또는 경로를 찾을 수 없음
                                    })
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        icHome.setOnClickListener {
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        icInterview.setOnClickListener {
            val intent = Intent(this@SettingsActivity, InterviewCoachActivity::class.java)
            startActivity(intent)
            finish()
        }
        icCommunity.setOnClickListener {
            val intent = Intent(this@SettingsActivity, WorkCommunityActivity::class.java)
            startActivity(intent)
            finish()
        }

        logOutButton.setOnClickListener {
            Firebase.auth.signOut()
            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}