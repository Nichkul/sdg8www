package com.app.sdg8www

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import Post
import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.bumptech.glide.Glide // import Glide library

class PostAdapter(
    private val context: Context,
    private val postsRef: DatabaseReference,
    private val postList: List<Post>,
    private val currentUserId: String
) : BaseAdapter() {

    override fun getCount(): Int = postList.size

    override fun getItem(position: Int): Any = postList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.post_list, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val post = postList[position]
        val postId = post.postId

        holder.userName.text = post.author
        holder.date.text = post.date
        holder.content.text = post.content
        holder.heartCount.text = post.likeCount.toString()

        // 프로필 이미지 설정
        if (!post.profile.isNullOrEmpty()) { // profile이 null이 아니고 비어 있지 않은 경우
            Glide.with(context)
                .load(post.profile) // URL or URI of the image
                .placeholder(R.drawable.loading) // 이미지 로딩 중 표시할 플레이스홀더
                .error(R.drawable.sad_face) // 로딩 실패 시 표시할 이미지
                .into(holder.txProfile) // ImageView where the image will be loaded
        } else {
            holder.txProfile.setImageResource(R.drawable.background_circle_blue) // 기본 프로필 이미지 설정
        }

        if (post.likeUsers.contains(currentUserId)) {
            holder.heart.setBackgroundResource(R.drawable.heart_image)
        } else {
            holder.heart.setBackgroundResource(R.drawable.heart_image_empty)
        }

        holder.heart.setOnClickListener {
            if (currentUserId.isNotEmpty()) {
                val postRef = postsRef.child(postId!!)

                if (post.likeUsers.contains(currentUserId)) {
                    post.likeCount = post.likeCount!! - 1
                    post.likeUsers.remove(currentUserId)
                } else {
                    post.likeCount = post.likeCount!! + 1
                    post.likeUsers.add(currentUserId)
                }

                postRef.child("likeCount").setValue(post.likeCount)
                postRef.child("likeUsers").setValue(post.likeUsers)

                notifyDataSetChanged()
            } else {
                Log.e("PostAdapter", "Current user ID is not set")
            }
        }

        return view
    }

    private class ViewHolder(view: View) {
        val userName: TextView = view.findViewById(R.id.user_name)
        val date: TextView = view.findViewById(R.id.date)
        val content: TextView = view.findViewById(R.id.context)
        val heart: ImageView = view.findViewById(R.id.heart)
        val heartCount: TextView = view.findViewById(R.id.heart_cnt)
        val txProfile: ImageView = view.findViewById(R.id.profile)
    }
}
