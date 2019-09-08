package com.atguigu.es.demo;

import io.searchbox.client.JestClient;
import io.searchbox.core.Delete;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Update;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsDemoApplicationTests {


    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() {
        System.out.println(jestClient);
    }

    /**
     * 1、测试给es索引一条数据
     */
    @Test
    public void index() throws IOException {

        User user = new User(UUID.randomUUID().toString().substring(0,5), "zhangsan@qq.com", 28);

        //1、获取一个Index动作的建造者，
        Index.Builder builder = new Index.Builder(user)
                .index("user")
                .type("info");

        //2、构造出这个index动作
        Index index = builder.build();

        //3、执行这个动作
        DocumentResult result = jestClient.execute(index);

        //4、打印结果
        System.out.println("刚才保存的是："+result.getId());
        System.out.println("刚才保存数据版本是："+result.getValue("_version"));

    }


    /**
     * 2、测试修改es的数据;
     * 修改，我们自己手动把要修改的对象放在 doc里面；
     * ==用index少传的字段就没了
     * ==用update我们自己手动把要修改的对象放在 doc里面；
     */
    @Test
    public void updateData() throws IOException {

        User user = new User();
        user.setAge(5000);
        user.setEmail("dsakdhasjkhdakjhdakjh@qq.com");
//        Map<String,User> u = new HashMap<>();
//        u.put("doc",user);



        //1、获取建造者，并构造数据
        Index.Builder builder = new Index.Builder(user)
                .index("user")
                .type("info")
                .id("AWx1EzUGqdatRxOHkGKo");

        //2、获取Action对象
        Index index = builder.build();

//        System.out.println(update.toString());
        //3、执行这个Action
        DocumentResult result = jestClient.execute(index);

        System.out.println(result+"修改完成....");
    }

    @Test
    public void updateData2() throws IOException {

        User user = new User();
        user.setAge(5000);
        user.setEmail("dsakdhasjkhdakjhdakjh@qq.com");
        //1、更新的时候手动放一个doc字段
        Map<String,User> u = new HashMap<>();
        u.put("doc",user);



        //1、获取建造者，并构造数据,注意传入的参数
        Update.Builder builder = new Update.Builder(u)
                .index("user")
                .type("info")
                .id("AWx1EzUGqdatRxOHkGKo");

        //2、获取Action对象
        Update index = builder.build();

//        System.out.println(update.toString());
        //3、执行这个Action
        DocumentResult result = jestClient.execute(index);

        System.out.println(result+"修改完成....");
    }





    /**
     * 2、测试修改es的数据
     */
    @Test
    public void deleteData() throws IOException {


        //1、获取建造者，并构造数据
        Delete build = new Delete.Builder("1").index("user").type("info").build();

        //2、获取Action对象
        //Update update = builder.build();

        //3、执行这个Action
        DocumentResult result = jestClient.execute(build);

        System.out.println(result+"删除完成....");
    }

}


@NoArgsConstructor
@AllArgsConstructor
@Data
class User{
    private String username;
    private String email;
    private Integer age;
}