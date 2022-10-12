package dev.pulsarfunction.sentiment;

import java.io.Serializable;
import java.util.Map;
import java.util.StringJoiner;

/**
 *

 {"userInfo":"Tim Spann","contactInfo":"Tim Spann, Developer Advocate @ StreamNative","comment":"What is StreamNative the best thing in the world?"}


 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.StringJoiner;

/**
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Chat implements Serializable {
    private static final long serialVersionUID = 7L;
    public String userInfo;
    public String contactInfo;
    public String comment;
    public String prediction;


    public Chat(String userInfo, String contactInfo, String comment, String prediction) {
        super();
        this.userInfo = userInfo;
        this.contactInfo = contactInfo;
        this.comment = comment;
        this.prediction = prediction;
    }

    public Chat() {
        super();
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String userInfo) {
        this.userInfo = userInfo;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Chat.class.getSimpleName() + "[", "]")
                .add("userInfo='" + userInfo + "'")
                .add("contactInfo='" + contactInfo + "'")
                .add("comment='" + comment + "'")
                .add("prediction='" + prediction + "'")
                .toString();
    }
}
