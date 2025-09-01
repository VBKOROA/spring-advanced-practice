package org.example.expert.domain.todo;

import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.user.entity.User;

public class TodoHelper {
    public static Todo createSingleDummy(User user) {
        String title = "title";
        String contents = "content";
        String weather = "weather";
        return new Todo(title, contents, weather, user);
    }

    public static Todo createSingleDummyWith(User user, String suffix) {
        String title = "title" + suffix;
        String contents = "content" + suffix;
        String weather = "weather" + suffix;
        return new Todo(title, contents, weather, user);
    }
}
