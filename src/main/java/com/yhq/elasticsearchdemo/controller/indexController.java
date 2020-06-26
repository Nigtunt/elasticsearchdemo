package com.yhq.elasticsearchdemo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Author: YHQ
 * @Date: 2020/6/16 13:34
 */
@Controller
public class indexController {
    @RequestMapping({"/","/index"})
    public String index(){
        return "index";
    }
}
