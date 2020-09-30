package com.lixiande.test.service;

import com.lixiande.common.annotation.NettyRpcService;

import java.util.ArrayList;
import java.util.List;
@NettyRpcService(PersonService.class)
public class PersonServiceImpl implements PersonService{
    @Override
    public List<Person> callPerson(String name, Integer num) {
        List<Person> personList = new ArrayList<>(num);
        for (int i = 0; i< num; i++){
            personList.add(new Person(Integer.toString(i),name));
        }
        return personList;
    }
}
