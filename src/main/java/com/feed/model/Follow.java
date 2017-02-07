package com.feed.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Follow {
    @Id
    private String follower;
    private String following;

    public String getFollower() {
        return follower;
    }

    public void setFollower(String follower) {
        this.follower = follower;
    }

    public String getFollowing() {
        return following;
    }

    public void setFollowing(String following) {
        this.following = following;
    }
}
