package back.code.todo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subtodolist")
@Getter
@Setter
@NoArgsConstructor
public class SubTodoList {

    // @Id: Primary Key (id INT PRIMARY KEY NOT NULL AUTO_INCREMENT)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id; // 하위 할 일 항목 고유 ID

    // todolist_id (INT NOT NULL) - Foreign Key
    @Column(name = "todolist_id", nullable = false)
    private Integer todoListId; // 상위 할 일 항목 ID (FK)

    // title (VARCHAR(255) NOT NULL)
    @Column(name = "title", nullable = false)
    private String title; // 하위 할 일 내용
}