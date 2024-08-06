class Post {
    var author: String? = null
    var content: String? = null
    var date: String? = null
    var likeCount: Int? = 0
    var profile: String? = null
    constructor(author: String, content: String, date: String, likeCount: Int, profile: String ) {
        this.author = author
        this.content = content
        this.date = date
        this.likeCount = likeCount
        this.profile = profile
    }
}