package com.app.sdg8www

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Spinner
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.util.Log
import android.widget.ScrollView

class InterviewCoachActivity : AppCompatActivity() {

    private var listData1: ArrayList<String>? = null
    private var listData2: ArrayList<String>? = null
    private var listData3: ArrayList<String>? = null
    private var currentListData: ArrayList<String>? = null
    private var jobList: ArrayList<String>? = null
    private var currentIndex = 0
    private lateinit var database: DatabaseReference
    private lateinit var userEmail: String
    private lateinit var selectedJob: String
    private lateinit var layout: LinearLayout
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interview_coach)

        // Get the logged-in user's email
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            userEmail = user.email ?: "unknown_user"
        } else {
            Log.e("InterviewCoachActivity", "No user is logged in.")
            finish()  // Exit the activity if no user is logged in
            return
        }

        // Initialize the Firebase database reference with the user's email
        database = Firebase.database.reference.child("Interview").child(userEmail.replace(".", ","))

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val alert = AlertDialog.Builder(this@InterviewCoachActivity)
                alert.setTitle("Please confirm to exit")
                alert.setPositiveButton("Exit") { dialogInterface: DialogInterface?, i: Int -> finish() }
                alert.setNegativeButton("Cancel") { dialogInterface: DialogInterface?, i: Int ->
                    dialogInterface?.dismiss()
                }
                alert.show()
            }
        })

        listData1 = ArrayList()
        listData1!!.apply {
            add("Why are you interested in becoming a teacher/educator?")
            add("What do you think are the most important qualities of a good teacher?")
            add("How do you think technology can be used to help students learn?")
            add("Can you describe your ideal classroom?")
            add("What do you know about SDG#8?")
        }

        listData2 = ArrayList()
        listData2!!.apply {
            add("Can you think of a way technology can help people find jobs?")
            add("How could software help businesses create more jobs?")
            add("Can you imagine how technology can improve working conditions?")
            add("How can software help small businesses compete with bigger ones?")
            add("Can you think of a way to use technology to help people learn new skills for jobs?")
        }

        listData3 = ArrayList()
        listData3!!.apply {
            add("Why are you interested in a career in sustainability?")
            add("What are some of the environmental challenges the world is facing?")
            add("How do you stay informed about sustainability trends and news?")
            add("What does it mean for a job to be sustainable?")
            add("How would you approach a project to reduce a company's carbon footprint?")
        }

        jobList = ArrayList()
        jobList!!.apply {
            add("Teacher/Educator")
            add("Software Developer")
            add("Sustainability Consultant")
        }

        val jobSpinner = findViewById<Spinner>(R.id.job_spinner)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jobList!!)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        jobSpinner.adapter = spinnerAdapter

        jobSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedJob = jobList!![position]
                when (position) {
                    0 -> currentListData = listData1
                    1 -> currentListData = listData2
                    2 -> currentListData = listData3
                }
                currentIndex = 0
                layout.removeAllViews() // Clear the conversation when a new job is selected

                // Clear previous conversation entries in Firebase
                database.removeValue().addOnSuccessListener {
                    Log.d("InterviewCoachActivity", "Previous conversation entries cleared.")
                    displayNextQuestion() // Display the first question after clearing previous entries
                }.addOnFailureListener {
                    Log.e("InterviewCoachActivity", "Failed to clear previous conversation entries.", it)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        val icHome = findViewById<ImageView>(R.id.ic_home)
        val icInterview = findViewById<ImageView>(R.id.ic_interview)
        val icCommunity = findViewById<ImageView>(R.id.ic_community)
        val icSetting = findViewById<ImageView>(R.id.ic_setting)
        val result = findViewById<TextView>(R.id.tx_result)
        val header = findViewById<TextView>(R.id.tx_header)
        val etType = findViewById<EditText>(R.id.et_type)
        layout = findViewById(R.id.layout_list)
        scrollView = findViewById(R.id.scroll_view)
        val btnSend = findViewById<Button>(R.id.btn_send)

        btnSend.setOnClickListener {
            if (etType.text.isNotEmpty()) {
                val userMessage = etType.text.toString()
                addUserMessage(userMessage)

                // Append conversation entry to Firebase
                val botQuestion = currentListData!![currentIndex]
                val conversationEntry = ConversationEntry(userMessage, botQuestion)
                database.push().setValue(conversationEntry)

                // Move to the next question
                currentIndex++
                if (currentIndex < currentListData!!.size) {
                    displayNextQuestion()
                } else {
                    addBotQuestion("no more questions!")
                }

                etType.setText("")
                scrollToBottom()
            }
        }

        result.setOnClickListener {
            val intent = Intent(this@InterviewCoachActivity, MainAiActivity::class.java)
            startActivity(intent)
            finish()
        }
        icHome.setOnClickListener {
            val intent = Intent(this@InterviewCoachActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        icCommunity.setOnClickListener {
            val intent = Intent(this@InterviewCoachActivity, WorkCommunityActivity::class.java)
            startActivity(intent)
            finish()
        }
        icSetting.setOnClickListener {
            val intent = Intent(this@InterviewCoachActivity, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }

        val interviewPaint = header.paint
        val interviewWidth = interviewPaint.measureText(header.text.toString())
        val interviewTextShader: Shader = LinearGradient(
            0f, 0f, interviewWidth, header.textSize,
            intArrayOf(
                Color.parseColor("#915B5B"),
                Color.parseColor("#F79A9A")
            ), null, Shader.TileMode.REPEAT
        )
        header.paint.shader = interviewTextShader

        // Scroll to bottom when layout changes
        layout.viewTreeObserver.addOnGlobalLayoutListener {
            scrollToBottom()
        }
    }

    private fun displayNextQuestion() {
        if (currentIndex < currentListData!!.size) {
            val botQuestion = currentListData!![currentIndex]
            addBotQuestion(botQuestion)
        } else {
            val botQuestion = "no more questions!"
            addBotQuestion(botQuestion)
        }
        scrollToBottom()
    }

    private fun addUserMessage(message: String) {
        val textViewUser = TextView(this)
        textViewUser.gravity = Gravity.RIGHT
        textViewUser.setTextColor(Color.parseColor("#ffffff"))
        textViewUser.text = message
        textViewUser.setBackgroundResource(R.drawable.background_circle_gray)
        textViewUser.setPadding(20, 20, 20, 20)
        layout.addView(textViewUser)
    }

    private fun addBotQuestion(message: String) {
        val textViewBot = TextView(this)
        textViewBot.gravity = Gravity.LEFT
        textViewBot.setTextColor(Color.parseColor("#ffffff"))
        textViewBot.text = message
        textViewBot.setBackgroundResource(R.drawable.background_circle_result)
        textViewBot.setPadding(20, 20, 20, 20)
        layout.addView(textViewBot)
    }

    private fun scrollToBottom() {
        scrollView.post {
            scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    data class ConversationEntry(val userMessage: String, val botResponse: String)
}
