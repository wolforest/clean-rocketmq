package com.wolf.minimq.hello.netty.pb;

public class HelloMain {
    public static void main(String[] args) {
        Hello.Person helloPb = Hello.Person.newBuilder()
            .setName("hello pb")
            .setAge(10)
            .build();

        System.out.println(helloPb);
        // output:
        // name: "hello pb"
        // age: 10

        Person person = new Person();
        person.setName("simple person");
        person.setAge(1);

        System.out.println(person);
        // output:
        // Person(name=simple person, age=1)
    }
}
