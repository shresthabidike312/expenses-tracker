import java.sql.Date;

public class User {

    private int userId;
    private String username;
    private String contact;
    private Date dob;
    private String password;

    // Default constructor
    public User() {}

    // Constructor for Registration
    public User(String username, String contact, Date dob, String password) {
        this.username = username;
        this.contact = contact;
        this.dob = dob;
        this.password = password;
    }

    public User(int userId, String username, String contact, Date dob, String password) {
        this.userId = userId;
        this.username = username;
        this.contact = contact;
        this.dob = dob;
        this.password = password;
    }



    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", contact='" + contact + '\'' +
                ", dob=" + dob +
                '}';
    }
}