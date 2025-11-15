package back.code.todo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    // Primary Key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id; // 하위 할 일 항목 고유 ID

    @ManyToOne // N:1 관계 (SubTodoList N개 -> TodoList 1개)
    @JoinColumn(name = "todolist_id", referencedColumnName = "todo_id", nullable = false)
    private TodoList todoList;

    // title (VARCHAR(255) NOT NULL)
    @Column(name = "title", nullable = false)
    private String title; // 하위 할 일 내용

    public Integer getTodoListId() {
        return this.todoList != null ? this.todoList.getTodoId() : null;
    }
}