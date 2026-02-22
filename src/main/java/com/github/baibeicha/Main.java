package com.github.baibeicha;

import com.github.baibeicha.database.DataSource;
import com.github.baibeicha.database.Database;
import com.github.baibeicha.database.DatabaseType;
import com.github.baibeicha.database.Session;
import com.github.baibeicha.database.entity.Role;
import com.github.baibeicha.database.entity.User;
import com.github.baibeicha.ioc.annotation.configuration.TeaApplicationConfiguration;

@TeaApplicationConfiguration
public class Main {
    public static void main(String[] args) {
        //TeaApplication app = TeaApplication.run(Main.class);

        try (Database db = new Database(DataSource.builder()
                .type(DatabaseType.POSTGRESQL)
                .host("localhost")
                .port(5432)
                .database("plans_db")
                .username("user")
                .password("pass")
                .build()
        )) {
            db.createTable(User.class);
            db.createTable(Role.class);

            Session session = db.getSession();
            session.beginTransaction();

            User user = new User();
            user.username = "user";
            user.password = "pass";
            session.persist(user);

            Role role = new Role();
            role.name = "role";
            role.user = user;
            session.persist(role);

            session.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
