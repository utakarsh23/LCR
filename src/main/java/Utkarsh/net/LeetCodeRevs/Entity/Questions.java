package Utkarsh.net.LeetCodeRevs.Entity;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "questions")
@NoArgsConstructor
public class Questions {


    @Setter
    @Getter
    @Id
    private ObjectId id;
    @Setter
    @Getter
    @DBRef
    private User user;
    @Setter
    @Getter
    private String questionLink;
    @Setter
    @Getter
    private List<String> solution;

    @CreatedDate
    private LocalDateTime createdDate;

}
