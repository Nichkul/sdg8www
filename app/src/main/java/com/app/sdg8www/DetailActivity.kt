package com.app.sdg8www

import Post
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailActivity : AppCompatActivity() {
    // View 요소들
    private lateinit var icHome: ImageView
    private lateinit var icInterview: ImageView
    private lateinit var icCommunity: ImageView
    private lateinit var icSetting: ImageView
    private lateinit var txName: TextView
    private lateinit var txDesc: TextView
    private lateinit var listView: ListView
    private lateinit var writeComment: EditText
    private lateinit var btnSend: Button
    private lateinit var heart: ImageView

    // Database
    private lateinit var database: FirebaseDatabase
    private lateinit var postsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private var postListData: ArrayList<Post> = ArrayList()

    // 사용자 정보
    private var uid: String? = null
    private var firstName: String = ""
    private var lastName: String = ""
    private var profile : String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // Intent로부터 데이터 가져오기
        var id: String? = ""
        var name: String? = ""
        var description: String? = ""

        val extras = intent.extras
        if (extras != null) {
            id = extras.getString("id")
            name = extras.getString("name")
            description = extras.getString("description")
        }

        // View 초기화
        icHome = findViewById(R.id.ic_home)
        icInterview = findViewById(R.id.ic_interview)
        icCommunity = findViewById(R.id.ic_community)
        icSetting = findViewById(R.id.ic_setting)
        txName = findViewById(R.id.tx_name)
        txDesc = findViewById(R.id.tx_description)
        listView = findViewById(R.id.post_list_view)
        writeComment = findViewById(R.id.write_comment)
        btnSend = findViewById(R.id.btn_send)
        heart = findViewById(R.id.heart)

        // Firebase Realtime Database 초기화
        database = FirebaseDatabase.getInstance()
        postsRef = database.getReference("Jobs").child(id.toString()).child("postList")
        usersRef = database.getReference("Users")

        txName.text = name
        txDesc.text = description

        // 현재 사용자 UID 가져오기
        uid = FirebaseAuth.getInstance().currentUser?.uid

        // 사용자 이름 가져오기
        getUserData()

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val intent = Intent(this@DetailActivity, WorkCommunityActivity::class.java)
                startActivity(intent)
                finish()
            }})

        getPostFromDatabase()

        icHome.setOnClickListener {
            val intent = Intent(this@DetailActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        icInterview.setOnClickListener {
            val intent = Intent(this@DetailActivity, InterviewCoachActivity::class.java)
            startActivity(intent)
            finish()
        }
        icSetting.setOnClickListener {
            val intent = Intent(this@DetailActivity, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 댓글 전송 버튼 클릭 처리
        btnSend.setOnClickListener {
            addCommentToFirebase()
        }
    }

    // Firebase에서 사용자 정보 가져오기
    private fun getUserData() {
        uid?.let { userId ->
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    firstName = dataSnapshot.child("firstName").getValue(String::class.java) ?: ""
                    lastName = dataSnapshot.child("lastName").getValue(String::class.java) ?: ""
                    profile = dataSnapshot.child("profile").getValue(String::class.java) ?: ""

                    // 디버그 로그
                    Log.d("DetailActivity", "User Name: $firstName $lastName")
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("DetailActivity", "Failed to read user data", databaseError.toException())
                }
            })
        }
    }

    private fun getPostFromDatabase() {
        postsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                postListData.clear()  // 리스트를 새로 고침
                for (postSnapshot in dataSnapshot.children) {
                    val postData = postSnapshot.value as? Map<*, *>
                    if (postData != null) {
                        val author = postData["author"] as? String ?: ""
                        val content = postData["content"] as? String ?: ""
                        val date = postData["date"] as? String ?: ""
                        val likeCount = postData["likeCount"] as? String ?: "0"
                        val profile = postData["profile"]as? String?: ""
                        val post = Post(author, content, date, likeCount.toInt(), profile)
                        postListData.add(post)
                    }
                }
                // 어댑터 설정 및 리스트뷰 업데이트
                val adapterPost = PostAdapter(this@DetailActivity, postListData)
                listView.adapter = adapterPost

                // 리스트뷰를 맨 아래로 스크롤
                listView.setSelection(postListData.size - 1)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("RealtimeDatabase", "Error getting posts", databaseError.toException())
            }
        })
    }

    // Firebase에 댓글 추가
    private fun addCommentToFirebase() {
        val commentContent = writeComment.text.toString().trim()

        if (commentContent.isNotEmpty()) {
            // 날짜 포맷 설정
            val dateFormat = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val currentDate = dateFormat.format(Date())

            // 새로운 댓글 객체 생성
            val newPost = Post(
                author = "$firstName $lastName", // 사용자 이름으로 설정
                content = commentContent, // 댓글 내용을 사용
                date = currentDate, // 포맷된 현재 날짜 사용
                likeCount = 0,
                profile = profile
            )

            // Firebase에 포스트 추가
            postsRef.push().setValue(newPost).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // EditText 초기화
                    writeComment.text.clear()
                    // 새로운 포스트를 포함하도록 리스트 새로고침
                    getPostFromDatabase()
                } else {
                    Log.e("RealtimeDatabase", "Failed to add comment", task.exception)
                }
            }
        }
    }
}