package dz.usthb.eclipseworkspace.workspace.model;

import java.sql.Date;

public class AppUser {

    private int userId;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private String passwordHash;
    private Date createdAt;

    public AppUser() {}

    public AppUser(int userId, String email, String username, String firstName,
                   String lastName, String phone, String passwordHash, Date createdAt) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    public int getUser_id() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public Date getCreatedAt() { return createdAt; }
}
