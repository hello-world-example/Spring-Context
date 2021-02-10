package xyz.kail.demo.spring.context.common.service;

import org.springframework.stereotype.Service;
import xyz.kail.demo.spring.context.common.conf.PropertiesConf;

import javax.annotation.Resource;

@Service
public class PropertiesService {

    @Resource
    private PropertiesConf.PropertiesUtil propertiesUtil;

    public void print() {
        System.out.println(propertiesUtil.get("key1"));
    }

}
