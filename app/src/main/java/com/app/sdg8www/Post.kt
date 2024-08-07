class Post {
    var postId: String? = null
    var author: String? = null
    var content: String? = null
    var date: String? = null
    var likeCount: Int? = 0
    var profile: String? = null
    var likeUsers: MutableList<String> = mutableListOf()
    constructor(postId:String, author: String, content: String, date: String, likeCount: Int,
                profile: String, likeUsers: MutableList<String>) {
        this.postId = postId
        this.author = author
        this.content = content
        this.date = date
        this.likeCount = likeCount
        this.profile = profile
        this.likeUsers = likeUsers
    }
}