package org.studyproject.metagram.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.studyproject.metagram.domain.util.MessageHelper;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

import static org.studyproject.metagram.config.Literals.*;

@Data
@NoArgsConstructor
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = PLEASE_FILL_MSG)
    @Length(max = 2048, message = MESSAGE_TOO_LONG)
    private String text;

    @NotBlank(message = PLEASE_ADD_TAGS)
    @Length(max = 255)
    private String tag;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User author;

    private String filename;

    @ManyToMany
    @JoinTable(name = "message_likes", joinColumns = {@JoinColumn(name = "message_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")})
    private Set<User> likes = new HashSet<>();

    public Message(String text, String tag, User user) {
        this.text = text;
        this.tag = tag;
        this.author = user;
    }

    public String getAuthorName() {
        return MessageHelper.getAuthorName(author);
    }


    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", tag='" + tag + '\'' +
                ", author=" + author.getUsername() +
                ", filename='" + filename + '\'' +
                ", likes=" + likes +
                '}';
    }
}
