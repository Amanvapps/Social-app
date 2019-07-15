package e.aman.socialapp;

/**
 * Created by AMAN on 08/01/2019.
 */

public class Posts
{
    public String uid,date,description,postimage,profileimage,time,fullname;

    public Posts()
    {

    }


    public Posts(String uid, String date, String description, String postimage, String profileImage, String time, String fullname) {
        this.uid = uid;
        this.date = date;
        this.description = description;
        this.postimage = postimage;
        this.profileimage = profileImage;
        this.time = time;
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
