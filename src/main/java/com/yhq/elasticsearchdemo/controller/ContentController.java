package com.yhq.elasticsearchdemo.controller;

import com.yhq.elasticsearchdemo.service.contentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: YHQ
 * @Date: 2020/6/16 12:33
 */
@RestController
public class ContentController {
    @Autowired
    contentService service;

    @GetMapping("/parse/{key}")
    public boolean parse(@PathVariable String key) throws IOException {
        return service.parseContent(key);
    }

    @GetMapping("/search/{key}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> parse(@PathVariable String key,
                                           @PathVariable int pageNo,
                                           @PathVariable int pageSize) throws IOException {
        return service.search(key,pageNo,pageSize);
    }
    @GetMapping("/search2/{key}/{pageNo}/{pageSize}")
    public List<Map<String, Object>> parse2(@PathVariable String key,
                                           @PathVariable int pageNo,
                                           @PathVariable int pageSize) throws IOException {
        return service.searchHighLight(key,pageNo,pageSize);
    }
}
