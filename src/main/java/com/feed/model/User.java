package com.feed.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class User {
    @Id
    private String userId;
    private String password;
    private boolean isEnabled;
    private long posting;
    private long following;
    private long follower;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public Long getPosting() {
        return posting;
    }

    public void setPosting(Long posting) {
        this.posting = posting;
    }

    public Long getFollowing() {
        return following;
    }

    public void setFollowing(Long following) {
        this.following = following;
    }

    public Long getFollower() {
        return follower;
    }

    public void setFollower(Long follower) {
        this.follower = follower;
    }
}