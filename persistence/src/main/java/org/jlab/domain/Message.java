package org.jlab.domain;

import org.springframework.data.annotation.Id;

/**
 * Created by scorpiovn on 1/3/15.
 */
public class Message {

    @Id
    private String id;

    private String url;
    private String html;

    public Message() {}

    public Message(String url, String html) {
        this.url = url;
        this.html = html;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        if (html != null ? !html.equals(message.html) : message.html != null) return false;
        if (url != null ? !url.equals(message.url) : message.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (html != null ? html.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", html='" + html + '\'' +
                '}';
    }
}
