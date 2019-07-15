package e.aman.socialapp;

public class FindFriends
{
    public String status, profileimage, fullname;


    public FindFriends()
    {

    }


    public FindFriends(String status, String profileimage, String fullname) {
        this.status = status;
        this.profileimage = profileimage;
        this.fullname = fullname;
    }



    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
